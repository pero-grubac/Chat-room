package org.unibl.etfbl.ChatRoom.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParameterLengthEnum {
    USERNAME("Username", 45),
    EMAIL("Email", 45),
    PASSWORD("Password", 255),
    TWO_FACTOR_TOKEN("Token", 255),
    NAME("name", 45),
    TEXT("text", 100);


    private final String paramName;
    private final int paramLength;
}
