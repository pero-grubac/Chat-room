package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.repositories.PermissionEntityRepository;
import org.unibl.etfbl.ChatRoom.services.PermissionService;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionEntityRepository repository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PermissionEntity create(PermissionEntity permission) {
        repository.save(permission);
        entityManager.refresh(permission);
        return permission;
    }

    @Override
    public void deleteAllByIdUser(Integer idUser) {
        repository.deleteAllByUser_IdUser(idUser);
    }
}
