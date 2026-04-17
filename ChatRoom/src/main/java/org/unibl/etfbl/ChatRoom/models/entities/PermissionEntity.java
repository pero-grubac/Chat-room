package org.unibl.etfbl.ChatRoom.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;

@Data
@Entity
@Table(name = "permission")
@NoArgsConstructor
public class PermissionEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IdPermission", nullable = false)
    @JsonIgnore
    private Integer idPermission;
    @Basic
    @Column(name = "Permission", nullable = false, length = 45)
    @Enumerated(EnumType.STRING)
    private PermissionEnum permission;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUser", referencedColumnName = "IdUser", nullable = false)
    @JsonIgnore
    private UserEntity user;

    public PermissionEntity(PermissionEnum permission, UserEntity user) {
        this.permission = permission;
        this.user = user;
    }
}
