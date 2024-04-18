package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
public class OAuthTokenRequest {
    @BannedWordsConstraint(paramName = "email")
    private String email;
    @BannedWordsConstraint(paramName = "token")
    private String token;
    private String type;
}
