package org.unibl.etfbl.ChatRoom.models.dtos;


import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

import java.sql.Timestamp;

@Data
public class CommentOutput {
    private Integer idComment;
    @BannedWordsConstraint(paramName = "text")
    private String text;
    private Timestamp createdAt;
    private ForumRoom forumRoom;
    private UserOutput user;
    private Byte isAllowed;
}
