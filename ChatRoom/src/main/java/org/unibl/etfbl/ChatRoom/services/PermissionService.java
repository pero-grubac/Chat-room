package org.unibl.etfbl.ChatRoom.services;

import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;

public interface PermissionService {
    PermissionEntity create(PermissionEntity permission);

    void deleteAllByIdUser(Integer idUser);

}
