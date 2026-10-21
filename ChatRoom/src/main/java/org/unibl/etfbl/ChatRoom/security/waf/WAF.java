package org.unibl.etfbl.ChatRoom.security.waf;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unibl.etfbl.ChatRoom.enums.ParameterLengthEnum;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.Arrays;
import java.util.Optional;

/**
 * Validator behind {@link BannedWordsConstraint}: rejects input that matches a {@link WafRule} or is too long,
 * reports it to the SIEM and ends the session of the signed-in user who sent it.
 */
public class WAF implements ConstraintValidator<BannedWordsConstraint, String> {
    private static final int LOGGED_INPUT_LENGTH = 100;

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityEventService securityEvents;
    private String paramName;

    public Optional<WafRule> findViolation(String input) {
        return Arrays.stream(WafRule.values()).filter(rule -> rule.matches(input)).findFirst();
    }

    public boolean containsBannedWord(String input) {
        return findViolation(input).isPresent();
    }

    /** True if the input fits the column it is stored in; parameters without a limit always fit. */
    public boolean checkingLength(String paramName, String input) {
        return Arrays.stream(ParameterLengthEnum.values())
                .filter(param -> param.getParamName().equalsIgnoreCase(paramName))
                .findFirst()
                .map(param -> input.length() <= param.getParamLength())
                .orElse(true);
    }

    @Override
    public void initialize(BannedWordsConstraint constraintAnnotation) {
        this.paramName = constraintAnnotation.paramName();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        String reason = findViolation(s).map(rule -> "rule " + rule.name())
                .orElse(checkingLength(paramName, s) ? null : "length " + s.length());
        if (reason == null) {
            return true;
        }

        String username = signedInUsername();
        securityEvents.record(SecurityEvent.WAF_BLOCKED, username,
                "Field " + paramName + ", " + reason + ", input: " + forLog(s));
        if (username != null) {
            try {
                userService.deleteJWT(username);
            } catch (NotFoundException ignored) {
                // The account is gone already, so there is no session to end
            }
        }
        return false;
    }

    private static String signedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }

    // Line breaks would let an attacker forge log entries
    private static String forLog(String input) {
        String flat = input.replaceAll("[\\r\\n\\t]", "_");
        return flat.length() > LOGGED_INPUT_LENGTH ? flat.substring(0, LOGGED_INPUT_LENGTH) + "…" : flat;
    }
}
