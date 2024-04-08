package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;

import java.util.List;

@Repository
public interface CommentEntityRepository extends JpaRepository<CommentEntity, Integer> {

    List<CommentEntity> findAllByForumRoom_IdForumRoomAndIsAllowed(Integer idForumRoom, Byte isAllowed);
    List<CommentEntity> findAllByForumRoom_IdForumRoomAndIsAllowedOrderByCreatedAtDesc(Integer idForumRoom, Byte isAllowed, Pageable pageable);
    Boolean existsByIdComment(Integer idComment);
}
