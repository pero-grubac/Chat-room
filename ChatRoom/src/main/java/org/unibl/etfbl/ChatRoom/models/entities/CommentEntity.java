package org.unibl.etfbl.ChatRoom.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "comment")
public class CommentEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IdComment", nullable = false)
    private Integer idComment;
    @Basic
    @Column(name = "Text", nullable = false, length = 45)
    private String text;
    @Basic
    @Column(name = "CreatedAt", nullable = false)
    private Timestamp createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdForumRoom", referencedColumnName = "IdForumRoom", nullable = false)
    private ForumRoomEntity forumRoom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser", referencedColumnName = "IdUser", nullable = false)
    private UserEntity user;
    @Basic
    @Column(name = "IsAllowed", nullable = true)
    private Byte isAllowed;

}
