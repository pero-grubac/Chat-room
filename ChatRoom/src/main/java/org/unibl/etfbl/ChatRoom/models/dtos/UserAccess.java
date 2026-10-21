package org.unibl.etfbl.ChatRoom.models.dtos;

import lombok.Data;

/** Blocks (allowed = false) or restores access for an approved account. */
@Data
public class UserAccess {
    private Integer id;
    private boolean allowed;
}
