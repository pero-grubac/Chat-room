package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;

/** The Google access token from the browser; the server checks it with Google. */
@Data
public class OAuthLoginRequest {
    private String accessToken;
}
