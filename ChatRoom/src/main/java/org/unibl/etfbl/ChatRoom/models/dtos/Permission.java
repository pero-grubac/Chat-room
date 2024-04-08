package org.unibl.etfbl.ChatRoom.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
@NoArgsConstructor
public class Permission {
    @BannedWordsConstraint(paramName = "permission")
    private PermissionEnum permission;
}
