package org.unibl.etfbl.ChatRoom.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
public class ApproveComment {
    private Integer id;
    private boolean isApproved;
    @JsonProperty("isApproved")
    public void setApproved(boolean approved) {
        isApproved = approved;
    }
    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }
}
