package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;

@Repository
public interface ForumRoomEntityRepository extends JpaRepository<ForumRoomEntity, Integer> {
    Boolean existsByName(String name);
    Boolean existsByIdForumRoom(Integer idForumRoom);
}
