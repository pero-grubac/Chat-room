package org.unibl.etfbl.ChatRoom.advices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.unibl.etfbl.ChatRoom.exceptions.BadRequestException;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.exceptions.EmailException;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEvent;
import org.unibl.etfbl.ChatRoom.security.siem.SecurityEventService;

/**
 * Turns exceptions into RFC 7807 problem responses with a fitting status code.
 * Standard Spring MVC errors (unknown route, wrong method, ...) come from the base class.
 * Details of unexpected errors stay in the log and never reach the client.
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Autowired
    private SecurityEventService securityEvents;

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflict(ConflictException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail badRequest(BadRequestException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Detail of a WAF block; the web app recognises it and signs the user out, as the session was closed. */
    public static final String WAF_BLOCKED = "Blocked by the web application firewall";

    // The WAF reports through Bean Validation (@BannedWordsConstraint), so a blocked input ends up here
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        boolean wafBlock = ex.getBindingResult().getAllErrors().stream()
                .anyMatch(error -> "BannedWordsConstraint".equals(error.getCode()));
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, wafBlock ? WAF_BLOCKED : "Validation failed"));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "Malformed request body"));
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail notApproved(DisabledException e) {
        return problem(HttpStatus.FORBIDDEN, "Account is not approved yet");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail badCredentials(BadCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Bad credentials".equals(e.getMessage()) ? "Invalid credentials" : e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail unauthorized(AuthenticationException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail forbidden(AccessDeniedException e, HttpServletRequest request) {
        securityEvents.record(SecurityEvent.ACCESS_DENIED, request.getMethod() + " " + request.getServletPath() + ": " + e.getMessage());
        return problem(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(EmailException.class)
    public ProblemDetail email(EmailException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "The email could not be sent, try again later");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception e) {
        log.error("Unhandled exception", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }
}
