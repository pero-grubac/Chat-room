package org.unibl.etfbl.ChatRoom.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.advices.ExceptionLoggingAdvice;
import org.unibl.etfbl.ChatRoom.models.dtos.ForumRoom;
import org.unibl.etfbl.ChatRoom.services.ForumRoomService;

import java.util.List;

@RestController
@RequestMapping("/forumrooms")
public class ForumRoomController {
    @Autowired
    private ForumRoomService forumRoomService;
    @Autowired
    private ExceptionLoggingAdvice exceptionLoggingAdvice;

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


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<?> addForumRoom(@RequestBody @Valid ForumRoom forumRoom, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        ForumRoom room = forumRoomService.createForumRoom(forumRoom);
        if (room != null) {
            return new ResponseEntity<>(room, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<?> update(@RequestBody @Valid ForumRoom forumRoom, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            forumRoomService.updateForumRoom(forumRoom);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            forumRoomService.deleteForumRoom(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
}
