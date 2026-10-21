package org.unibl.etfbl.ChatRoom.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.exceptions.BadRequestException;
import org.unibl.etfbl.ChatRoom.models.dtos.ApproveUser;
import org.unibl.etfbl.ChatRoom.models.dtos.ChangeRole;
import org.unibl.etfbl.ChatRoom.models.dtos.UserAccess;
import org.unibl.etfbl.ChatRoom.models.dtos.UserInput;
import org.unibl.etfbl.ChatRoom.models.dtos.UserOutput;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('ADD')")
    public List<UserOutput> getAll() {
        return userService.getAllUsers();
    }

    // isapproved 0/1
    @GetMapping("/approved/{isApproved}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserOutput> getAll(@PathVariable Byte isApproved) {
        if (isApproved != 1 && isApproved != 0) {
            throw new BadRequestException("isApproved must be 0 or 1");
        }
        return userService.getAllUsersByIsApproved(isApproved);
    }

    //isapproved = null
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserOutput> getAllRequests() {
        return userService.getAllUsersByIsApproved(null);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addUser(@RequestBody @Valid UserInput user) {
        userService.createUser(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','KORISNIK')")
    public ResponseEntity<Void> update(@AuthenticationPrincipal(expression = "username") String username, @RequestBody @Valid UserInput user) {
        userService.updateUser(username, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approve(@RequestBody @Valid ApproveUser approveUser) {
        userService.approveUser(approveUser);
        return ResponseEntity.ok().build();
    }

    /** Blocks or restores access for an approved account; a blocked user is signed out at once. */
    @PutMapping("/access")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> access(@AuthenticationPrincipal(expression = "username") String admin, @RequestBody UserAccess access) {
        userService.setAccess(access.getId(), access.isAllowed(), admin);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/changeRole")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeRole(@RequestBody @Valid ChangeRole changeRole) {
        userService.changeRole(changeRole);
        return ResponseEntity.ok().build();
    }
}
