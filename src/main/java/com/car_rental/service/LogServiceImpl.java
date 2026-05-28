package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.LogDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);
    private final LogDAO dao;
    private final UtilityService utilityService = new UtilityService();

    @Autowired
    public LogServiceImpl(LogDAO dao) {
        this.dao = dao;
    }

    @Override
    public void logEvent(Integer userId, Log.AuditEventType eventType, String logText) {
        try {
            Log logEntry = new Log(userId, eventType, logText);
            dao.saveLog(logEntry);
            log.info("Logged event: {} for user {}", eventType, userId);
        } catch (Exception e) {
            log.error("Error logging event: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    @Override
    public PageResult<Log> getLogsPage(String q, int page, int size) {
        return dao.getLogsPage(q, page, size);
    }
}
