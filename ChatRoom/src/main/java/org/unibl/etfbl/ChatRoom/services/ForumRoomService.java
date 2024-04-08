package org.unibl.etfbl.ChatRoom.services;

import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.ForumRoom;

import java.util.List;

public interface ForumRoomService {
    List<ForumRoom> getAllForumRooms();
    ForumRoom getForumRoom(Integer id) throws NotFoundException;

    ForumRoom createForumRoom(ForumRoom forumRoom) throws ConflictException;

    void updateForumRoom(ForumRoom forumRoom) throws ConflictException;

    void deleteForumRoom(Integer idForumRoom) throws NotFoundException;
}
