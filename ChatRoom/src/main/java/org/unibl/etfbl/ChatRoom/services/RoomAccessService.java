package org.unibl.etfbl.ChatRoom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;

/**
 * Permissions apply per room: an administrator moderates every room,
 * a moderator only the rooms an administrator assigned to them.
 */
@Service
public class RoomAccessService {

    @Autowired
    private UserEntityRepository userRepository;

    public void requireModerator(UserEntity actor, Integer idForumRoom) {
        if (actor.getRole() == RoleEnum.ROLE_ADMIN) {
            return;
        }
        if (actor.getRole() == RoleEnum.ROLE_MODERATOR
                && userRepository.existsByIdUserAndRooms_IdForumRoom(actor.getIdUser(), idForumRoom)) {
            return;
        }
        throw new AccessDeniedException("Not a moderator of room " + idForumRoom);
    }
}
