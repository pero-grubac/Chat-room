package org.unibl.etfbl.ChatRoom.advices;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
@Component
public class ActionLoggingAdvice {
    Logger log = LoggerFactory.getLogger(ActionLoggingAdvice.class);

    @Pointcut(value = "execution(* org.unibl.etfbl.ChatRoom.*.*.*(..) )")
    public void myPointcut() {

    }

    @Around("myPointcut()")
    public Object applicationLogger(ProceedingJoinPoint pjp) throws  Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";

        // Log method invocation
        String methodName = pjp.getSignature().getName();
        String className = pjp.getTarget().getClass().toString();
        Object[] array = pjp.getArgs();
        // PROVJERI stvari argumenata
        Object result;
       log.info("Method invoked {}: {}.{}() ", username, className, methodName);
        result = pjp.proceed();
        return result;
    }


}
