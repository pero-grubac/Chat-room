package org.unibl.etfbl.ChatRoom.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.models.dtos.ForumRoom;
import org.unibl.etfbl.ChatRoom.services.ForumRoomService;

import java.util.List;

@RestController
@RequestMapping("/forumrooms")
public class ForumRoomController {
    @Autowired
    private ForumRoomService forumRoomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK')")
    public List<ForumRoom> getAll() {
        return forumRoomService.getAllForumRooms();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK')")
    public ForumRoom getById(@PathVariable Integer id) {
        return forumRoomService.getForumRoom(id);
    }


    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ForumRoom addForumRoom(@RequestBody @Valid ForumRoom forumRoom) {
        return forumRoomService.createForumRoom(forumRoom);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Void> update(@RequestBody @Valid ForumRoom forumRoom) {
        forumRoomService.updateForumRoom(forumRoom);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        forumRoomService.deleteForumRoom(id);
        return ResponseEntity.ok().build();
    }
}
