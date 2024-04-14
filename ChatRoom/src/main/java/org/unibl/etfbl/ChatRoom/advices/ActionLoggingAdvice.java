package org.unibl.etfbl.ChatRoom.advices;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unibl.etfbl.ChatRoom.enums.ActionEnum;
import org.unibl.etfbl.ChatRoom.models.entities.LoggerEntity;
import org.unibl.etfbl.ChatRoom.services.LoggerService;

@Aspect
@Component
public class ActionLoggingAdvice {
    Logger log = LoggerFactory.getLogger(ActionLoggingAdvice.class);

    //    @Pointcut("execution(* org.unibl.etfbl.ChatRoom.controllers.*.*(..))")
    @Pointcut(value = "execution(* org.unibl.etfbl.ChatRoom.*.*.*(..) )")
    public void myPointcut() {

    }

    @Autowired
    private LoggerService service;

    @Around("myPointcut()")
    public Object applicationLogger(ProceedingJoinPoint pjp) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";

        // Log method invocation
        String methodName = pjp.getSignature().getName();
        String className = pjp.getTarget().getClass().toString();
        Object[] array = pjp.getArgs();
        // PROVJERI stvari argumenata
        Object result;
        String message = String.format("Method invoked %s: %s.%s()", username, className, methodName);
        log.info(message);
        result = pjp.proceed();
       /* LoggerEntity logger = new LoggerEntity();
        logger.setActionType(ActionEnum.ACTION);
        logger.setMessage(message);
        service.saveLogger(logger);*/
        return result;
    }


}
