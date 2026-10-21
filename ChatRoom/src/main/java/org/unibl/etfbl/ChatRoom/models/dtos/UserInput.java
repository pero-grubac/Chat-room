package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Builder;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

/** Registration and profile data. There is no id or role: the server assigns both. */
@Data
@Builder
public class UserInput {
    @BannedWordsConstraint(paramName = "username")
    private String username;
    @BannedWordsConstraint(paramName = "password")
    private String password;
    @BannedWordsConstraint(paramName = "email")
    private String email;
}
