package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;

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
    }
    */
}
