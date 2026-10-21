package org.unibl.etfbl.ChatRoom.controllers;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthRequest;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthResponse;
import org.unibl.etfbl.ChatRoom.models.dtos.OAuthLoginRequest;
import org.unibl.etfbl.ChatRoom.models.dtos.OAuthTokenRequest;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.security.auth.AuthenticationService;
import org.unibl.etfbl.ChatRoom.security.auth.google.GoogleService;
import org.unibl.etfbl.ChatRoom.security.jwt.SessionCookieService;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private GoogleService googleService;
    @Autowired
    private SessionCookieService sessionCookieService;

    /** Step 1: password. On success a one-time code is emailed. */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid AuthRequest request) {
        authenticationService.login(request);
        return ResponseEntity.ok().build();
    }

    /** Step 2: password + code. Starts the session cookie. */
    @PostMapping("/token")
    public ResponseEntity<AuthResponse> verifyTwoFactorToken(@RequestBody @Valid AuthRequest request) {
        return withSession(authenticationService.verifyTwoFactorToken(request));
    }

    @PostMapping("/oauth/token")
    public ResponseEntity<AuthResponse> verifyOAuthToken(@RequestBody @Valid OAuthTokenRequest request) {
        return withSession(authenticationService.verifyOAuthToken(request));
    }

    /**
     * Google sign-in, step 1: the access token is checked with Google. A new account is registered (201),
     * an approved one gets a code by email (200), anything else is refused (403).
     */
    @PostMapping("/oauth")
    public ResponseEntity<Map<String, String>> oauth(@RequestBody OAuthLoginRequest request) {
        GoogleService.Result result = googleService.signIn(request.getAccessToken());
        HttpStatus status = switch (result.outcome()) {
            case CREATED -> HttpStatus.CREATED;
            case CODE_SENT -> HttpStatus.OK;
            case NOT_APPROVED -> HttpStatus.FORBIDDEN;
        };
        return ResponseEntity.status(status).body(Map.of("email", result.email()));
    }

    /** Who is signed in; the web app calls it on start because it cannot read the cookie. */
    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal UserEntity user) {
        return authenticationService.describe(user);
    }

    /** Ends the session on the server and removes the cookie. Works even if the session already expired. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserEntity user) {
        if (user != null) {
            authenticationService.logout(user.getUsername());
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookieService.clear().toString())
                .build();
    }

    private ResponseEntity<AuthResponse> withSession(AuthenticationService.SignIn signIn) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookieService.create(signIn.jwt()).toString())
                .body(signIn.user());
    }
}
