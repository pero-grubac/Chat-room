package org.unibl.etfbl.ChatRoom.security.auth.google;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Checks a Google OAuth2 access token with Google itself, instead of trusting a profile sent by the browser.
 * The token must have been issued to this application's client ID and belong to a verified email.
 */
@Service
public class GoogleTokenVerifier {

    public record GoogleProfile(String email, String name) {
    }

    static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?access_token={token}";
    static final String USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    private final RestClient restClient;

    @Value("${google.client-id:}")
    private String clientId;

    public GoogleTokenVerifier(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public GoogleProfile verify(String accessToken) {
        if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(clientId)) {
            throw new BadCredentialsException("Google sign-in is not available");
        }
        try {
            Map<?, ?> tokenInfo = restClient.get().uri(TOKEN_INFO_URL, accessToken).retrieve().body(Map.class);
            // A token issued to another application must not sign anyone in here
            if (tokenInfo == null || !clientId.equals(tokenInfo.get("aud"))) {
                throw new BadCredentialsException("Google token was not issued to this application");
            }
            Map<?, ?> userInfo = restClient.get().uri(USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve().body(Map.class);
            if (userInfo == null || !Boolean.TRUE.equals(userInfo.get("email_verified"))
                    || StringUtils.isAnyBlank((String) userInfo.get("email"), (String) userInfo.get("name"))) {
                throw new BadCredentialsException("Google account has no verified email");
            }
            return new GoogleProfile((String) userInfo.get("email"), (String) userInfo.get("name"));
        } catch (RestClientException e) {
            throw new BadCredentialsException("Invalid Google token");
        }
    }
}
