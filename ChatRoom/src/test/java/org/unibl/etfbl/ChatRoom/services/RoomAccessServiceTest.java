package org.unibl.etfbl.ChatRoom.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomAccessServiceTest {

    @Mock
    private UserEntityRepository userRepository;

    @InjectMocks
    private RoomAccessService roomAccessService;

    private static UserEntity user(RoleEnum role) {
        UserEntity user = new UserEntity();
        user.setIdUser(7);
        user.setRole(role);
        return user;
    }

    @Test
    void adminModeratesEveryRoom() {
        assertThatCode(() -> roomAccessService.requireModerator(user(RoleEnum.ROLE_ADMIN), 3)).doesNotThrowAnyException();
        verifyNoInteractions(userRepository);
    }

    @Test
    void moderatorModeratesTheirOwnRooms() {
        when(userRepository.existsByIdUserAndRooms_IdForumRoom(7, 3)).thenReturn(true);

        assertThatCode(() -> roomAccessService.requireModerator(user(RoleEnum.ROLE_MODERATOR), 3)).doesNotThrowAnyException();
    }

    @Test
    void moderatorCannotModerateOtherRooms() {
        when(userRepository.existsByIdUserAndRooms_IdForumRoom(7, 4)).thenReturn(false);

        assertThatThrownBy(() -> roomAccessService.requireModerator(user(RoleEnum.ROLE_MODERATOR), 4))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void regularUserModeratesNothing() {
        assertThatThrownBy(() -> roomAccessService.requireModerator(user(RoleEnum.ROLE_KORISNIK), 3))
                .isInstanceOf(AccessDeniedException.class);
    }
}
