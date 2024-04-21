package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.exceptions.EmailException;
import org.unibl.etfbl.ChatRoom.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String receiver, String username, String subject, String content) {
        try {
            validateEmail(receiver, username);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(receiver);
            helper.setSubject(subject);
            helper.setText(content);
             javaMailSender.send(message);
            System.out.println("\"token\":\"" + content + "\"");
        } catch (Exception e) {
            throw new EmailException(username, receiver);
        }
    }

    private void validateEmail(String email, String username) {
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (Exception e) {
            throw new EmailException(username, email);
        }
    }
}
