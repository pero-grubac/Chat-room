package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;

/** A moderator's decision on a pending comment: isApproved 1 publishes it, 0 rejects it. */
@Data
public class ApproveComment {
    private Integer id;
    private Byte isApproved;
}
