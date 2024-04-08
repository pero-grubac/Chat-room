package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean isAuthenticated;
    @BannedWordsConstraint(paramName = "token")
    private String token;
}
