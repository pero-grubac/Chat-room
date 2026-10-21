package org.unibl.etfbl.ChatRoom.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.models.dtos.ApproveComment;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.services.CommentService;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /** The latest 20 approved comments of a room. Moderation endpoints below are limited to the moderator's rooms. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK')")
    public List<CommentOutput> getAll(@PathVariable Integer id) {
        return commentService.getCommentsInForumRoom(id, (byte) 1);
    }

    @GetMapping("/requests/{idForumRoom}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public List<CommentOutput> getAllRequests(@AuthenticationPrincipal UserEntity moderator, @PathVariable Integer idForumRoom) {
        return commentService.getRequestedComments(moderator, idForumRoom);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK') ")
    public ResponseEntity<Void> create(@AuthenticationPrincipal(expression = "username") String username, @RequestBody @Valid CommentInput comment) {
        commentService.createComment(username, comment);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') and hasAuthority('UPDATE')")
    public ResponseEntity<Void> update(@AuthenticationPrincipal UserEntity moderator, @RequestBody @Valid CommentInput comment) {
        commentService.updateComment(moderator, comment);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') and hasAuthority('ADD')")
    public ResponseEntity<Void> approve(@AuthenticationPrincipal UserEntity moderator, @RequestBody @Valid ApproveComment comment) {
        commentService.allowComment(moderator, comment.getId(), comment.getIsApproved());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') and hasAuthority('DELETE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserEntity moderator, @PathVariable Integer id) {
        commentService.deleteComment(moderator, id);
        return ResponseEntity.ok().build();
    }

}
