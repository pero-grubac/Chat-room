package org.unibl.etfbl.ChatRoom.services;

import org.unibl.etfbl.ChatRoom.enums.ActionEnum;
import org.unibl.etfbl.ChatRoom.models.entities.LoggerEntity;

import java.util.List;

public interface LoggerService {
    List<LoggerEntity> getAllByType(ActionEnum actionEnum);
    void saveLogger(LoggerEntity logger);
}
