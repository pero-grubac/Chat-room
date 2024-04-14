package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;
import org.unibl.etfbl.ChatRoom.models.dtos.ForumRoom;
import org.unibl.etfbl.ChatRoom.models.dtos.UserOutput;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.CommentPageRepository;
import org.unibl.etfbl.ChatRoom.services.CommentService;
import org.unibl.etfbl.ChatRoom.services.ForumRoomService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentEntityRepository commentEntityRepository;
    @Autowired
    private CommentPageRepository commentPageRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ForumRoomService forumRoomService;

    @Autowired
    private ModelMapper modelMapper;

    // Osvježivanje verzije entiteta
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<CommentOutput> getCommentsInForumRoom(Integer idForumRoom, Byte isAllowed) {
        return commentEntityRepository.findFirst20ByForumRoom_IdForumRoomAndIsAllowedOrderByCreatedAtDesc(idForumRoom, isAllowed).stream().map(c -> modelMapper.map(c, CommentOutput.class)).toList();
    }

    @Override
    public List<CommentEntity> getCommentsByIdForumRoom(Integer idForumRoom) {
        return commentEntityRepository.findAllByForumRoom_IdForumRoom(idForumRoom);
    }

    @Override
    public List<CommentOutput> getRequestedComments(Integer idForumRoom, Byte isAllowed) {
        return commentEntityRepository.findAllByForumRoom_IdForumRoomAndIsAllowed(idForumRoom, isAllowed).stream().map(c -> modelMapper.map(c, CommentOutput.class)).toList();

    }

    private void setUserAndForumRoomForComment(String username, CommentInput comment, CommentEntity commentEntity) throws NotFoundException {

        UserEntity userEntity = userService.findByUsername(username);
        ForumRoom forumRoom = forumRoomService.getForumRoom(comment.getIdForumRoom());
        commentEntity.setUser(userEntity);
        commentEntity.setText(comment.getText());
        commentEntity.setForumRoom(modelMapper.map(forumRoom, ForumRoomEntity.class));
        commentEntity.setCreatedAt(Timestamp.from(Instant.now()));
    }

    @Override
    public void createComment(String username, CommentInput comment) throws NotFoundException, ConflictException {
        CommentEntity commentEntity = new CommentEntity();
        setUserAndForumRoomForComment(username, comment, commentEntity);
        commentEntityRepository.saveAndFlush(commentEntity);
        entityManager.refresh(commentEntity);
    }


    @Override
    public void updateComment(String username, CommentInput comment) throws NotFoundException, ConflictException {
        CommentEntity commentEntity = commentEntityRepository.findById(comment.getIdComment()).orElseThrow(
                () -> new NotFoundException("Comment with ID " + comment.getIdComment() + " not found")
        );

        setUserAndForumRoomForComment(username, comment, commentEntity);
        commentEntityRepository.saveAndFlush(commentEntity);
        entityManager.refresh(commentEntity);

    }

    @Override
    public void deleteComment(Integer idComment) throws ConflictException {
        if (!commentEntityRepository.existsByIdComment(idComment)) {
            throw new ConflictException("Comment with ID " + idComment + " not found");
        } else {
            commentEntityRepository.deleteById(idComment);
        }
    }

    @Override
    public void allowComment(Integer idComment, Byte isAllowed) throws NotFoundException, ConflictException {
        CommentEntity commentEntity = commentEntityRepository.findById(idComment).orElseThrow(
                () -> new NotFoundException("Comment with ID " + idComment + " not found")
        );
        if (commentEntity.getIsAllowed() != null && commentEntity.getIsAllowed() == 1) {
            throw new ConflictException("Invalid data");
        }
        commentEntity.setIsAllowed(isAllowed);
        commentEntityRepository.saveAndFlush(commentEntity);
        entityManager.refresh(commentEntity);
    }
}
