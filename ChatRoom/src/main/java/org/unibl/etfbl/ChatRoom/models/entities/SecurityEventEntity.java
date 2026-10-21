package org.unibl.etfbl.ChatRoom.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** One SIEM record. Type is a plain string so rows written by older versions still load. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logger")
public class SecurityEventEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IdLogger", nullable = false)
    private Integer id;
    @Column(name = "Type", nullable = false, length = 45)
    private String type;
    @Column(name = "Message", nullable = false, length = 255)
    private String message;
    @Column(name = "Username", length = 45)
    private String username;
    @Column(name = "IpAddress", length = 45)
    private String ipAddress;
    @Column(name = "CreatedAt", nullable = false)
    private Instant createdAt;
}
