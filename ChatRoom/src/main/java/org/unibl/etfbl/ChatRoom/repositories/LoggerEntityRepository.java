package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.LoggerEntity;
@Repository
public interface LoggerEntityRepository extends JpaRepository<LoggerEntity,Integer> {
}
