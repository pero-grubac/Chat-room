package org.unibl.etfbl.ChatRoom.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Integer> {
    Boolean existsByUsername(String username);

    Boolean existsByIdUser(Integer idUser);

    Optional<UserEntity> getUserEntityByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> getAllByIsApproved(Byte isApproved);

    List<UserEntity> getAllByRooms_IdForumRoom(Integer id);

    /** Whether the user belongs to the room, without loading the lazy room collection. */
    boolean existsByIdUserAndRooms_IdForumRoom(Integer idUser, Integer idForumRoom);

    @Query("select r.idForumRoom from UserEntity u join u.rooms r where u.idUser = :idUser order by r.idForumRoom")
    List<Integer> findRoomIds(@Param("idUser") Integer idUser);
}
