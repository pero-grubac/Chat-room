package org.unibl.etfbl.ChatRoom.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.unibl.etfbl.ChatRoom.security.jwt.JwtAuthenticationFilter;
import org.unibl.etfbl.ChatRoom.security.jwt.SessionCookieService;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.io.IOException;

/** API callers get JSON 401/403 instead of a redirect to a login page. */
@Component
public class JsonSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Autowired
    private SessionCookieService sessionCookieService;

    @Autowired
    private SecurityEventService securityEvents;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object reason = request.getAttribute(JwtAuthenticationFilter.REJECTION_REASON);
        if (reason != null) {
            // The presented session is dead; drop the cookie as well
            response.addHeader(HttpHeaders.SET_COOKIE, sessionCookieService.clear().toString());
        }
        ProblemResponses.write(response, HttpStatus.UNAUTHORIZED, reason != null ? reason.toString() : "Authentication required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        securityEvents.record(SecurityEvent.ACCESS_DENIED, request.getMethod() + " " + request.getServletPath());
        ProblemResponses.write(response, HttpStatus.FORBIDDEN, "Access denied");
    }
}
