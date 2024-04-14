package org.unibl.etfbl.ChatRoom.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.unibl.etfbl.ChatRoom.security.waf.BannedWordsConstraint;

@Data
public class ApproveComment {
    private Integer id;
    private Byte isApproved;

  /*  @JsonProperty("isApproved")
    public void setApproved(String isApproved) {
        this.isApproved = isApproved.equals("true");
    }

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return isApproved;
    }*/
}
