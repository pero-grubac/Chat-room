package org.unibl.etfbl.ChatRoom.services;

import org.springframework.data.domain.Pageable;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;

import java.util.List;

public interface CommentService {
    List<CommentOutput> getCommentsInForumRoom(Integer idForumRoom, Byte isAllowed);
    List<CommentEntity> getCommentsByIdForumRoom(Integer idForumRoom);

    List<CommentOutput> getRequestedComments(Integer idForumRoom, Byte isAllowed);
    void createComment(String username,CommentInput comment) throws NotFoundException, ConflictException;

    void updateComment(String username,CommentInput comment) throws NotFoundException, ConflictException;

    void deleteComment(Integer idComment);
    void allowComment(Integer idComment,Byte isAllowed) throws NotFoundException, ConflictException;
}
