package org.unibl.etfbl.ChatRoom.services;

public interface EmailService {
    void sendEmail(String receiver, String username, String subject, String content);
}
