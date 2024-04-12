package org.unibl.etfbl.ChatRoom.security.waf;


import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unibl.etfbl.ChatRoom.enums.BannedWordsEnum;
import jakarta.validation.ConstraintValidator;
import org.unibl.etfbl.ChatRoom.enums.ParameterLengthEnum;
import org.unibl.etfbl.ChatRoom.services.UserService;

public class WAF implements ConstraintValidator<BannedWordsConstraint, String> {
    @Autowired
    private UserService userService;
    private static final int MAX_LENGTH = 255;
    private String paramName;

    public boolean containsBannedWord(String input) {
        for (BannedWordsEnum bannedWord : BannedWordsEnum.values()) {
            if (input.toLowerCase().contains(bannedWord.getWord().toLowerCase()))
                return true;
        }
        return false;
    }

    public boolean checkingLength(String paramName, String input) {
        for (ParameterLengthEnum param : ParameterLengthEnum.values())
            if (paramName.toLowerCase().equals(param.getParamName()))
                return input.length() < param.getParamLength();

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

       //  System.out.println(s + " " + containsBannedWord(s) + " " + checkingLength(paramName, s));

        if (!containsBannedWord(s) && checkingLength(paramName, s)) {
            if (username != null && !username.isEmpty() && !containsBannedWord(username))
                userService.deleteJWT(username);
            return false;
        }
        return true;
    }
}

