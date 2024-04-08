package org.unibl.etfbl.ChatRoom.security.waf;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WAF.class)
public @interface BannedWordsConstraint {
    String message() default "Contains banned word";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String paramName() default "";
}
