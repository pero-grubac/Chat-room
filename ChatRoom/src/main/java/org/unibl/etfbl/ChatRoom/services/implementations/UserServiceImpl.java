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
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.*;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.services.EmailService;
import org.unibl.etfbl.ChatRoom.services.ForumRoomService;
import org.unibl.etfbl.ChatRoom.services.PermissionService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserEntityRepository repository;
    @Autowired
    private PermissionService permissionService;

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

    @Override
    public List<UserOutput> getAllUsers() {
        return repository.findAll().stream().map(u -> modelMapper.map(u, UserOutput.class)).toList();
    }

    @Override
    public List<UserOutput> getAllUsersByIsApproved(Byte isApproved) {
        return repository.getAllByIsApproved(isApproved).stream().map(u -> modelMapper.map(u, UserOutput.class)).toList();
    }

    @Override
    public UserOutput getUser(Integer id) throws NotFoundException {
        return modelMapper.map(repository.findById(id).orElseThrow(
                () -> new NotFoundException("User with ID " + id + " not found")
        ), UserOutput.class);
    }

    @Override
    public UserOutput createUser(UserInput user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new ConflictException("User exists");
        } else {
            UserEntity userEntity = modelMapper.map(user, UserEntity.class);
            if (userEntity.getUsername() != null && !userEntity.getUsername().isEmpty()
                    && userEntity.getPassword() != null && !userEntity.getPassword().isEmpty()
                    && userEntity.getEmail() != null && !userEntity.getEmail().isEmpty()) {
                userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
                // MOZDA BILJEZI AKO NEKO POKUSA DA SETUJE ROLE NA NESTO
                userEntity.setRole(null);
                repository.saveAndFlush(userEntity);
                entityManager.refresh(userEntity);
                return modelMapper.map(userEntity, UserOutput.class);
            }
            throw new ConflictException("Incomplete user data");
        }
    }

    @Override
    public UserOutput updateUser(String username, UserInput user) {
        UserEntity userEntity = repository.getUserEntityByUsername(username).orElseThrow(
                () -> new NotFoundException("User with username " + username + " not found")
        );

        if (user.getUsername() != null && !user.getUsername().isEmpty())
            userEntity.setUsername(user.getUsername());

        if (user.getPassword() != null && !user.getPassword().isEmpty())
            userEntity.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getEmail() != null && !user.getEmail().isEmpty())
            userEntity.setEmail(user.getEmail());

        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
        return modelMapper.map(userEntity, UserOutput.class);
    }

    private boolean isValidRole(RoleEnum role) {
        return role != null && EnumSet.allOf(RoleEnum.class).contains(role);
    }

    @Override
    public void deleteUser(Integer id) {
        if (!repository.existsByIdUser(id)) {
            throw new NotFoundException("User with ID " + id + " not found");
        } else {
            repository.deleteById(id);
        }
    }

    private void checkAndSetUserRole(RoleEnum role, UserEntity userEntity) throws ConflictException {
        if (!isValidRole(role)) {
            throw new ConflictException("Invalid user role");
        }
        userEntity.setRole(role);
    }

    private boolean doesUserHavePermission(UserEntity user, PermissionEnum permission) {
        List<PermissionEntity> permisssions = user.getPermissions();
        for (PermissionEntity per : permisssions) {
            if (per.getPermission().equals(permission)) {
                return true;
            }
        }
        return false;
    }

    private void setPermissionsForRole(RoleEnum role, List<PermissionEnum> permissions, UserEntity userEntity) throws ConflictException {
        switch (role) {
            case ROLE_ADMIN:
                if (permissions.size() != EnumSet.allOf(PermissionEnum.class).size()) {
                    throw new ConflictException("ROLE_ADMIN must have all permissions");
                }
                for (PermissionEnum permission : PermissionEnum.values()) {
                    if (!doesUserHavePermission(userEntity, permission)) {
                        permissionService.create(new PermissionEntity(permission, userEntity));
                    }

                }
                break;
            case ROLE_MODERATOR:
                if (permissions.isEmpty()) {
                    throw new ConflictException("Permissions cannot be empty for ROLE_MODERATOR");
                }
                for (PermissionEnum permission : permissions) {
                    if (!doesUserHavePermission(userEntity, permission)) {
                        permissionService.create(new PermissionEntity(permission, userEntity));
                    }
                }
                break;
            case ROLE_KORISNIK:
                if (!permissions.isEmpty()) {
                    throw new ConflictException("ROLE_KORISNIK should not have any permissions");
                }
                permissionService.deleteAllByIdUser(userEntity.getIdUser());
                userEntity.setPermissions(null);
                break;
            default:
                throw new ConflictException("Unsupported user role");
        }
    }

    @Override
    public void approveUser(ApproveUser approveUser) throws NotFoundException, ConflictException {
        UserEntity userEntity = repository.findById(approveUser.getId()).orElseThrow(
                () -> new NotFoundException("User with ID " + approveUser.getId() + " not found")
        );
        // Ako je vec odobren
        if (userEntity.getIsApproved() != null && userEntity.getIsApproved() == 1) {
            // TODO
            throw new ConflictException("Invalid data");
        }
        userEntity.setIsApproved((byte) (approveUser.isApproved() ? 1 : 0));
        if (userEntity.getIsApproved() == 1) {
            checkAndSetUserRole(approveUser.getRole(), userEntity);
            setPermissionsForRole(approveUser.getRole(), approveUser.getPermissions(), userEntity);
            ForumRoomEntity forumRoom = forumRoomEntityRepository.findById(approveUser.getIdRoom()).orElseThrow(
                    () -> new NotFoundException("Forum room with id: " + approveUser.getIdRoom() + " doesn't exists."));
            userEntity.getRooms().add(forumRoom);
        }
        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
        emailService.sendEmail(userEntity.getEmail(), userEntity.getUsername(), "ChatRoom-regristacija", "Odobren vam je zahtjev za registraciju");
    }

    @Override
    public void changeRole(ChangeRole changeRole) throws NotFoundException, ConflictException {
        UserEntity userEntity = repository.findById(changeRole.getId()).orElseThrow(
                () -> new NotFoundException("User with ID " + changeRole.getId() + " not found")
        );
        if (userEntity.getIsApproved() == null || userEntity.getIsApproved() == 0) {
            throw new ConflictException("Unapproved user");
        }
        Set<ForumRoomEntity> rooms = userEntity.getRooms();

        checkAndSetUserRole(changeRole.getRole(), userEntity);
        setPermissionsForRole(changeRole.getRole(), changeRole.getPermissions(), userEntity);

        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
    }

    @Override
    public UserOutput findByEmail(String email) throws NotFoundException {
        return modelMapper.map(repository.getUserEntityByEmail(email).orElseThrow(
                () -> new NotFoundException("User with email " + email + " not found")
        ), UserOutput.class);
    }

    @Override
    public UserEntity findByUsername(String username) throws NotFoundException {
        return repository.getUserEntityByUsername(username).orElseThrow(
                () -> new NotFoundException("User with username " + username + " not found"));
    }

    @Override
    public void save(UserEntity userEntity) {
        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
    }

    @Override
    public void deleteJWT(String username) {
        UserEntity userEntity = repository.getUserEntityByUsername(username).orElseThrow(
                () -> new NotFoundException("User with username " + username + " not found"));
        userEntity.setJWT(null);
        repository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);
    }

}
