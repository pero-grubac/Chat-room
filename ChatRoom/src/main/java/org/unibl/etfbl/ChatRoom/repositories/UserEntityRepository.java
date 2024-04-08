package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Integer> {
    Boolean existsByUsername(String username);

    Boolean existsByIdUser(Integer idUser);
    Boolean existsByEmail(String email);

    Optional<UserEntity> getUserEntityByUsername(String username);

    Optional<UserEntity> getUserEntityByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> getAllByIsApproved(Byte isApproved);
}
