package org.unibl.etfbl.ChatRoom.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.advices.ExceptionLoggingAdvice;
import org.unibl.etfbl.ChatRoom.models.dtos.ApproveUser;
import org.unibl.etfbl.ChatRoom.models.dtos.ChangeRole;
import org.unibl.etfbl.ChatRoom.models.dtos.UserInput;
import org.unibl.etfbl.ChatRoom.models.dtos.UserOutput;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ExceptionLoggingAdvice exceptionLoggingAdvice;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ADD')")
    public List<UserOutput> getAll() {
        return userService.getAllUsers();
    }

    // isapproved 0/1
    @GetMapping("/approved/{isApproved}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserOutput>> getAll(@PathVariable Byte isApproved) {
        if (isApproved == 1 || isApproved == 0) {
            List<UserOutput> users = userService.getAllUsersByIsApproved(isApproved);
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } else
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    //isapproved = null
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserOutput> getAllRequests() {
        return userService.getAllUsersByIsApproved(null);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody @Valid UserInput user, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            UserOutput newUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    // trazi po usernamu
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK')")
    public ResponseEntity<?> update(@AuthenticationPrincipal(expression = "username") String username, @RequestBody @Valid UserInput user, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            UserOutput newUser = userService.updateUser(username, user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @PutMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@RequestBody @Valid ApproveUser approveUser, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            userService.approveUser(approveUser);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/changeRole")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeRole(@RequestBody @Valid ChangeRole changeRole, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            userService.changeRole(changeRole);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
}
