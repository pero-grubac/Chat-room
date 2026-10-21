package org.unibl.etfbl.ChatRoom.security.siem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.unibl.etfbl.ChatRoom.models.entities.SecurityEventEntity;
import org.unibl.etfbl.ChatRoom.repositories.SecurityEventRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityEventServiceTest {

    private SecurityEventRepository repository;
    private SecurityEventService service;

    @BeforeEach
    void setUp() {
        repository = mock(SecurityEventRepository.class);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new SecurityEventService(repository, transactions);
        service.setClock(Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    private SecurityEventEntity saved() {
        ArgumentCaptor<SecurityEventEntity> captor = ArgumentCaptor.forClass(SecurityEventEntity.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void eventIsStoredWithUserIpAndTime() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.7");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(SecurityEvent.LOGIN_FAILED, "ana", "Wrong password");

        SecurityEventEntity event = saved();
        assertThat(event.getType()).isEqualTo("LOGIN_FAILED");
        assertThat(event.getUsername()).isEqualTo("ana");
        assertThat(event.getIpAddress()).isEqualTo("203.0.113.7");
        assertThat(event.getMessage()).isEqualTo("Wrong password");
        assertThat(event.getCreatedAt()).isEqualTo(Instant.parse("2026-10-21T12:00:00Z"));
    }

    @Test
    void signedInUserIsUsedWhenNoneIsGiven() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", null, List.of()));

        service.record(SecurityEvent.USER_BLOCKED, "Blocked ana");

        assertThat(saved().getUsername()).isEqualTo("admin");
    }

    @Test
    void longValuesAreCutToTheColumnSize() {
        service.record(SecurityEvent.WAF_BLOCKED, "u".repeat(80), "x".repeat(400));

        SecurityEventEntity event = saved();
        assertThat(event.getUsername()).hasSize(45);
        assertThat(event.getMessage()).hasSize(255);
    }

    @Test
    void lineBreaksCannotForgeLogEntries() {
        service.record(SecurityEvent.WAF_BLOCKED, "ana", "a\nFAKE\r\nb");

        assertThat(saved().getMessage()).isEqualTo("a_FAKE__b");
    }

    @Test
    void storageFailureNeverBreaksTheRequest() {
        when(repository.save(any())).thenThrow(new RuntimeException("database down"));

        assertThatCode(() -> service.record(SecurityEvent.LOGIN_FAILED, "ana", "x")).doesNotThrowAnyException();
    }

    @Test
    void latestIsCappedAndFiltered() {
        service.latest(10_000, null);
        service.latest(5, "WAF_BLOCKED");

        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByOrderByCreatedAtDescIdDesc(page.capture());
        assertThat(page.getValue().getPageSize()).isEqualTo(500);
        verify(repository).findAllByTypeOrderByCreatedAtDescIdDesc(any(), any());
    }
}
