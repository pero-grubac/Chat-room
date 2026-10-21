package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;

import java.util.List;

@Data
@NoArgsConstructor
public class UserOutput {
    private Integer idUser;
    private String username;
    private String email;
    private RoleEnum role;
    private List<PermissionEntity> permissions;
    /** null pending, 0 rejected or blocked, 1 approved */
    private Byte isApproved;
    private List<Integer> roomIds;
}
