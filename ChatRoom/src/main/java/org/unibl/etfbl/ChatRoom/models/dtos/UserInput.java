package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Builder;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

import java.util.List;

@Data
@Builder
public class UserInput {
    private Integer idUser;
    @BannedWordsConstraint(paramName = "username")
    private String username;
    @BannedWordsConstraint(paramName = "password")
    private String password;
    @BannedWordsConstraint(paramName = "email")
    private String email;
    private RoleEnum role;
}
