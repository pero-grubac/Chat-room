package org.unibl.etfbl.ChatRoom.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

import java.util.List;

@Data
public class ApproveUser {
    private int id;
    private boolean isApproved;
    @BannedWordsConstraint(paramName = "role")
    private RoleEnum role;
    @BannedWordsConstraint(paramName = "permissions")
    private List<PermissionEnum> permissions;

    @JsonProperty("isApproved")
    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }

}
