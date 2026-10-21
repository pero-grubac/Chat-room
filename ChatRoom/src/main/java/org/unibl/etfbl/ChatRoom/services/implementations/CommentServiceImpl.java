package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.exceptions.BadRequestException;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.services.CommentService;
import org.unibl.etfbl.ChatRoom.services.RoomAccessService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentEntityRepository commentEntityRepository;
    @Autowired
    private ForumRoomEntityRepository forumRoomEntityRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RoomAccessService roomAccessService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CommentOutput> getCommentsInForumRoom(Integer idForumRoom, Byte isAllowed) {
        return commentEntityRepository.findFirst20ByForumRoom_IdForumRoomAndIsAllowedOrderByCreatedAtDesc(idForumRoom, isAllowed)
                .stream().map(c -> modelMapper.map(c, CommentOutput.class)).toList();
    }

    @Override
    public List<CommentOutput> getRequestedComments(UserEntity moderator, Integer idForumRoom) {
        roomAccessService.requireModerator(moderator, idForumRoom);
        return commentEntityRepository.findAllByForumRoom_IdForumRoomAndIsAllowed(idForumRoom, null)
                .stream().map(c -> modelMapper.map(c, CommentOutput.class)).toList();
    }

    @Override
    public void createComment(String username, CommentInput comment) throws NotFoundException {
        ForumRoomEntity room = forumRoomEntityRepository.findById(comment.getIdForumRoom()).orElseThrow(
                () -> new NotFoundException("Forum room with id: " + comment.getIdForumRoom() + " doesn't exists."));
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setUser(userService.findByUsername(username));
        commentEntity.setForumRoom(room);
        commentEntity.setText(comment.getText());
        commentEntity.setCreatedAt(Timestamp.from(Instant.now()));
        commentEntityRepository.saveAndFlush(commentEntity);
    }

    @Override
    public void updateComment(UserEntity moderator, CommentInput comment) throws NotFoundException {
        CommentEntity commentEntity = findModerated(moderator, comment.getIdComment());
        // A moderator's edit changes the text only: author, room and date stay as they were
        commentEntity.setText(comment.getText());
        commentEntityRepository.saveAndFlush(commentEntity);
    }

    @Override
    public void deleteComment(UserEntity moderator, Integer idComment) throws NotFoundException {
        commentEntityRepository.delete(findModerated(moderator, idComment));
    }

    @Override
    public void allowComment(UserEntity moderator, Integer idComment, Byte isAllowed) throws NotFoundException {
        if (isAllowed == null || (isAllowed != 0 && isAllowed != 1)) {
            throw new BadRequestException("isApproved must be 0 (reject) or 1 (publish)");
        }
        CommentEntity commentEntity = findModerated(moderator, idComment);
        if (commentEntity.getIsAllowed() != null) {
            throw new ConflictException("Comment with ID " + idComment + " was already moderated");
        }
        commentEntity.setIsAllowed(isAllowed);
        commentEntityRepository.saveAndFlush(commentEntity);
    }

    private CommentEntity findModerated(UserEntity moderator, Integer idComment) {
        CommentEntity commentEntity = commentEntityRepository.findById(idComment).orElseThrow(
                () -> new NotFoundException("Comment with ID " + idComment + " not found"));
        roomAccessService.requireModerator(moderator, commentEntity.getForumRoom().getIdForumRoom());
        return commentEntity;
    }
}
