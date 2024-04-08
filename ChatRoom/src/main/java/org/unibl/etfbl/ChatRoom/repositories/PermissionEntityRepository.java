package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;

@Repository
public interface PermissionEntityRepository extends JpaRepository<PermissionEntity, Integer> {

    void deleteAllByUser_IdUser(Integer idUser);
}
