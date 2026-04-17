package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
public class CommentInput {
    private Integer idComment;
    @BannedWordsConstraint(paramName = "text")
    private String text;
    private Integer idForumRoom;
}
