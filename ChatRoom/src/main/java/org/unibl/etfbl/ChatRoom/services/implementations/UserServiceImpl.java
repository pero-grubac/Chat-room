package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.exceptions.BadRequestException;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.ApproveUser;
import org.unibl.etfbl.ChatRoom.models.dtos.ChangeRole;
import org.unibl.etfbl.ChatRoom.models.dtos.UserInput;
import org.unibl.etfbl.ChatRoom.models.dtos.UserOutput;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;
import org.unibl.etfbl.ChatRoom.services.EmailService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private UserEntityRepository repository;
    @Autowired
    private ModelMapper modelMapper;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ForumRoomEntityRepository forumRoomEntityRepository;
    @Autowired
    private CommentEntityRepository commentEntityRepository;
    @Autowired
    private SecurityEventService securityEvents;

    @Override
    public List<UserOutput> getAllUsers() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    @Override
    public List<UserOutput> getAllUsersByIsApproved(Byte isApproved) {
        return repository.getAllByIsApproved(isApproved).stream().map(this::toOutput).toList();
    }

    @Override
    public UserOutput createUser(UserInput user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new ConflictException("User exists");
        }
        UserEntity userEntity = modelMapper.map(user, UserEntity.class);
        if (userEntity.getUsername() == null || userEntity.getUsername().isBlank()
                || userEntity.getPassword() == null || userEntity.getPassword().isEmpty()
                || userEntity.getEmail() == null || userEntity.getEmail().isEmpty()) {
            throw new ConflictException("Incomplete user data");
        }
        requireValidEmail(user.getEmail());
        requireStrongPassword(user.getPassword());
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        // Only an administrator assigns roles, whatever the request says
        userEntity.setRole(null);
        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
        securityEvents.record(SecurityEvent.REGISTERED, userEntity.getUsername(), "Local account");
        return toOutput(userEntity);
    }

    @Override
    public UserOutput updateUser(String username, UserInput user) {
        UserEntity userEntity = findByUsername(username);

        if (user.getUsername() != null && !user.getUsername().isEmpty())
            userEntity.setUsername(user.getUsername());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            requireStrongPassword(user.getPassword());
            userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            requireValidEmail(user.getEmail());
            userEntity.setEmail(user.getEmail());
        }

        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
        return toOutput(userEntity);
    }

    @Override
    public void deleteUser(Integer id) {
        UserEntity userEntity = findById(id);
        // Comments reference the user; permissions and room memberships are removed with it
        commentEntityRepository.deleteAllByUser_IdUser(id);
        repository.delete(userEntity);
        securityEvents.record(SecurityEvent.USER_DELETED, "Deleted user " + userEntity.getUsername());
    }

    @Override
    public void approveUser(ApproveUser approveUser) throws NotFoundException, ConflictException {
        UserEntity userEntity = findById(approveUser.getId());
        if (userEntity.getRole() != null) {
            throw new ConflictException("The registration was already approved; use the access setting instead");
        }
        if (approveUser.isApproved()) {
            assignRole(userEntity, approveUser.getRole(), approveUser.getPermissions());
            assignRooms(userEntity, approveUser.getRoomIds());
            userEntity.setIsApproved((byte) 1);
        } else {
            userEntity.setIsApproved((byte) 0);
        }
        repository.saveAndFlush(userEntity);
        boolean approved = userEntity.getIsApproved() == 1;
        securityEvents.record(approved ? SecurityEvent.USER_APPROVED : SecurityEvent.USER_REJECTED,
                (approved ? "Approved " : "Rejected ") + userEntity.getUsername());
        emailService.sendEmail(userEntity.getEmail(), userEntity.getUsername(), "ChatRoom - registracija",
                approved ? "Odobren vam je zahtjev za registraciju." : "Vaš zahtjev za registraciju je odbijen.");
    }

    @Override
    public void setAccess(Integer id, boolean allowed, String actingUsername) {
        UserEntity userEntity = findById(id);
        if (userEntity.getRole() == null) {
            throw new ConflictException("The registration has not been approved yet");
        }
        if (Objects.equals(userEntity.getUsername(), actingUsername)) {
            throw new ConflictException("You cannot change your own access");
        }
        if (userEntity.isEnabled() == allowed) {
            return;
        }
        userEntity.setIsApproved((byte) (allowed ? 1 : 0));
        if (!allowed) {
            // A blocked user loses the current session at once, not when the JWT expires
            userEntity.setJWT(null);
            userEntity.setTwoFactorToken(null);
            userEntity.setTwoFactorTokenExpiresAt(null);
        }
        repository.saveAndFlush(userEntity);
        securityEvents.record(allowed ? SecurityEvent.USER_UNBLOCKED : SecurityEvent.USER_BLOCKED,
                (allowed ? "Restored access for " : "Blocked ") + userEntity.getUsername());
        emailService.sendEmail(userEntity.getEmail(), userEntity.getUsername(), "ChatRoom - pristup",
                allowed ? "Pristup forumu vam je ponovo omogućen." : "Pristup forumu vam je zabranjen.");
    }

    @Override
    public void changeRole(ChangeRole changeRole) throws NotFoundException, ConflictException {
        UserEntity userEntity = findById(changeRole.getId());
        if (!userEntity.isEnabled()) {
            throw new ConflictException("Unapproved user");
        }
        assignRole(userEntity, changeRole.getRole(), changeRole.getPermissions());
        if (changeRole.getRoomIds() != null) {
            assignRooms(userEntity, changeRole.getRoomIds());
        } else if (changeRole.getRole() == RoleEnum.ROLE_MODERATOR && userEntity.getRooms().isEmpty()) {
            throw new ConflictException("A moderator needs at least one room");
        }
        repository.saveAndFlush(userEntity);
        securityEvents.record(SecurityEvent.ROLE_CHANGED, userEntity.getUsername() + " is now " + changeRole.getRole()
                + " with " + changeRole.getPermissions() + (changeRole.getRoomIds() != null ? " in rooms " + changeRole.getRoomIds() : ""));
    }

    @Override
    public UserEntity findByUsername(String username) throws NotFoundException {
        return repository.getUserEntityByUsername(username).orElseThrow(
                () -> new NotFoundException("User with username " + username + " not found"));
    }

    @Override
    public void deleteJWT(String username) {
        UserEntity userEntity = findByUsername(username);
        userEntity.setJWT(null);
        repository.saveAndFlush(userEntity);
    }

    /** Replaces the user's rooms. Moderators need at least one: their permissions apply only there. */
    private void assignRooms(UserEntity user, List<Integer> roomIds) {
        List<Integer> ids = roomIds == null ? List.of() : roomIds;
        if (user.getRole() == RoleEnum.ROLE_MODERATOR && ids.isEmpty()) {
            throw new ConflictException("A moderator needs at least one room");
        }
        List<ForumRoomEntity> rooms = forumRoomEntityRepository.findAllById(ids);
        if (rooms.size() != new HashSet<>(ids).size()) {
            throw new NotFoundException("Unknown forum room in " + ids);
        }
        user.getRooms().clear();
        user.getRooms().addAll(rooms);
    }

    // The same rule the registration form shows: 8 to 72 characters (BCrypt ignores the rest),
    // with lower and upper case letters, a digit and a special character
    private static void requireStrongPassword(String password) {
        boolean strong = password.length() >= 8 && password.length() <= 72
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        if (!strong) {
            throw new BadRequestException("The password needs 8 to 72 characters with lower and upper case letters, "
                    + "a digit and a special character");
        }
    }

    private static void requireValidEmail(String email) {
        if (!EMAIL.matcher(email).matches()) {
            throw new BadRequestException("Invalid email address");
        }
    }

    private UserEntity findById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Sets the role and makes the permissions exactly the requested set: missing ones are added,
     * extra ones removed. Admins hold all permissions, regular users none, moderators at least one.
     */
    private void assignRole(UserEntity user, RoleEnum role, List<PermissionEnum> requested) {
        if (role == null) {
            throw new ConflictException("Invalid user role");
        }
        Set<PermissionEnum> wanted = EnumSet.noneOf(PermissionEnum.class);
        if (requested != null) {
            wanted.addAll(requested);
        }
        switch (role) {
            case ROLE_ADMIN -> {
                if (!wanted.equals(EnumSet.allOf(PermissionEnum.class))) {
                    throw new ConflictException("ROLE_ADMIN must have all permissions");
                }
            }
            case ROLE_MODERATOR -> {
                if (wanted.isEmpty()) {
                    throw new ConflictException("Permissions cannot be empty for ROLE_MODERATOR");
                }
            }
            case ROLE_KORISNIK -> {
                if (!wanted.isEmpty()) {
                    throw new ConflictException("ROLE_KORISNIK should not have any permissions");
                }
            }
        }
        user.setRole(role);
        if (user.getPermissions() == null) {
            user.setPermissions(new ArrayList<>());
        }
        user.getPermissions().removeIf(permission -> !wanted.contains(permission.getPermission()));
        Set<PermissionEnum> held = EnumSet.noneOf(PermissionEnum.class);
        user.getPermissions().forEach(permission -> held.add(permission.getPermission()));
        for (PermissionEnum permission : wanted) {
            if (!held.contains(permission)) {
                user.getPermissions().add(new PermissionEntity(permission, user));
            }
        }
    }

    private UserOutput toOutput(UserEntity user) {
        UserOutput output = modelMapper.map(user, UserOutput.class);
        output.setIsApproved(user.getIsApproved());
        output.setRoomIds(user.getRooms() == null ? List.of()
                : user.getRooms().stream().map(ForumRoomEntity::getIdForumRoom).sorted().toList());
        return output;
    }
}
