package org.unibl.etfbl.ChatRoom.advices;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionLoggingAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingAdvice.class);

    @Pointcut("execution(* org.unibl.etfbl.ChatRoom.*.*.*(..))")
    public void applicationMethod() {
        // Pointcut definition for methods in the specified package
    }

    @AfterThrowing(pointcut = "applicationMethod()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";
        log.error("Exception occurred for user {}: {}", username, ex.getMessage());
    }
}
