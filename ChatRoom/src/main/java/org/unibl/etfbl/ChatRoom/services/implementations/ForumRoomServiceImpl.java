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
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.services.ForumRoomService;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ForumRoomServiceImpl implements ForumRoomService {

    @Autowired
    private ForumRoomEntityRepository repository;

    @Autowired
    private ModelMapper modelMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ForumRoom> getAllForumRooms() {
        List<ForumRoom> forumRooms = new java.util.ArrayList<>(repository.findAll().stream()
                .map(f -> modelMapper.map(f, ForumRoom.class))
                .toList());
        forumRooms.sort(Comparator.comparing(ForumRoom::getIdForumRoom));
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
            ForumRoomEntity forumRoomEntity = repository.findById(forumRoom.getIdForumRoom()).orElseThrow(
                    () -> new NotFoundException("Forum room with ID " + forumRoom.getIdForumRoom() + " not found")
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
            repository.deleteById(idForumRoom);
        }
    }
}
