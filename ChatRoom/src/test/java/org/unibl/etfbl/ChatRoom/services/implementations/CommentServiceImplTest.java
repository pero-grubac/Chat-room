package org.unibl.etfbl.ChatRoom.services.implementations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.unibl.etfbl.ChatRoom.exceptions.BadRequestException;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.services.RoomAccessService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentEntityRepository commentEntityRepository;
    @Mock
    private ForumRoomEntityRepository forumRoomEntityRepository;
    @Mock
    private UserService userService;
    @Mock
    private RoomAccessService roomAccessService;
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CommentServiceImpl commentService;

    private final UserEntity moderator = new UserEntity();
    private final ForumRoomEntity science = ForumRoomEntity.builder().idForumRoom(1).name("Science").build();

    private static CommentInput input(Integer idComment, String text, Integer idForumRoom) {
        CommentInput input = new CommentInput();
        input.setIdComment(idComment);
        input.setText(text);
        input.setIdForumRoom(idForumRoom);
        return input;
    }

    private CommentEntity pendingComment() {
        CommentEntity comment = new CommentEntity();
        comment.setIdComment(5);
        comment.setForumRoom(science);
        return comment;
    }

    @Test
    void newCommentIsLinkedToAuthorAndRoomAndAwaitsModeration() {
        UserEntity ana = new UserEntity();
        when(userService.findByUsername("ana")).thenReturn(ana);
        when(forumRoomEntityRepository.findById(1)).thenReturn(Optional.of(science));

        commentService.createComment("ana", input(null, "Hello", 1));

        ArgumentCaptor<CommentEntity> saved = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentEntityRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getText()).isEqualTo("Hello");
        assertThat(saved.getValue().getUser()).isSameAs(ana);
        assertThat(saved.getValue().getForumRoom()).isSameAs(science);
        assertThat(saved.getValue().getCreatedAt()).isNotNull();
        assertThat(saved.getValue().getIsAllowed()).isNull();
    }

    @Test
    void commentInMissingRoomFails() {
        when(forumRoomEntityRepository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment("ana", input(null, "Hi", 42)))
                .isInstanceOf(NotFoundException.class);
        verify(commentEntityRepository, never()).saveAndFlush(any());
    }

    @Test
    void publicFeedShowsOnlyTheLatestApprovedComments() {
        when(commentEntityRepository.findFirst20ByForumRoom_IdForumRoomAndIsAllowedOrderByCreatedAtDesc(1, (byte) 1))
                .thenReturn(List.of(new CommentEntity()));

        assertThat(commentService.getCommentsInForumRoom(1, (byte) 1)).hasSize(1);
    }

    @Test
    void moderationQueueChecksTheRoomFirst() {
        doThrow(new AccessDeniedException("no")).when(roomAccessService).requireModerator(moderator, 1);

        assertThatThrownBy(() -> commentService.getRequestedComments(moderator, 1)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(commentEntityRepository);
    }

    @Test
    void approvingPublishesAPendingComment() {
        CommentEntity comment = pendingComment();
        when(commentEntityRepository.findById(5)).thenReturn(Optional.of(comment));

        commentService.allowComment(moderator, 5, (byte) 1);

        verify(roomAccessService).requireModerator(moderator, 1);
        assertThat(comment.getIsAllowed()).isEqualTo((byte) 1);
    }

    @Test
    void rejectingHidesAPendingCommentForGood() {
        CommentEntity comment = pendingComment();
        when(commentEntityRepository.findById(5)).thenReturn(Optional.of(comment));

        commentService.allowComment(moderator, 5, (byte) 0);

        assertThat(comment.getIsAllowed()).isEqualTo((byte) 0);
    }

    @Test
    void decisionCanOnlyBeZeroOrOne() {
        assertThatThrownBy(() -> commentService.allowComment(moderator, 5, (byte) 7)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> commentService.allowComment(moderator, 5, null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void moderatedCommentCannotBeModeratedAgain() {
        CommentEntity comment = pendingComment();
        comment.setIsAllowed((byte) 0);
        when(commentEntityRepository.findById(5)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.allowComment(moderator, 5, (byte) 1)).isInstanceOf(ConflictException.class);
    }

    @Test
    void moderatorOfAnotherRoomCannotTouchTheComment() {
        when(commentEntityRepository.findById(5)).thenReturn(Optional.of(pendingComment()));
        doThrow(new AccessDeniedException("no")).when(roomAccessService).requireModerator(moderator, 1);

        assertThatThrownBy(() -> commentService.deleteComment(moderator, 5)).isInstanceOf(AccessDeniedException.class);
        verify(commentEntityRepository, never()).delete(any());
    }

    @Test
    void unknownCommentIsNotFound() {
        when(commentEntityRepository.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.allowComment(moderator, 5, (byte) 1)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> commentService.updateComment(moderator, input(5, "x", 1))).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> commentService.deleteComment(moderator, 5)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deletingRemovesTheComment() {
        CommentEntity comment = pendingComment();
        when(commentEntityRepository.findById(5)).thenReturn(Optional.of(comment));

        commentService.deleteComment(moderator, 5);

        verify(commentEntityRepository).delete(comment);
    }

    @Test
    void moderatorEditKeepsAuthorRoomAndDate() {
        UserEntity author = new UserEntity();
        Timestamp written = Timestamp.valueOf("2026-01-01 10:00:00");
        CommentEntity comment = pendingComment();
        comment.setUser(author);
        comment.setCreatedAt(written);
        comment.setText("Helo");
        when(commentEntityRepository.findById(5)).thenReturn(Optional.of(comment));

        commentService.updateComment(moderator, input(5, "Hello", 2));

        assertThat(comment.getText()).isEqualTo("Hello");
        assertThat(comment.getUser()).isSameAs(author);
        assertThat(comment.getForumRoom()).isSameAs(science);
        assertThat(comment.getCreatedAt()).isEqualTo(written);
    }
}
