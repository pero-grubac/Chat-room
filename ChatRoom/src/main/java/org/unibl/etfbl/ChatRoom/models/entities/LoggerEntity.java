package org.unibl.etfbl.ChatRoom.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.unibl.etfbl.ChatRoom.enums.ActionEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "logger")
public class LoggerEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IdLogger", nullable = false)
    private Integer idLogger;
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false)
    private ActionEnum actionType;
    @Basic
    @Column(name = "Message", nullable = false, length = 255)
    private String message;

}
