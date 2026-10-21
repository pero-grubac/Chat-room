package org.unibl.etfbl.ChatRoom.security.auth.google;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.auth.TwoFactorService;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleServiceTest {

    @Mock
    private UserEntityRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private TwoFactorService twoFactorService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GoogleTokenVerifier googleTokenVerifier;
    @Mock
    private SecurityEventService securityEvents;

    @InjectMocks
    private GoogleService googleService;

    private static final GoogleTokenVerifier.GoogleProfile ANA = new GoogleTokenVerifier.GoogleProfile("ana@gmail.com", "Ana");

    @Test
    void firstSignInRegistersAPendingGoogleAccount() {
        when(googleTokenVerifier.verify("token")).thenReturn(ANA);
        when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        GoogleService.Result result = googleService.signIn("token");

        assertThat(result.outcome()).isEqualTo(GoogleService.Outcome.CREATED);
        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("Ana");
        assertThat(saved.getValue().getSource()).isEqualTo(RegristrationSourceEnum.GOOGLE);
        assertThat(saved.getValue().getRole()).isNull();
        assertThat(saved.getValue().getIsApproved()).isNull();
        verify(securityEvents).record(eq(SecurityEvent.REGISTERED), eq("Ana"), anyString());
        verifyNoInteractions(twoFactorService);
    }

    @Test
    void takenNameGetsASuffixInsteadOfFailing() {
        when(googleTokenVerifier.verify("token")).thenReturn(ANA);
        when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("Ana")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        googleService.signIn("token");

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUsername()).startsWith("Ana-").hasSizeLessThanOrEqualTo(45);
    }

    @Test
    void approvedAccountGetsACode() {
        UserEntity ana = new UserEntity();
        ana.setUsername("Ana");
        ana.setIsApproved((byte) 1);
        ana.setRole(RoleEnum.ROLE_KORISNIK);
        when(googleTokenVerifier.verify("token")).thenReturn(ANA);
        when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(ana));

        GoogleService.Result result = googleService.signIn("token");

        assertThat(result.outcome()).isEqualTo(GoogleService.Outcome.CODE_SENT);
        assertThat(result.email()).isEqualTo("ana@gmail.com");
        verify(twoFactorService).issueCode(ana);
    }

    @Test
    void pendingOrBlockedAccountGetsNoCode() {
        UserEntity ana = new UserEntity();
        ana.setUsername("Ana");
        ana.setIsApproved((byte) 0);
        when(googleTokenVerifier.verify("token")).thenReturn(ANA);
        when(userRepository.findByEmail("ana@gmail.com")).thenReturn(Optional.of(ana));

        assertThat(googleService.signIn("token").outcome()).isEqualTo(GoogleService.Outcome.NOT_APPROVED);
        verifyNoInteractions(twoFactorService);
        verify(securityEvents).record(eq(SecurityEvent.LOGIN_NOT_APPROVED), eq("Ana"), anyString());
    }

    @Test
    void invalidTokenChangesNothing() {
        when(googleTokenVerifier.verify("forged")).thenThrow(new BadCredentialsException("Invalid Google token"));

        assertThatThrownBy(() -> googleService.signIn("forged")).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(userRepository, twoFactorService, emailService);
    }
}
