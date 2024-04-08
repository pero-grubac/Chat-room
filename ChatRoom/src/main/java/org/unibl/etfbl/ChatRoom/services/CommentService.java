package org.unibl.etfbl.ChatRoom.services;

import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;

import java.util.List;

public interface CommentService {
    List<CommentOutput> getCommentsInForumRoom(Integer idForumRoom, Byte isAllowed);
    List<CommentOutput> getLastComments(Integer idForumRoom, Byte isAllowed,Integer pageSize);
    void createComment(CommentInput comment) throws NotFoundException, ConflictException;

    void updateComment(CommentInput comment) throws NotFoundException, ConflictException;

    void deleteComment(Integer idComment);
    void allowComment(Integer idComment,Boolean isAllowed) throws NotFoundException, ConflictException;
}
