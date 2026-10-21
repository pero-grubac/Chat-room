package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.SecurityEventEntity;

import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, Integer> {
    List<SecurityEventEntity> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

    List<SecurityEventEntity> findAllByTypeOrderByCreatedAtDescIdDesc(String type, Pageable pageable);
}
