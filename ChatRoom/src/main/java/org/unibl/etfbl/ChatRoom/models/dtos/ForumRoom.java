package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
public class ForumRoom {
    private Integer idForumRoom;
    @BannedWordsConstraint(paramName = "name")
    private String name;
}
