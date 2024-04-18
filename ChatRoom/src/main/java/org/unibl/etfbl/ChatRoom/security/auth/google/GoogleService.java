package org.unibl.etfbl.ChatRoom.security.auth.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.exceptions.ConflictException;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleService {
    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public String loadUser(Map<String, Object> user) {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(user);


        if (googleUserInfo.getEmail() == null || googleUserInfo.getEmail().isEmpty() || googleUserInfo.getEmail().isBlank()
                || googleUserInfo.getName() == null || googleUserInfo.getName().isEmpty() || googleUserInfo.getName().isBlank()) {
            throw new ConflictException("Invalid data");
        }
        return processUser(googleUserInfo);
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

    private String processUser(GoogleUserInfo googleUserInfo) {
        Optional<UserEntity> user = userRepository.findByEmail(googleUserInfo.getEmail());
        if (user.isPresent()) {
            UserEntity userEntity = user.get();
            if (userEntity.getIsApproved() != null && userEntity.getIsApproved() == 1) {

                String uuid = UUID.randomUUID().toString();
                String truncatedToken = uuid.substring(0, Math.min(uuid.length(), 255));
                userEntity.setTwoFactorToken(truncatedToken);
                userRepository.saveAndFlush(userEntity);
                emailService.sendEmail(userEntity.getEmail(), userEntity.getUsername(), "ChatRoom - token za prijavu", truncatedToken);
                return "token";
            }
            return "rejected";
        } else {
            if (userRepository.existsByUsername(googleUserInfo.getName())) {
                throw new ConflictException("User exists");
            }
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(returnUniqueUsername(googleUserInfo.getName()));
            userEntity.setEmail(googleUserInfo.getEmail());
            userEntity.setRole(null);
            String uuid = UUID.randomUUID().toString();
            String newPassword = uuid.substring(0, Math.min(uuid.length(), 45));
            userEntity.setPassword(passwordEncoder.encode(newPassword));
            userEntity.setSource(RegristrationSourceEnum.GOOGLE);
            userRepository.saveAndFlush(userEntity);
            emailService.sendEmail(userEntity.getEmail(), userEntity.getUsername(), "ChatRoom - prijava sačuvana",
                    "Šifra postavljena na " + newPassword + " . \n Username je:" + userEntity.getUsername() + " Preporučuje se izmjena šifre nakon što se nalog odobri.");
            return "created";
        }
    }

}
