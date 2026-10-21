package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.unibl.etfbl.ChatRoom.exceptions.BadRequestException;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.ForumRoom;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumRoomServiceImplTest {

    @Mock
    private ForumRoomEntityRepository repository;
    @Mock
    private CommentEntityRepository commentEntityRepository;
    @Mock
    private UserEntityRepository userEntityRepository;
    @Spy
    private ModelMapper modelMapper = new ModelMapper();
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ForumRoomServiceImpl forumRoomService;

    private static ForumRoom room(Integer id, String name) {
        ForumRoom room = new ForumRoom();
        room.setId(id);
        room.setName(name);
        return room;
    }

    private static ForumRoomEntity entity(Integer id, String name) {
        return ForumRoomEntity.builder().idForumRoom(id).name(name).build();
    }

    @Test
    void roomsAreReturnedSortedById() {
        when(repository.findAll()).thenReturn(List.of(entity(3, "Sports"), entity(1, "Science"), entity(2, "Culture")));

        assertThat(forumRoomService.getAllForumRooms()).extracting(ForumRoom::getName)
                .containsExactly("Science", "Culture", "Sports");
    }

    @Test
    void unknownRoomIsNotFound() {
        when(repository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumRoomService.getForumRoom(9)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void duplicateRoomNameIsRejected() {
        when(repository.existsByName("Music")).thenReturn(true);

        assertThatThrownBy(() -> forumRoomService.createForumRoom(room(null, "Music"))).isInstanceOf(ConflictException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void newRoomIsSaved() {
        when(repository.existsByName("Movies")).thenReturn(false);

        forumRoomService.createForumRoom(room(null, "Movies"));

        verify(repository).saveAndFlush(any(ForumRoomEntity.class));
    }

    @Test
    void renamingToAnExistingNameIsRejected() {
        when(repository.findById(1)).thenReturn(Optional.of(entity(1, "Science")));
        when(repository.existsByName("Music")).thenReturn(true);

        assertThatThrownBy(() -> forumRoomService.updateForumRoom(room(1, "Music"))).isInstanceOf(ConflictException.class);
    }

    @Test
    void keepingTheSameNameIsNotAConflict() {
        when(repository.findById(1)).thenReturn(Optional.of(entity(1, "Science")));

        forumRoomService.updateForumRoom(room(1, "Science"));

        verify(repository, never()).existsByName(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void renameWithoutAnIdIsABadRequest() {
        assertThatThrownBy(() -> forumRoomService.updateForumRoom(room(null, "Physics"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void renameChangesTheName() {
        ForumRoomEntity science = entity(1, "Science");
        when(repository.existsByName("Physics")).thenReturn(false);
        when(repository.findById(1)).thenReturn(Optional.of(science));

        forumRoomService.updateForumRoom(room(1, "Physics"));

        assertThat(science.getName()).isEqualTo("Physics");
    }

    @Test
    void deletingUnknownRoomFails() {
        when(repository.existsByIdForumRoom(9)).thenReturn(false);

        assertThatThrownBy(() -> forumRoomService.deleteForumRoom(9)).isInstanceOf(NotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deletingRoomRemovesItsCommentsAndMemberships() {
        ForumRoomEntity science = entity(1, "Science");
        CommentEntity comment = new CommentEntity();
        comment.setIdComment(11);
        UserEntity member = new UserEntity();
        Set<ForumRoomEntity> rooms = new HashSet<>(Set.of(science, entity(2, "Culture")));
        member.setRooms(rooms);

        when(repository.existsByIdForumRoom(1)).thenReturn(true);
        when(commentEntityRepository.findAllByForumRoom_IdForumRoom(1)).thenReturn(List.of(comment));
        when(userEntityRepository.getAllByRooms_IdForumRoom(1)).thenReturn(List.of(member));

        forumRoomService.deleteForumRoom(1);

        verify(commentEntityRepository).deleteById(11);
        assertThat(member.getRooms()).extracting(ForumRoomEntity::getName).containsExactly("Culture");
        verify(userEntityRepository).saveAndFlush(member);
        verify(repository).deleteById(1);
    }
}
