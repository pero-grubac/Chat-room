package org.unibl.etfbl.ChatRoom.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forumroom")
public class ForumRoomEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IdForumRoom", nullable = false)
    private Integer idForumRoom;
    @Basic
    @Column(name = "Name", nullable = false, length = 45,unique = true)
    private String name;
    @OneToMany(mappedBy = "forumRoom")
    @JsonIgnore
    private List<CommentEntity> comments;

}
