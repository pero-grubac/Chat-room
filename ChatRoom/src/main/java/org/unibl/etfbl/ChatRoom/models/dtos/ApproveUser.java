package org.unibl.etfbl.ChatRoom.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;

import java.util.List;

/** The admin's decision on a registration: approve with a role, permissions and (for moderators) rooms, or reject. */
@Data
public class ApproveUser {
    private int id;
    private boolean isApproved;
    private RoleEnum role;
    private List<PermissionEnum> permissions;
    /** Rooms a moderator manages; required for moderators, ignored for the other roles. */
    private List<Integer> roomIds;

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }

    @JsonProperty("isApproved")
    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
