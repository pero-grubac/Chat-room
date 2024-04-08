package org.unibl.etfbl.ChatRoom.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
