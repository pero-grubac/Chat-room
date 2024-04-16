package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.ForumRoom;
import org.unibl.etfbl.ChatRoom.models.entities.CommentEntity;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.services.CommentService;
import org.unibl.etfbl.ChatRoom.services.ForumRoomService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ForumRoomServiceImpl implements ForumRoomService {

    @Autowired
    private ForumRoomEntityRepository repository;
    @Autowired
    private CommentEntityRepository commentEntityRepository;
    @Autowired
    private UserEntityRepository userEntityRepository;
    @Autowired
    private ModelMapper modelMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ForumRoom> getAllForumRooms() {
        List<ForumRoom> forumRooms = new java.util.ArrayList<>(repository.findAll().stream()
                .map(f -> modelMapper.map(f, ForumRoom.class))
                .toList());
        forumRooms.sort(Comparator.comparing(ForumRoom::getId));
        return forumRooms;
    }

    @Override
    public ForumRoom getForumRoom(Integer id) throws NotFoundException {
        return modelMapper.map(repository.findById(id).orElseThrow(
                () -> new NotFoundException("Forum room with id: " + id + " doesn't exists.")
        ), ForumRoom.class);
    }

    @Override
    public ForumRoom createForumRoom(ForumRoom forumRoom) throws ConflictException {
        if (repository.existsByName(forumRoom.getName())) {
            throw new ConflictException("Forum room with name: " + forumRoom.getName() + " already exists.");
        } else {
            ForumRoomEntity forumRoomEntity = modelMapper.map(forumRoom, ForumRoomEntity.class);
            repository.saveAndFlush(forumRoomEntity);
            entityManager.refresh(forumRoomEntity);
            return modelMapper.map(forumRoomEntity, ForumRoom.class);
        }
    }

    @Override
    public void updateForumRoom(ForumRoom forumRoom) throws ConflictException {
        if (repository.existsByName(forumRoom.getName())) {
            throw new ConflictException("Forum room with name: " + forumRoom.getName() + " already exists.");
        } else {
            ForumRoomEntity forumRoomEntity = repository.findById(forumRoom.getId()).orElseThrow(
                    () -> new NotFoundException("Forum room with ID " + forumRoom.getId() + " not found")
            );
            forumRoomEntity.setName(forumRoom.getName());
            repository.saveAndFlush(forumRoomEntity);
            entityManager.refresh(forumRoomEntity);
        }
    }

    @Override
    public void deleteForumRoom(Integer idForumRoom) throws NotFoundException {
        if (!repository.existsByIdForumRoom(idForumRoom)) {
            throw new NotFoundException("Forum room with id: " + idForumRoom + " doesn't exists.");
        } else {
            List<CommentEntity> comments = commentEntityRepository.findAllByForumRoom_IdForumRoom(idForumRoom);
            for (CommentEntity c : comments)
                commentEntityRepository.deleteById(c.getIdComment());
            List<UserEntity> users = userEntityRepository.getAllByRooms_IdForumRoom(idForumRoom);
            for(UserEntity u : users){
                Set<ForumRoomEntity> rooms = u.getRooms();
                rooms.removeIf(r->r.getIdForumRoom().equals(idForumRoom));
                userEntityRepository.saveAndFlush(u);
            }
            repository.deleteById(idForumRoom);
        }
    }
}
