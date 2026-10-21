package org.unibl.etfbl.ChatRoom.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthRequest;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthResponse;
import org.unibl.etfbl.ChatRoom.models.dtos.OAuthTokenRequest;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.jwt.JwtService;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.util.Objects;

@Service
public class AuthenticationService {

    /** A completed sign-in: the JWT for the session cookie and what the web app may show. */
    public record SignIn(String jwt, AuthResponse user) {
    }

    @Autowired
    private UserEntityRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private SecurityEventService securityEvents;

    /**
     * Step 1: checks the password and emails a one-time code. Unknown users and wrong passwords
     * fail with BadCredentialsException, unapproved accounts with DisabledException.
     */
    public void login(AuthRequest authRequest) {
        UserEntity user = checkPassword(authRequest, "password step");
        twoFactorService.issueCode(user);
        securityEvents.record(SecurityEvent.LOGIN_CODE_SENT, user.getUsername(), "Code emailed");
    }

    /** Step 2: password again plus the emailed code. */
    public SignIn verifyTwoFactorToken(AuthRequest authRequest) {
        UserEntity user = checkPassword(authRequest, "code step");
        if (!twoFactorService.consumeCode(user, authRequest.getToken())) {
            securityEvents.record(SecurityEvent.LOGIN_CODE_INVALID, user.getUsername(), "Wrong or expired code");
            throw new BadCredentialsException("Invalid or expired code");
        }
        return startSession(user, "Password and code");
    }

    /** Google sign-in: the code that was emailed after the OAuth step, for an account from that provider. */
    public SignIn verifyOAuthToken(OAuthTokenRequest request) {
        UserEntity user = repository.findByEmail(request.getEmail()).orElse(null);
        boolean sourceMatches = user != null && user.getSource() != null
                && Objects.equals(user.getSource().toString(), request.getType());
        if (!sourceMatches || !user.isEnabled() || !twoFactorService.consumeCode(user, request.getToken())) {
            securityEvents.record(SecurityEvent.LOGIN_CODE_INVALID, user == null ? null : user.getUsername(),
                    "OAuth code for " + request.getEmail());
            throw new BadCredentialsException("Invalid or expired code");
        }
        return startSession(user, "OAuth and code");
    }

    /** Ends the session on the server: the stored JWT stops being accepted. */
    public void logout(String username) {
        repository.getUserEntityByUsername(username).ifPresent(user -> {
            user.setJWT(null);
            repository.saveAndFlush(user);
            securityEvents.record(SecurityEvent.LOGOUT, username, "Signed out");
        });
    }

    /** The signed-in user, with the rooms they belong to. */
    public AuthResponse describe(UserEntity user) {
        return AuthResponse.of(user, repository.findRoomIds(user.getIdUser()));
    }

    private UserEntity checkPassword(AuthRequest authRequest, String step) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (DisabledException e) {
            securityEvents.record(SecurityEvent.LOGIN_NOT_APPROVED, authRequest.getUsername(), "Account not approved, " + step);
            throw e;
        } catch (AuthenticationException e) {
            securityEvents.record(SecurityEvent.LOGIN_FAILED, authRequest.getUsername(), "Wrong username or password, " + step);
            throw e;
        }
        return repository.getUserEntityByUsername(authRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    private SignIn startSession(UserEntity user, String method) {
        String jwt = jwtService.generateToken(user);
        user.setJWT(jwt);
        repository.saveAndFlush(user);
        securityEvents.record(SecurityEvent.LOGIN_SUCCESS, user.getUsername(), method);
        return new SignIn(jwt, describe(user));
    }
}
