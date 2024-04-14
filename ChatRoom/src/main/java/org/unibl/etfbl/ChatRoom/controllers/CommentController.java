package org.unibl.etfbl.ChatRoom.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.advices.ExceptionLoggingAdvice;
import org.unibl.etfbl.ChatRoom.models.dtos.ApproveComment;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentInput;
import org.unibl.etfbl.ChatRoom.models.dtos.CommentOutput;
import org.unibl.etfbl.ChatRoom.services.CommentService;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private ExceptionLoggingAdvice exceptionLoggingAdvice;
    @Autowired
    private ModelMapper modelMapper;

    // TODO posljednjih 20 a ne sve
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK')")
    public ResponseEntity<List<CommentOutput>> getAll(@PathVariable Integer id) {
        List<CommentOutput> comments = commentService.getCommentsInForumRoom(id, (byte) 1);
        return ResponseEntity.status(HttpStatus.OK).body(comments);
    }

    @GetMapping("/requests/{idForumRoom}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public List<CommentOutput> getAllRequests(@PathVariable Integer idForumRoom, Pageable pageable) {
        return commentService.getRequestedComments(idForumRoom, null);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK') ")
    public ResponseEntity<?> create(@AuthenticationPrincipal(expression = "username") String username, @RequestBody @Valid CommentInput comment, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            commentService.createComment(username, comment);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') and hasAuthority('UPDATE')")
    public ResponseEntity<?> update(@AuthenticationPrincipal(expression = "username") String username, @RequestBody @Valid CommentInput comment, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            commentService.updateComment(username, comment);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @PutMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') and hasAuthority('ADD')")
    public ResponseEntity<?> approve(@RequestBody @Valid ApproveComment comment, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            commentService.allowComment(comment.getId(), comment.getIsApproved());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') and hasAuthority('DELETE')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

}
