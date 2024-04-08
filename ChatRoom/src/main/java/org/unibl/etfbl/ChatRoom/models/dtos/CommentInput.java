package org.unibl.etfbl.ChatRoom.models.dtos;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

import java.sql.Timestamp;

@Data
public class CommentInput {
    private Integer idComment;
    @BannedWordsConstraint(paramName = "text")
    private String text;
    private Timestamp createdAt;
    private Integer idForumRoom;
    private Integer idUser;
}
