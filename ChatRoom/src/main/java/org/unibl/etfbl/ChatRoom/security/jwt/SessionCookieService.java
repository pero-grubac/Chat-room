package org.unibl.etfbl.ChatRoom.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * The session lives in an httpOnly, SameSite=Strict cookie: page scripts cannot read it,
 * and the browser does not send it with requests started by other sites.
 */
@Service
public class SessionCookieService {

    private static final String BEARER = "Bearer ";

    @Autowired
    private JwtService jwtService;

    @Value("${auth.cookie.name:CHATROOM_SESSION}")
    private String cookieName = "CHATROOM_SESSION";

    @Value("${auth.cookie.secure:true}")
    private boolean secure = true;

    public ResponseCookie create(String jwt) {
        return build(jwt, jwtService.getExpiration());
    }

    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    /** The JWT from the session cookie, or from an Authorization: Bearer header for API clients. */
    public Optional<String> resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.startsWith(header, BEARER) && header.length() > BEARER.length()) {
            return Optional.of(header.substring(BEARER.length()));
        }
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::isNotBlank)
                .findFirst();
    }

    public String getCookieName() {
        return cookieName;
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
