package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserEntityRepository repository;
    @Spy
    private ModelMapper modelMapper = new ModelMapper();
    @Mock
    private EntityManager entityManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private ForumRoomEntityRepository forumRoomEntityRepository;
    @Mock
    private CommentEntityRepository commentEntityRepository;
    @Mock
    private SecurityEventService securityEvents;

    @InjectMocks
    private UserServiceImpl userService;

    private final ForumRoomEntity science = ForumRoomEntity.builder().idForumRoom(1).name("Science").build();
    private final ForumRoomEntity music = ForumRoomEntity.builder().idForumRoom(2).name("Music").build();

    @BeforeEach
    void configureMapper() {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
    }

    private static UserEntity pendingUser() {
        UserEntity user = new UserEntity();
        user.setIdUser(7);
        user.setUsername("ana");
        user.setEmail("ana@example.com");
        user.setPassword("hash");
        user.setPermissions(new ArrayList<>());
        user.setRooms(new HashSet<>());
        return user;
    }

    private static UserEntity approvedUser(RoleEnum role, PermissionEnum... permissions) {
        UserEntity user = pendingUser();
        user.setIsApproved((byte) 1);
        user.setRole(role);
        for (PermissionEnum p : permissions) {
            user.getPermissions().add(new PermissionEntity(p, user));
        }
        return user;
    }

    private static Set<PermissionEnum> permissionsOf(UserEntity user) {
        Set<PermissionEnum> result = new HashSet<>();
        user.getPermissions().forEach(p -> result.add(p.getPermission()));
        return result;
    }

    private static ApproveUser approve(boolean isApproved, RoleEnum role, List<PermissionEnum> permissions) {
        ApproveUser approveUser = new ApproveUser();
        approveUser.setId(7);
        approveUser.setApproved(isApproved);
        approveUser.setRole(role);
        approveUser.setPermissions(permissions);
        approveUser.setRoomIds(role == RoleEnum.ROLE_MODERATOR ? List.of(1) : List.of());
        return approveUser;
    }

    private static ChangeRole changeRole(RoleEnum role, List<PermissionEnum> permissions, List<Integer> roomIds) {
        ChangeRole changeRole = new ChangeRole();
        changeRole.setId(7);
        changeRole.setRole(role);
        changeRole.setPermissions(permissions);
        changeRole.setRoomIds(roomIds);
        return changeRole;
    }

    // ── registration ───────────────────────────────────────────────

    @Test
    void registrationHashesThePasswordAndLeavesTheAccountPending() {
        when(repository.existsByUsername("ana")).thenReturn(false);
        when(passwordEncoder.encode("Strong#123")).thenReturn("bcrypt-hash");

        userService.createUser(UserInput.builder().username("ana").password("Strong#123").email("ana@example.com").build());

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("bcrypt-hash");
        assertThat(saved.getValue().getIsApproved()).isNull();
        assertThat(saved.getValue().getRole()).isNull();
        verify(securityEvents).record(SecurityEvent.REGISTERED, "ana", "Local account");
    }


    @Test
    void weakPasswordIsRejected() {
        when(repository.existsByUsername(anyString())).thenReturn(false);

        for (String weak : List.of("Sh#1abc", "nouppercase#1", "NOLOWERCASE#1", "NoDigits#here", "NoSpecial123", "Aa#1".repeat(19))) {
            assertThatThrownBy(() -> userService.createUser(UserInput.builder().username("ana").password(weak).email("a@b.cd").build()))
                    .as(weak).isInstanceOf(BadRequestException.class);
        }
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void invalidEmailIsRejected() {
        when(repository.existsByUsername("ana")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(UserInput.builder().username("ana").password("Strong#123").email("not-an-email").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void profileUpdateFollowsThePasswordRules() {
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(approvedUser(RoleEnum.ROLE_KORISNIK)));

        assertThatThrownBy(() -> userService.updateUser("ana", UserInput.builder().password("weak").build()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void duplicateUsernameIsRejected() {
        when(repository.existsByUsername("ana")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(UserInput.builder().username("ana").password("p").email("a@b.c").build()))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void incompleteRegistrationIsRejected() {
        when(repository.existsByUsername("ana")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(UserInput.builder().username("ana").password("p").build()))
                .isInstanceOf(ConflictException.class);
    }

    // ── approval ───────────────────────────────────────────────────

    @Test
    void approvingAsModeratorGrantsRolePermissionsAndRoom() {
        UserEntity ana = pendingUser();
        when(repository.findById(7)).thenReturn(Optional.of(ana));
        when(forumRoomEntityRepository.findAllById(List.of(1))).thenReturn(List.of(science));

        userService.approveUser(approve(true, RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD, PermissionEnum.UPDATE)));

        assertThat(ana.getIsApproved()).isEqualTo((byte) 1);
        assertThat(ana.getRole()).isEqualTo(RoleEnum.ROLE_MODERATOR);
        assertThat(ana.getRooms()).containsExactly(science);
        assertThat(permissionsOf(ana)).containsExactlyInAnyOrder(PermissionEnum.ADD, PermissionEnum.UPDATE);
        verify(emailService).sendEmail(eq("ana@example.com"), eq("ana"), anyString(), contains("Odobren"));
        verify(securityEvents).record(eq(SecurityEvent.USER_APPROVED), anyString());
    }

    @Test
    void adminMustReceiveAllPermissions() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> userService.approveUser(approve(true, RoleEnum.ROLE_ADMIN, List.of(PermissionEnum.ADD))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("all permissions");
    }

    @Test
    void moderatorWithoutPermissionsIsRejected() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> userService.approveUser(approve(true, RoleEnum.ROLE_MODERATOR, List.of())))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void regularUserCannotHavePermissions() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> userService.approveUser(approve(true, RoleEnum.ROLE_KORISNIK, List.of(PermissionEnum.DELETE))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approvalWithoutRoleIsRejected() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> userService.approveUser(approve(true, null, List.of())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("role");
    }

    @Test
    void approvalIntoMissingRoomFails() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));
        when(forumRoomEntityRepository.findAllById(List.of(1))).thenReturn(List.of());

        assertThatThrownBy(() -> userService.approveUser(approve(true, RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void moderatorNeedsARoom() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));
        ApproveUser request = approve(true, RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD));
        request.setRoomIds(List.of());

        assertThatThrownBy(() -> userService.approveUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("room");
    }

    @Test
    void approvingAsAdminGrantsEveryPermission() {
        UserEntity ana = pendingUser();
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.approveUser(approve(true, RoleEnum.ROLE_ADMIN, List.of(PermissionEnum.values())));

        assertThat(ana.getRole()).isEqualTo(RoleEnum.ROLE_ADMIN);
        assertThat(permissionsOf(ana)).containsExactlyInAnyOrder(PermissionEnum.values());
        assertThat(ana.getRooms()).isEmpty();
    }

    @Test
    void approvedAccountIsNotApprovedTwice() {
        when(repository.findById(7)).thenReturn(Optional.of(approvedUser(RoleEnum.ROLE_KORISNIK)));

        assertThatThrownBy(() -> userService.approveUser(approve(true, RoleEnum.ROLE_KORISNIK, List.of())))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectedUserGetsNoRoleAndARejectionEmail() {
        UserEntity ana = pendingUser();
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.approveUser(approve(false, RoleEnum.ROLE_ADMIN, List.of()));

        assertThat(ana.getIsApproved()).isEqualTo((byte) 0);
        assertThat(ana.getRole()).isNull();
        verify(emailService).sendEmail(eq("ana@example.com"), eq("ana"), anyString(), contains("odbijen"));
        verify(securityEvents).record(eq(SecurityEvent.USER_REJECTED), anyString());
    }

    @Test
    void rejectedRegistrationCanBeApprovedLater() {
        UserEntity ana = pendingUser();
        ana.setIsApproved((byte) 0);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.approveUser(approve(true, RoleEnum.ROLE_KORISNIK, List.of()));

        assertThat(ana.getIsApproved()).isEqualTo((byte) 1);
    }

    // ── blocking access ────────────────────────────────────────────

    @Test
    void blockingEndsTheSessionAndEmailsTheUser() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_KORISNIK);
        ana.setJWT("jwt");
        ana.setTwoFactorToken("code");
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.setAccess(7, false, "admin");

        assertThat(ana.getIsApproved()).isEqualTo((byte) 0);
        assertThat(ana.isEnabled()).isFalse();
        assertThat(ana.getJWT()).isNull();
        assertThat(ana.getTwoFactorToken()).isNull();
        assertThat(ana.getRole()).isEqualTo(RoleEnum.ROLE_KORISNIK);
        verify(emailService).sendEmail(eq("ana@example.com"), eq("ana"), anyString(), contains("zabranjen"));
        verify(securityEvents).record(eq(SecurityEvent.USER_BLOCKED), anyString());
    }

    @Test
    void unblockingRestoresTheSameRole() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD);
        ana.setIsApproved((byte) 0);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.setAccess(7, true, "admin");

        assertThat(ana.isEnabled()).isTrue();
        assertThat(ana.getRole()).isEqualTo(RoleEnum.ROLE_MODERATOR);
        assertThat(permissionsOf(ana)).containsExactly(PermissionEnum.ADD);
        verify(securityEvents).record(eq(SecurityEvent.USER_UNBLOCKED), anyString());
    }

    @Test
    void pendingRegistrationIsNotBlockedButRejected() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> userService.setAccess(7, false, "admin")).isInstanceOf(ConflictException.class);
    }

    @Test
    void adminCannotBlockThemselves() {
        UserEntity admin = approvedUser(RoleEnum.ROLE_ADMIN, PermissionEnum.values());
        admin.setUsername("admin");
        when(repository.findById(7)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.setAccess(7, false, "admin")).isInstanceOf(ConflictException.class);
    }

    @Test
    void settingTheSameAccessTwiceChangesNothing() {
        when(repository.findById(7)).thenReturn(Optional.of(approvedUser(RoleEnum.ROLE_KORISNIK)));

        userService.setAccess(7, true, "admin");

        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(emailService, securityEvents);
    }

    // ── role change ────────────────────────────────────────────────

    @Test
    void blockedOrPendingUserCannotChangeRole() {
        when(repository.findById(7)).thenReturn(Optional.of(pendingUser()));

        assertThatThrownBy(() -> userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD), null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void permissionsBecomeExactlyTheRequestedSet() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD, PermissionEnum.UPDATE, PermissionEnum.DELETE);
        ana.getRooms().add(science);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD), null));

        assertThat(permissionsOf(ana)).containsExactly(PermissionEnum.ADD);
    }

    @Test
    void adminDemotedToModeratorKeepsOnlyTheGivenPermissions() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_ADMIN, PermissionEnum.values());
        ana.getRooms().add(science);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.UPDATE), null));

        assertThat(ana.getRole()).isEqualTo(RoleEnum.ROLE_MODERATOR);
        assertThat(permissionsOf(ana)).containsExactly(PermissionEnum.UPDATE);
    }

    @Test
    void demotionToRegularUserRemovesAllPermissions() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.changeRole(changeRole(RoleEnum.ROLE_KORISNIK, List.of(), null));

        assertThat(ana.getRole()).isEqualTo(RoleEnum.ROLE_KORISNIK);
        assertThat(ana.getPermissions()).isEmpty();
        verify(securityEvents).record(eq(SecurityEvent.ROLE_CHANGED), anyString());
    }

    @Test
    void promotionAddsOnlyMissingPermissions() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_KORISNIK);
        PermissionEntity existing = new PermissionEntity(PermissionEnum.ADD, ana);
        ana.getPermissions().add(existing);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.changeRole(changeRole(RoleEnum.ROLE_ADMIN, List.of(PermissionEnum.values()), null));

        assertThat(permissionsOf(ana)).containsExactlyInAnyOrder(PermissionEnum.values());
        assertThat(ana.getPermissions()).contains(existing).hasSize(3);
    }

    @Test
    void roomsCanBeReassigned() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD);
        ana.getRooms().add(science);
        when(repository.findById(7)).thenReturn(Optional.of(ana));
        when(forumRoomEntityRepository.findAllById(List.of(2))).thenReturn(List.of(music));

        userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD), List.of(2)));

        assertThat(ana.getRooms()).containsExactly(music);
    }

    @Test
    void promotedModeratorWithoutRoomsIsRejected() {
        when(repository.findById(7)).thenReturn(Optional.of(approvedUser(RoleEnum.ROLE_KORISNIK)));

        assertThatThrownBy(() -> userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD), null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void roomsStayWhenNotSent() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD);
        ana.getRooms().add(science);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD), null));

        assertThat(ana.getRooms()).containsExactly(science);
        verifyNoInteractions(forumRoomEntityRepository);
    }

    @Test
    void unknownRoomIsRejected() {
        when(repository.findById(7)).thenReturn(Optional.of(approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD)));
        when(forumRoomEntityRepository.findAllById(List.of(2, 99))).thenReturn(List.of(music));

        assertThatThrownBy(() -> userService.changeRole(changeRole(RoleEnum.ROLE_MODERATOR, List.of(PermissionEnum.ADD), List.of(2, 99))))
                .isInstanceOf(NotFoundException.class);
    }

    // ── other ──────────────────────────────────────────────────────

    @Test
    void outputCarriesStatusAndRooms() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD);
        ana.getRooms().add(music);
        ana.getRooms().add(science);
        when(repository.getAllByIsApproved((byte) 1)).thenReturn(List.of(ana));

        UserOutput output = userService.getAllUsersByIsApproved((byte) 1).get(0);

        assertThat(output.getIsApproved()).isEqualTo((byte) 1);
        assertThat(output.getRoomIds()).containsExactly(1, 2);
    }

    @Test
    void deletingUnknownUserFails() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99)).isInstanceOf(NotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void deletingUserRemovesTheirCommentsFirst() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_KORISNIK);
        when(repository.findById(7)).thenReturn(Optional.of(ana));

        userService.deleteUser(7);

        var order = inOrder(commentEntityRepository, repository);
        order.verify(commentEntityRepository).deleteAllByUser_IdUser(7);
        order.verify(repository).delete(ana);
        verify(securityEvents).record(eq(SecurityEvent.USER_DELETED), anyString());
    }

    @Test
    void updateOnlyChangesProvidedFields() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_KORISNIK);
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(ana));

        userService.updateUser("ana", UserInput.builder().email("new@example.com").build());

        assertThat(ana.getUsername()).isEqualTo("ana");
        assertThat(ana.getPassword()).isEqualTo("hash");
        assertThat(ana.getEmail()).isEqualTo("new@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deleteJwtRevokesTheSession() {
        UserEntity ana = approvedUser(RoleEnum.ROLE_KORISNIK);
        ana.setJWT("jwt");
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(ana));

        userService.deleteJWT("ana");

        assertThat(ana.getJWT()).isNull();
        verify(repository).saveAndFlush(ana);
    }
}
