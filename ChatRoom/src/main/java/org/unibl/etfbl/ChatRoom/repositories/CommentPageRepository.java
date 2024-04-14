package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;

@Repository
public interface CommentPageRepository  extends PagingAndSortingRepository<CommentEntity,Integer> {
    Page<CommentEntity> findAllByForumRoom_IdForumRoomAndIsAllowedOrderByCreatedAtDesc(Integer idForumRoom, Byte isAllowed, Pageable pageable);

}
