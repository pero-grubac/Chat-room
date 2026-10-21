package org.unibl.etfbl.ChatRoom.security.siem;

/** Security-sensitive events recorded for the SIEM. WARN events are the ones worth an alert. */
public enum SecurityEvent {
    REGISTERED(Severity.INFO),
    LOGIN_CODE_SENT(Severity.INFO),
    LOGIN_SUCCESS(Severity.INFO),
    LOGOUT(Severity.INFO),
    LOGIN_FAILED(Severity.WARN),
    LOGIN_NOT_APPROVED(Severity.WARN),
    LOGIN_CODE_INVALID(Severity.WARN),
    SESSION_REJECTED(Severity.WARN),
    SESSION_REVOKED(Severity.WARN),
    ACCESS_DENIED(Severity.WARN),
    WAF_BLOCKED(Severity.WARN),
    RATE_LIMITED(Severity.WARN),
    USER_APPROVED(Severity.INFO),
    USER_REJECTED(Severity.INFO),
    USER_BLOCKED(Severity.WARN),
    USER_UNBLOCKED(Severity.INFO),
    ROLE_CHANGED(Severity.INFO),
    USER_DELETED(Severity.WARN);

    public enum Severity {INFO, WARN}

    private final Severity severity;

    SecurityEvent(Severity severity) {
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
    }
}
