package org.unibl.etfbl.ChatRoom.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.config.UserInfoService;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Authenticates a request from its JWT. A token is accepted only if it is valid and is the one stored
 * for the user. Anything else leaves the request anonymous, and the reason is kept for the 401 body.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Request attribute with the reason a presented token was not accepted. */
    public static final String REJECTION_REASON = JwtAuthenticationFilter.class.getName() + ".reason";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserEntityRepository repository;

    @Autowired
    private SessionCookieService sessionCookieService;

    @Autowired
    private SecurityEventService securityEvents;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> token = sessionCookieService.resolveToken(request);
        if (token.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token.get(), request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String jwt, HttpServletRequest request) {
        try {
            UserEntity user = userInfoService.loadUserByUsername(jwtService.extractUserName(jwt));
            if (jwtService.isTokenValid(jwt, user) && Objects.equals(jwt, user.getJWT())) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            } else {
                // A correctly signed but replaced token: one active session per user, so end it
                user.setJWT(null);
                repository.saveAndFlush(user);
                request.setAttribute(REJECTION_REASON, "Session ended");
                securityEvents.record(SecurityEvent.SESSION_REVOKED, user.getUsername(), "An older token was presented");
            }
        } catch (ExpiredJwtException e) {
            reject(request, "JWT expired", e.getClaims().getSubject());
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            reject(request, "Invalid session", null);
        }
    }

    private void reject(HttpServletRequest request, String reason, String username) {
        request.setAttribute(REJECTION_REASON, reason);
        securityEvents.record(SecurityEvent.SESSION_REJECTED, username, reason + " on " + request.getMethod() + " " + request.getServletPath());
    }
}
