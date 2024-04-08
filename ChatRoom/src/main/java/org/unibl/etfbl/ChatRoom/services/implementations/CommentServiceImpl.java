package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
        return commentEntityRepository.findAllByForumRoom_IdForumRoomAndIsAllowed(idForumRoom, isAllowed).stream().map(c -> modelMapper.map(c, CommentOutput.class)).toList();
    }

    @Override
    public List<CommentOutput> getLastComments(Integer idForumRoom, Byte isAllowed, Integer pageSize) {
        Pageable pageable = PageRequest.of(0,pageSize);
        return commentEntityRepository.findAllByForumRoom_IdForumRoomAndIsAllowedOrderByCreatedAtDesc(idForumRoom, isAllowed,pageable).stream().map(c -> modelMapper.map(c, CommentOutput.class)).toList();
    }

    private void setUserAndForumRoomForComment(CommentInput comment, CommentEntity commentEntity) throws NotFoundException {
        UserOutput userEntity = userService.getUser(comment.getIdUser());
        ForumRoom forumRoom = forumRoomService.getForumRoom(comment.getIdForumRoom());
        commentEntity.setUser(modelMapper.map(userEntity, UserEntity.class));
        commentEntity.setText(comment.getText());
        commentEntity.setForumRoom(modelMapper.map(forumRoom, ForumRoomEntity.class));
        commentEntity.setCreatedAt(Timestamp.from(Instant.now()));
    }

    @Override
    public void createComment(CommentInput comment) throws NotFoundException, ConflictException {
        CommentEntity commentEntity = new CommentEntity();
        setUserAndForumRoomForComment(comment, commentEntity);
        commentEntityRepository.saveAndFlush(commentEntity);
        entityManager.refresh(commentEntity);
    }


    @Override
    public void updateComment(CommentInput comment) throws NotFoundException, ConflictException {
        CommentEntity commentEntity = commentEntityRepository.findById(comment.getIdComment()).orElseThrow(
                () -> new NotFoundException("Comment with ID " + comment.getIdComment() + " not found")
        );
        if (commentEntity.getIsAllowed() == null || commentEntity.getIsAllowed() == 0)
            throw new ConflictException("Invalid data");
        setUserAndForumRoomForComment(comment, commentEntity);
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
    public void allowComment(Integer idComment, Boolean isAllowed) throws NotFoundException, ConflictException {
        CommentEntity commentEntity = commentEntityRepository.findById(idComment).orElseThrow(
                () -> new NotFoundException("Comment with ID " + idComment + " not found")
        );
        if (commentEntity.getIsAllowed() != null && commentEntity.getIsAllowed() == 1) {
            throw new ConflictException("Invalid data");
        }
        commentEntity.setIsAllowed((byte) (isAllowed ? 1 : 0));
        commentEntityRepository.saveAndFlush(commentEntity);
        entityManager.refresh(commentEntity);
    }
}
