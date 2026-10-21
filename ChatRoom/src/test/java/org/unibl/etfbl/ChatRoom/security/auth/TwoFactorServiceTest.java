package org.unibl.etfbl.ChatRoom.security.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-21T12:00:00Z");

    @Mock
    private UserEntityRepository repository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private TwoFactorService twoFactorService;

    private UserEntity ana;

    @BeforeEach
    void setUp() {
        twoFactorService.setClock(Clock.fixed(NOW, ZoneOffset.UTC));
        ana = new UserEntity();
        ana.setUsername("ana");
        ana.setEmail("ana@example.com");
    }

    private void moveClock(Duration by) {
        twoFactorService.setClock(Clock.fixed(NOW.plus(by), ZoneOffset.UTC));
    }

    @Test
    void issuedCodeIsStoredWithAnExpiryAndEmailed() {
        twoFactorService.issueCode(ana);

        assertThat(ana.getTwoFactorToken()).hasSize(36);
        assertThat(ana.getTwoFactorTokenExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(5)));
        verify(repository).saveAndFlush(ana);
        verify(emailService).sendEmail(eq("ana@example.com"), eq("ana"), anyString(), eq(ana.getTwoFactorToken()));
    }

    @Test
    void everyCodeIsDifferent() {
        twoFactorService.issueCode(ana);
        String first = ana.getTwoFactorToken();
        twoFactorService.issueCode(ana);

        assertThat(ana.getTwoFactorToken()).isNotEqualTo(first);
    }

    @Test
    void correctCodeWithinTheWindowIsAcceptedOnce() {
        twoFactorService.issueCode(ana);
        String code = ana.getTwoFactorToken();
        moveClock(Duration.ofMinutes(4));

        assertThat(twoFactorService.consumeCode(ana, code)).isTrue();
        assertThat(twoFactorService.consumeCode(ana, code)).isFalse();
        assertThat(ana.getTwoFactorToken()).isNull();
        assertThat(ana.getTwoFactorTokenExpiresAt()).isNull();
    }

    @Test
    void expiredCodeIsRejected() {
        twoFactorService.issueCode(ana);
        String code = ana.getTwoFactorToken();
        moveClock(Duration.ofMinutes(5));

        assertThat(twoFactorService.consumeCode(ana, code)).isFalse();
    }

    @Test
    void wrongGuessBurnsTheCode() {
        twoFactorService.issueCode(ana);
        String code = ana.getTwoFactorToken();

        assertThat(twoFactorService.consumeCode(ana, "guess")).isFalse();
        assertThat(twoFactorService.consumeCode(ana, code)).isFalse();
    }

    @Test
    void noPendingCodeMeansNoSignIn() {
        assertThat(twoFactorService.consumeCode(ana, null)).isFalse();
        assertThat(twoFactorService.consumeCode(ana, "")).isFalse();
    }

    @Test
    void blankCodeIsRejectedEvenWithACodePending() {
        twoFactorService.issueCode(ana);

        assertThat(twoFactorService.consumeCode(ana, " ")).isFalse();
    }
}
