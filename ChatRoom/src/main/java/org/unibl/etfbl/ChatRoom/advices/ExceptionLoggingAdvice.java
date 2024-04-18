package org.unibl.etfbl.ChatRoom.advices;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.unibl.etfbl.ChatRoom.enums.ActionEnum;
import org.unibl.etfbl.ChatRoom.models.entities.LoggerEntity;
import org.unibl.etfbl.ChatRoom.services.LoggerService;

@Aspect
@Component
public class ExceptionLoggingAdvice {
    private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingAdvice.class);
    @Autowired
    private LoggerService service;
    @Pointcut("execution(* org.unibl.etfbl.ChatRoom.*.*.*(..))")
    public void applicationMethod() {
        // Pointcut definition for methods in the specified package
    }

    @AfterThrowing(pointcut = "applicationMethod()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";
        String message = String.format("Exception occurred for user <%s>: message <%s>", username, ex.getMessage());
        log.error(message);
       /* LoggerEntity logger = new LoggerEntity();
        logger.setActionType(ActionEnum.EXCEPTION);
        logger.setMessage(message);
        service.saveLogger(logger);*/

    }
}
