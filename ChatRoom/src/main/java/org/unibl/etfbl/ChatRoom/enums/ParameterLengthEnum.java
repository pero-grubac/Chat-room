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
    PERMISSION("Permission", 45),
    PERMISSIONS("permissions", 3),
    FORUM_ROOM_NAME("ForumRoomName", 45),
    COMMENT_TEXT("CommentText", 45),
    ROLE("role", 45),
    SOURCE("source", 45),
    NAME("name", 45),
    TEXT("text", 100);


    private final String paramName;
    private final int paramLength;
}
