package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

import java.util.List;

@Data
@NoArgsConstructor
public class UserOutput {
    private Integer idUser;
    @BannedWordsConstraint(paramName = "username")
    private String username;
    @BannedWordsConstraint(paramName = "email")
    private String email;
    @BannedWordsConstraint(paramName = "role")
    private RoleEnum role;
    @BannedWordsConstraint(paramName = "permissions")
    private List<PermissionEntity> permissions;


}
