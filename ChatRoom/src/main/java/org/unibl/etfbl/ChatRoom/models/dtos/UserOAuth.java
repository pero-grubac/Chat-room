package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Builder;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
@Builder
public class UserOAuth {
    private Integer idUser;
    @BannedWordsConstraint(paramName = "username")
    private String username;
    @BannedWordsConstraint(paramName = "password")
    private String password;
    @BannedWordsConstraint(paramName = "email")
    private String email;
    @BannedWordsConstraint(paramName = "role")
    private RoleEnum role;
    @BannedWordsConstraint(paramName = "source")
    private RegristrationSourceEnum source;
}
