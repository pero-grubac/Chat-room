package org.unibl.etfbl.ChatRoom.exceptions;

public class EmailException extends RuntimeException {
    public EmailException(String username, String email) {
        super("Failed to send email to user " + username + " at email " + email);
    }
}
