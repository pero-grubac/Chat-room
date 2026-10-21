package org.unibl.etfbl.ChatRoom.services;

import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.ApproveUser;
import org.unibl.etfbl.ChatRoom.models.dtos.ChangeRole;
import org.unibl.etfbl.ChatRoom.models.dtos.UserInput;
import org.unibl.etfbl.ChatRoom.models.dtos.UserOutput;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;

import java.util.List;

public interface UserService {
    List<UserOutput> getAllUsers();

    List<UserOutput> getAllUsersByIsApproved(Byte isApproved);

    UserOutput createUser(UserInput user);

    UserOutput updateUser(String username, UserInput user);

    void deleteUser(Integer id);

    void approveUser(ApproveUser approveUser) throws NotFoundException, ConflictException;

    /** Blocks or restores access for an account that was approved before. */
    void setAccess(Integer id, boolean allowed, String actingUsername);

    void changeRole(ChangeRole changeRole) throws NotFoundException, ConflictException;

    UserEntity findByUsername(String username) throws NotFoundException;

    void deleteJWT(String username);

}
