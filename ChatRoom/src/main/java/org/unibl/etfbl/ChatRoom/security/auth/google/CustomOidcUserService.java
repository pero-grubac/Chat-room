package org.unibl.etfbl.ChatRoom.security.auth.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        try {
            return processOidcUser(userRequest, oidcUser);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private String returnUniqueUsername(String givenName) {
        if (userRepository.existsByUsername(givenName)) {
            String uuid = UUID.randomUUID().toString();
            if (givenName.length() > 45) {
                return uuid.substring(0, Math.min(uuid.length(), 45));
            } else {
                var left = 45 - givenName.length();
                return givenName + uuid.substring(0, Math.min(uuid.length(), left));
            }
        }
        return givenName;
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(oidcUser.getAttributes());
System.out.println(oidcUser.getAttributes());
        Optional<UserEntity> userOptional = userRepository.findByEmail(googleUserInfo.getEmail());
        if (!userOptional.isPresent()) {
            UserEntity user = new UserEntity();
            user.setEmail(googleUserInfo.getEmail());
            user.setUsername(returnUniqueUsername(googleUserInfo.getName()));
            String uuid = UUID.randomUUID().toString();
            String newPassword = uuid.substring(0, Math.min(uuid.length(), 45));
            user.setPassword(passwordEncoder.encode(newPassword));
            RegristrationSourceEnum type = RegristrationSourceEnum.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
            user.setSource(type);
            userRepository.saveAndFlush(user);
            emailService.sendEmail(user.getEmail(), user.getUsername(), "ChatRoom - prijava sačuvana",
                    "Šifra postavljena na " + newPassword + " . \n Preporučuje se izmjena nakon što se nalog odobri.");
        } else {
            UserEntity user = userOptional.get();
            if (user.getIsApproved() != null && user.getIsApproved() == 1) {
                String uuid = UUID.randomUUID().toString();
                String truncatedToken = uuid.substring(0, Math.min(uuid.length(), 255));
                user.setTwoFactorToken(truncatedToken);
                userRepository.saveAndFlush(user);
                emailService.sendEmail(user.getEmail(), user.getUsername(), "ChatRoom - token za prijavu", truncatedToken);
            }
        }

        return oidcUser;
    }
}
