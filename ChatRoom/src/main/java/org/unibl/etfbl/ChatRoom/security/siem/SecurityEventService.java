package org.unibl.etfbl.ChatRoom.security.siem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.unibl.etfbl.ChatRoom.models.entities.SecurityEventEntity;
import org.unibl.etfbl.ChatRoom.repositories.SecurityEventRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The SIEM component: every security-sensitive event goes to logs/security.log as one JSON line
 * (for a log shipper or SIEM) and to the logger table (for the admin's security log).
 * Recording never fails the request that triggered it.
 */
@Service
public class SecurityEventService {

    private static final Logger siem = LoggerFactory.getLogger("SIEM");
    private static final Logger log = LoggerFactory.getLogger(SecurityEventService.class);
    private static final int MESSAGE_LENGTH = 255;
    private static final int USERNAME_LENGTH = 45;

    private final SecurityEventRepository repository;
    private final TransactionTemplate newTransaction;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Clock clock = Clock.systemUTC();

    public SecurityEventService(SecurityEventRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Records an event for the signed-in user (or anonymous). */
    public void record(SecurityEvent event, String detail) {
        record(event, currentUsername(), detail);
    }

    public void record(SecurityEvent event, String username, String detail) {
        SecurityEventEntity entity = SecurityEventEntity.builder()
                .type(event.name())
                .username(StringUtils.truncate(username, USERNAME_LENGTH))
                .ipAddress(clientIp())
                .message(StringUtils.truncate(clean(detail), MESSAGE_LENGTH))
                .createdAt(Instant.now(clock))
                .build();
        writeLog(event, entity);
        try {
            newTransaction.executeWithoutResult(status -> repository.save(entity));
        } catch (RuntimeException e) {
            log.error("Could not store security event {}", event, e);
        }
    }

    public List<SecurityEventEntity> latest(int limit, String type) {
        PageRequest page = PageRequest.of(0, Math.max(1, Math.min(limit, 500)));
        return StringUtils.isBlank(type)
                ? repository.findAllByOrderByCreatedAtDescIdDesc(page)
                : repository.findAllByTypeOrderByCreatedAtDescIdDesc(type, page);
    }

    private void writeLog(SecurityEvent event, SecurityEventEntity entity) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("time", entity.getCreatedAt().toString());
        line.put("event", event.name());
        line.put("severity", event.getSeverity().name());
        line.put("user", entity.getUsername());
        line.put("ip", entity.getIpAddress());
        line.put("detail", entity.getMessage());
        String json;
        try {
            json = objectMapper.writeValueAsString(line);
        } catch (JsonProcessingException e) {
            json = line.toString();
        }
        if (event.getSeverity() == SecurityEvent.Severity.WARN) {
            siem.warn(json);
        } else {
            siem.info(json);
        }
    }

    private static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }

    /** The client address; behind Nginx Tomcat already resolved it from X-Forwarded-For. */
    private static String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }

    // Line breaks would let a value forge extra log lines
    private static String clean(String detail) {
        return detail == null ? "" : detail.replaceAll("[\\r\\n\\t]", "_");
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }
}
