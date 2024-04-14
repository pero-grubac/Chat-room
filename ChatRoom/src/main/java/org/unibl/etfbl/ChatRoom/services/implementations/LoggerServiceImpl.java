package org.unibl.etfbl.ChatRoom.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unibl.etfbl.ChatRoom.enums.ActionEnum;
import org.unibl.etfbl.ChatRoom.models.entities.LoggerEntity;
import org.unibl.etfbl.ChatRoom.repositories.LoggerEntityRepository;
import org.unibl.etfbl.ChatRoom.services.LoggerService;

import java.util.List;

@Service
@Transactional
public class LoggerServiceImpl implements LoggerService {
    @Autowired
    private LoggerEntityRepository repository;
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public List<LoggerEntity> getAllByType(ActionEnum actionEnum) {
        return null;
    }

    @Override
    public void saveLogger(LoggerEntity logger) {
        System.out.println(logger.toString());

        repository.saveAndFlush(logger);
        entityManager.refresh(logger);

    }
}
