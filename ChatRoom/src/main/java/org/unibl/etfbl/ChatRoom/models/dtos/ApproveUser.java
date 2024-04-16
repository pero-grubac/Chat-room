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
    private RoleEnum role;
    private List<PermissionEnum> permissions;
    private  Integer idRoom;
    @JsonProperty("isApproved")
    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }

}
