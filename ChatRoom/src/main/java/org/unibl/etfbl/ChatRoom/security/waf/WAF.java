package org.unibl.etfbl.ChatRoom.security.waf;


import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unibl.etfbl.ChatRoom.advices.ExceptionLoggingAdvice;
import org.unibl.etfbl.ChatRoom.enums.BannedWordsEnum;
import jakarta.validation.ConstraintValidator;
import org.unibl.etfbl.ChatRoom.enums.ParameterLengthEnum;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.services.UserService;

import java.util.regex.Pattern;

public class WAF implements ConstraintValidator<BannedWordsConstraint, String> {
    @Autowired
    private UserService userService;
    private static final int MAX_LENGTH = 255;
    private String paramName;
    @Autowired
    private ExceptionLoggingAdvice exceptionLoggingAdvice;

    public boolean containsBannedWord(String input) {
        for (BannedWordsEnum bannedWord : BannedWordsEnum.values()) {
            String regex = "\\b" + Pattern.quote(bannedWord.getWord()) + "\\b";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input).find()) {
                //   System.out.println("Banned word found: " + bannedWord.getWord());
                throw new ConflictException("Banned word '" + bannedWord.getWord() + "' found in \"" + input + "\"");
            }
        }
        return false;
    }

    public boolean checkingLength(String paramName, String input) {
        for (ParameterLengthEnum param : ParameterLengthEnum.values())
            if (paramName.equalsIgnoreCase(param.getParamName())) {
                //  System.out.println(input.length() + " " + param.getParamLength());
                if (input.length() < param.getParamLength())
                    return true;
                else
                    throw new ConflictException("Input length for " + input + " is " + input.length() + ", but allowed is +" + paramName.length());
            }

        return false;
    }

    @Override
    public void initialize(BannedWordsConstraint constraintAnnotation) {
        this.paramName = constraintAnnotation.paramName();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "";

        //  System.out.println(s + " " + paramName + " " + containsBannedWord(s) + " " + checkingLength(paramName, s));
        try {
            if (containsBannedWord(s) || !checkingLength(paramName, s)) {
                if (username != null && !username.isEmpty() && !containsBannedWord(username))
                    userService.deleteJWT(username);
                return false;
            }
        } catch (Exception e) {
            userService.deleteJWT(username);
            exceptionLoggingAdvice.afterThrowing(e);
            return false;
        }
        return true;
    }
}

