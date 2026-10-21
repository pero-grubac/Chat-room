package org.unibl.etfbl.ChatRoom.security.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues and checks the one-time sign-in codes. A code is emailed, expires after
 * {@code two-factor.expiration} and is removed after one attempt, right or wrong.
 */
@Service
public class TwoFactorService {

    @Autowired
    private UserEntityRepository repository;
    @Autowired
    private EmailService emailService;

    @Value("${two-factor.expiration:PT5M}")
    private Duration expiration = Duration.ofMinutes(5);

    private Clock clock = Clock.systemUTC();

    public void issueCode(UserEntity user) {
        String code = UUID.randomUUID().toString();
        user.setTwoFactorToken(code);
        user.setTwoFactorTokenExpiresAt(Instant.now(clock).plus(expiration));
        repository.saveAndFlush(user);
        emailService.sendEmail(user.getEmail(), user.getUsername(), "ChatRoom - token za prijavu", code);
    }

    /**
     * True only if a code was issued, has not expired and equals {@code code}.
     * The pending code is removed in every case, so it can never be guessed twice.
     */
    public boolean consumeCode(UserEntity user, String code) {
        String pending = user.getTwoFactorToken();
        Instant expiresAt = user.getTwoFactorTokenExpiresAt();
        if (pending == null) {
            return false;
        }
        user.setTwoFactorToken(null);
        user.setTwoFactorTokenExpiresAt(null);
        repository.saveAndFlush(user);
        return StringUtils.isNotBlank(code)
                && Objects.equals(pending, code)
                && expiresAt != null
                && Instant.now(clock).isBefore(expiresAt);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }
}
