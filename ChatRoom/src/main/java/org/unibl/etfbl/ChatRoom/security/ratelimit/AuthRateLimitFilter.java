package org.unibl.etfbl.ChatRoom.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.unibl.etfbl.ChatRoom.security.config.ProblemResponses;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits sign-in and registration attempts per client IP with a fixed window, against password
 * guessing and code spraying. In memory, so the limit is per backend instance.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private record Window(Instant start, int count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    private SecurityEventService securityEvents;

    @Value("${rate-limit.auth.requests:10}")
    private int maxRequests = 10;

    @Value("${rate-limit.auth.window:PT1M}")
    private Duration window = Duration.ofMinutes(1);

    private Clock clock = Clock.systemUTC();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        boolean limited = path.startsWith("/auth/") || path.equals("/users/add");
        return !"POST".equalsIgnoreCase(request.getMethod()) || !limited || path.equals("/auth/logout");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String client = request.getRemoteAddr();
        Instant now = Instant.now(clock);
        if (windows.size() > MAX_TRACKED_CLIENTS) {
            windows.values().removeIf(w -> !now.isBefore(w.start().plus(window)));
        }
        Window current = windows.compute(client, (key, w) ->
                w == null || !now.isBefore(w.start().plus(window)) ? new Window(now, 1) : new Window(w.start(), w.count() + 1));

        if (current.count() > maxRequests) {
            long retryAfter = Math.max(1, Duration.between(now, current.start().plus(window)).toSeconds());
            securityEvents.record(SecurityEvent.RATE_LIMITED, null, "Limit exceeded on " + request.getServletPath());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            ProblemResponses.write(response, HttpStatus.TOO_MANY_REQUESTS, "Too many attempts, try again in " + retryAfter + " s");
            return;
        }
        filterChain.doFilter(request, response);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    void configure(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }
}
