package org.unibl.etfbl.ChatRoom.controllers;


import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.RedirectView;
import org.unibl.etfbl.ChatRoom.advices.ExceptionLoggingAdvice;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthRequest;
import org.unibl.etfbl.ChatRoom.models.dtos.OAuthTokenRequest;
import org.unibl.etfbl.ChatRoom.security.auth.AuthenticationService;
import org.unibl.etfbl.ChatRoom.security.auth.google.GoogleService;
import org.unibl.etfbl.ChatRoom.security.auth.google.GoogleUserInfo;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ExceptionLoggingAdvice exceptionLoggingAdvice;
    @Autowired
    private GoogleService googleService;

    // TODO logout
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            authenticationService.login(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

    }

    @PostMapping("/token")
    public ResponseEntity<?> verifyTwoFactorToken(@RequestBody @Valid AuthRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            return ResponseEntity.ok(authenticationService.verifyTwoFactorToken(request));
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @PostMapping("/oauth/token")
    public ResponseEntity<?> verifyOAuthToken(@RequestBody @Valid OAuthTokenRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            return ResponseEntity.ok(authenticationService.verifyOAuthToken(request));
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

    }

    @CrossOrigin(origins = "https://accounts.google.com")
    @PostMapping("/oauth")
    public ResponseEntity<?> oauth(@RequestBody Map<String, Object> user) {
        try {
            String message = googleService.loadUser(user);
            if ("created".equals(message))
                return ResponseEntity.status(HttpStatus.CREATED).build();
            else if ("token".equals(message))
                return ResponseEntity.ok().build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
}


