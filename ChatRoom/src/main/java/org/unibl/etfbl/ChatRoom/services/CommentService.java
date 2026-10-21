package org.unibl.etfbl.ChatRoom.services;

import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;

import java.util.List;

/** Comments; every moderation method checks that {@code moderator} may moderate the comment's room. */
public interface CommentService {
    List<CommentOutput> getCommentsInForumRoom(Integer idForumRoom, Byte isAllowed);

    List<CommentOutput> getRequestedComments(UserEntity moderator, Integer idForumRoom);

    void createComment(String username, CommentInput comment) throws NotFoundException;

    void updateComment(UserEntity moderator, CommentInput comment) throws NotFoundException;

    void deleteComment(UserEntity moderator, Integer idComment) throws NotFoundException;

    void allowComment(UserEntity moderator, Integer idComment, Byte isAllowed) throws NotFoundException;
}
