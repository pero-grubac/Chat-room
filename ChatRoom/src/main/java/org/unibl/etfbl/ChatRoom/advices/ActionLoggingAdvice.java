package org.unibl.etfbl.ChatRoom.advices;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Application log: who called which controller or service method. Security events go to the SIEM instead. */
@Aspect
@Component
public class ActionLoggingAdvice {
    private static final Logger log = LoggerFactory.getLogger(ActionLoggingAdvice.class);

    @Pointcut(value = "execution(* org.unibl.etfbl.ChatRoom.*.*.*(..) )")
    public void myPointcut() {

    }

    @Around("myPointcut()")
    public Object applicationLogger(ProceedingJoinPoint pjp) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymousUser";
        log.info("Method invoked username <{}>: classname <{}> method <{}()>",
                username, pjp.getTarget().getClass(), pjp.getSignature().getName());
        return pjp.proceed();
    }
}
