package org.unibl.etfbl.ChatRoom.security.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthRequest;
import org.unibl.etfbl.ChatRoom.models.dtos.OAuthTokenRequest;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.jwt.JwtService;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserEntityRepository repository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TwoFactorService twoFactorService;
    @Mock
    private SecurityEventService securityEvents;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static UserEntity ana() {
        UserEntity user = new UserEntity();
        user.setUsername("ana");
        user.setEmail("ana@example.com");
        user.setIsApproved((byte) 1);
        user.setRole(RoleEnum.ROLE_MODERATOR);
        user.setSource(RegristrationSourceEnum.LOCAL);
        List<PermissionEntity> permissions = new ArrayList<>();
        permissions.add(new PermissionEntity(PermissionEnum.ADD, user));
        user.setPermissions(permissions);
        return user;
    }

    private static AuthRequest request(String code) {
        return AuthRequest.builder().username("ana").password("secret").token(code).build();
    }

    private static OAuthTokenRequest oauthRequest(String code, String type) {
        OAuthTokenRequest request = new OAuthTokenRequest();
        request.setEmail("ana@example.com");
        request.setToken(code);
        request.setType(type);
        return request;
    }

    // ── step 1 ─────────────────────────────────────────────────────

    @Test
    void correctPasswordSendsACode() {
        UserEntity ana = ana();
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(ana));

        authenticationService.login(request(null));

        verify(twoFactorService).issueCode(ana);
        verify(securityEvents).record(SecurityEvent.LOGIN_CODE_SENT, "ana", "Code emailed");
    }

    @Test
    void passwordIsCheckedBeforeAnythingElse() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(request(null))).isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(twoFactorService);
        verify(securityEvents).record(eq(SecurityEvent.LOGIN_FAILED), eq("ana"), anyString());
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("ana");
        assertThat(captor.getValue().getCredentials()).isEqualTo("secret");
    }

    @Test
    void unapprovedAccountGetsNoCode() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        assertThatThrownBy(() -> authenticationService.login(request(null))).isInstanceOf(DisabledException.class);
        verifyNoInteractions(twoFactorService);
        verify(securityEvents).record(eq(SecurityEvent.LOGIN_NOT_APPROVED), eq("ana"), anyString());
    }

    // ── step 2 ─────────────────────────────────────────────────────

    @Test
    void validCodeStartsAStoredSession() {
        UserEntity ana = ana();
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(ana));
        when(twoFactorService.consumeCode(ana, "code")).thenReturn(true);
        when(jwtService.generateToken(ana)).thenReturn("jwt");
        when(repository.findRoomIds(ana.getIdUser())).thenReturn(List.of(1, 3));

        AuthenticationService.SignIn signIn = authenticationService.verifyTwoFactorToken(request("code"));

        assertThat(signIn.jwt()).isEqualTo("jwt");
        assertThat(ana.getJWT()).isEqualTo("jwt");
        assertThat(signIn.user().getUsername()).isEqualTo("ana");
        assertThat(signIn.user().getRole()).isEqualTo("ROLE_MODERATOR");
        assertThat(signIn.user().getPermissions()).containsExactly("ADD");
        assertThat(signIn.user().getRooms()).containsExactly(1, 3);
        verify(repository).saveAndFlush(ana);
        verify(securityEvents).record(SecurityEvent.LOGIN_SUCCESS, "ana", "Password and code");
    }

    @Test
    void invalidCodeIsRejected() {
        UserEntity ana = ana();
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(ana));
        when(twoFactorService.consumeCode(ana, null)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.verifyTwoFactorToken(request(null)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("code");
        verifyNoInteractions(jwtService);
        verify(securityEvents).record(eq(SecurityEvent.LOGIN_CODE_INVALID), eq("ana"), anyString());
    }

    // ── Google ─────────────────────────────────────────────────────

    @Test
    void oauthCodeForTheRightProviderStartsASession() {
        UserEntity ana = ana();
        ana.setSource(RegristrationSourceEnum.GOOGLE);
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(ana));
        when(twoFactorService.consumeCode(ana, "code")).thenReturn(true);
        when(jwtService.generateToken(ana)).thenReturn("jwt");

        assertThat(authenticationService.verifyOAuthToken(oauthRequest("code", "GOOGLE")).jwt()).isEqualTo("jwt");
    }

    @Test
    void oauthCodeForAnotherProviderIsRejected() {
        UserEntity ana = ana();
        ana.setSource(RegristrationSourceEnum.GOOGLE);
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> authenticationService.verifyOAuthToken(oauthRequest("code", "GITHUB")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService);
    }

    @Test
    void emailAloneIsNotEnough() {
        UserEntity ana = ana();
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(ana));
        when(twoFactorService.consumeCode(ana, null)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.verifyOAuthToken(oauthRequest(null, "LOCAL")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService);
    }

    @Test
    void unknownEmailLooksLikeAWrongCode() {
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.verifyOAuthToken(oauthRequest("code", "GOOGLE")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired code");
    }

    // ── logout ─────────────────────────────────────────────────────

    @Test
    void logoutForgetsTheStoredJwt() {
        UserEntity ana = ana();
        ana.setJWT("jwt");
        when(repository.getUserEntityByUsername("ana")).thenReturn(Optional.of(ana));

        authenticationService.logout("ana");

        assertThat(ana.getJWT()).isNull();
        verify(repository).saveAndFlush(ana);
        verify(securityEvents).record(SecurityEvent.LOGOUT, "ana", "Signed out");
    }

    @Test
    void blockedGoogleAccountCannotFinishSigningIn() {
        UserEntity ana = ana();
        ana.setSource(RegristrationSourceEnum.GOOGLE);
        ana.setIsApproved((byte) 0);
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> authenticationService.verifyOAuthToken(oauthRequest("code", "GOOGLE")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(twoFactorService, jwtService);
    }
}
