package org.unibl.etfbl.ChatRoom.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.EmailException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthRequest;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthResponse;
import org.unibl.etfbl.ChatRoom.models.dtos.OAuthTokenRequest;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.jwt.JwtService;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Autowired
    private UserEntityRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private EmailService emailService;

    public void login(AuthRequest authRequest) throws NotFoundException, EmailException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        var user = repository.getUserEntityByUsername(authRequest.getUsername()).orElseThrow(
                () -> new NotFoundException("User with username " + authRequest.getUsername() + " not found")
        );
        if (user.getIsApproved() != null && user.getIsApproved() == 1) {
            String uuid = UUID.randomUUID().toString();
            String truncatedToken = uuid.substring(0, Math.min(uuid.length(), 255));
            user.setTwoFactorToken(truncatedToken);
            repository.saveAndFlush(user);
            emailService.sendEmail(user.getEmail(), user.getUsername(), "ChatRoom - token za prijavu", truncatedToken);
        }
    }

    public AuthResponse verifyTwoFactorToken(AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        var user = repository.getUserEntityByUsername(authRequest.getUsername()).orElseThrow(
                () -> new NotFoundException("User with username " + authRequest.getUsername() + " not found")
        );
        if (Objects.equals(user.getTwoFactorToken(), authRequest.getToken())) {
            var jwt = jwtService.generateToken(user);
            user.setJWT(jwt);
            user.setTwoFactorToken(null);
            repository.saveAndFlush(user);
            return AuthResponse.builder().token(jwt).build();
        } else {
            throw new ConflictException("User with username " + user.getUsername() + " doesn't have two factor token.");
        }

    }

    public AuthResponse verifyOAuthToken(OAuthTokenRequest request) {
        UserEntity user = repository.getUserEntityByEmail(request.getEmail()).orElseThrow(
                () -> new NotFoundException("User with email " + request.getEmail() + " not found")
        );
        System.out.println((user.getUsername()));
        if (Objects.equals(user.getTwoFactorToken(), request.getToken())
                && Objects.equals(user.getSource().toString(), request.getType())) {
            var jwt = jwtService.generateToken(user);
            user.setJWT(jwt);
            user.setTwoFactorToken(null);
            repository.saveAndFlush(user);
            return AuthResponse.builder().token(jwt).build();

        } else {
            throw new ConflictException("User with username " + user.getUsername() + " doesn't have two factor token.");
        }

    }
}
