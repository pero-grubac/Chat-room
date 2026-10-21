package org.unibl.etfbl.ChatRoom.security.ratelimit;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthRateLimitFilterTest {

    private static final Instant NOW = Instant.parse("2026-10-21T12:00:00Z");

    private AuthRateLimitFilter filter;
    private FilterChain chain;
    private SecurityEventService securityEvents;

    @BeforeEach
    void setUp() {
        filter = new AuthRateLimitFilter();
        securityEvents = mock(SecurityEventService.class);
        ReflectionTestUtils.setField(filter, "securityEvents", securityEvents);
        filter.configure(3, Duration.ofMinutes(1));
        filter.setClock(Clock.fixed(NOW, ZoneOffset.UTC));
        chain = mock(FilterChain.class);
    }

    private MockHttpServletResponse send(String method, String path, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void requestsWithinTheLimitPass() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertThat(send("POST", "/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        }
        verify(chain, times(3)).doFilter(any(), any());
    }

    @Test
    void requestOverTheLimitGets429WithRetryAfter() throws Exception {
        for (int i = 0; i < 3; i++) {
            send("POST", "/auth/token", "10.0.0.1");
        }

        MockHttpServletResponse blocked = send("POST", "/auth/token", "10.0.0.1");

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(blocked.getContentAsString()).contains("Too many attempts");
        verify(securityEvents).record(eq(SecurityEvent.RATE_LIMITED), isNull(), contains("/auth/token"));
        verify(chain, times(3)).doFilter(any(), any());
    }

    @Test
    void limitIsPerClient() throws Exception {
        for (int i = 0; i < 3; i++) {
            send("POST", "/auth/login", "10.0.0.1");
        }

        assertThat(send("POST", "/auth/login", "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void windowResets() throws Exception {
        for (int i = 0; i < 4; i++) {
            send("POST", "/auth/login", "10.0.0.1");
        }
        filter.setClock(Clock.fixed(NOW.plus(Duration.ofMinutes(1)), ZoneOffset.UTC));

        assertThat(send("POST", "/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
    }

    @Test
    void registrationIsLimitedToo() throws Exception {
        for (int i = 0; i < 3; i++) {
            send("POST", "/users/add", "10.0.0.1");
        }

        assertThat(send("POST", "/users/add", "10.0.0.1").getStatus()).isEqualTo(429);
    }

    @Test
    void otherRequestsAreNotCounted() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertThat(send("GET", "/forumrooms", "10.0.0.1").getStatus()).isEqualTo(200);
            assertThat(send("GET", "/auth/me", "10.0.0.1").getStatus()).isEqualTo(200);
            assertThat(send("POST", "/auth/logout", "10.0.0.1").getStatus()).isEqualTo(200);
        }
    }
}
