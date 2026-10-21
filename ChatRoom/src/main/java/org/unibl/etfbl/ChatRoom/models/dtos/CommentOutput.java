package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CommentOutput {
    private Integer idComment;
    private String text;
    private Timestamp createdAt;
    private ForumRoom forumRoom;
    private UserOutput user;
    private Byte isAllowed;
}
