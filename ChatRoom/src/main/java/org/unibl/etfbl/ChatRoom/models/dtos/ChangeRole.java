package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;

import java.util.List;

@Data
public class ChangeRole {
    private Integer id;
    private RoleEnum role;
    private List<PermissionEnum> permissions;
    /** Rooms the user moderates or belongs to; null leaves them unchanged. */
    private List<Integer> roomIds;

}
