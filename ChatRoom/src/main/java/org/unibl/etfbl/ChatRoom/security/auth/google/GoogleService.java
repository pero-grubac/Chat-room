package org.unibl.etfbl.ChatRoom.security.auth.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.auth.TwoFactorService;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.util.Optional;
import java.util.UUID;

/** Sign-in with Google: the first time registers a pending account, later ones email a one-time code. */
@Service
public class GoogleService {

    public enum Outcome {
        /** A new account was registered and waits for approval. */
        CREATED,
        /** The account is approved; a code was emailed for the second step. */
        CODE_SENT,
        /** The account exists but is not (or no longer) approved. */
        NOT_APPROVED
    }

    public record Result(Outcome outcome, String email) {
    }

    private static final int MAX_USERNAME_LENGTH = 45;

    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private SecurityEventService securityEvents;

    public Result signIn(String accessToken) {
        GoogleTokenVerifier.GoogleProfile profile = googleTokenVerifier.verify(accessToken);
        Optional<UserEntity> existing = userRepository.findByEmail(profile.email());
        if (existing.isEmpty()) {
            register(profile);
            return new Result(Outcome.CREATED, profile.email());
        }
        UserEntity user = existing.get();
        if (!user.isEnabled()) {
            securityEvents.record(SecurityEvent.LOGIN_NOT_APPROVED, user.getUsername(), "Google sign-in");
            return new Result(Outcome.NOT_APPROVED, profile.email());
        }
        twoFactorService.issueCode(user);
        securityEvents.record(SecurityEvent.LOGIN_CODE_SENT, user.getUsername(), "Google sign-in");
        return new Result(Outcome.CODE_SENT, profile.email());
    }

    private void register(GoogleTokenVerifier.GoogleProfile profile) {
        UserEntity user = new UserEntity();
        user.setUsername(uniqueUsername(profile.name()));
        user.setEmail(profile.email());
        String password = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(password));
        user.setSource(RegristrationSourceEnum.GOOGLE);
        userRepository.saveAndFlush(user);
        securityEvents.record(SecurityEvent.REGISTERED, user.getUsername(), "Google account " + profile.email());
        emailService.sendEmail(user.getEmail(), user.getUsername(), "ChatRoom - prijava sačuvana",
                "Šifra postavljena na " + password + " . \n Username je: " + user.getUsername()
                        + " Preporučuje se izmjena šifre nakon što se nalog odobri.");
    }

    /** The Google name if it is free, otherwise the name with a random suffix, never longer than the column. */
    private String uniqueUsername(String name) {
        String base = name.length() > MAX_USERNAME_LENGTH ? name.substring(0, MAX_USERNAME_LENGTH) : name;
        String candidate = base;
        while (userRepository.existsByUsername(candidate)) {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            candidate = base.substring(0, Math.min(base.length(), MAX_USERNAME_LENGTH - suffix.length() - 1)) + "-" + suffix;
        }
        return candidate;
    }
}
