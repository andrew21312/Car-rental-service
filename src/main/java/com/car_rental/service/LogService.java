package com.car_rental.service;

import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;


public interface LogService {
    void logEvent(Integer userId, Log.AuditEventType eventType, String logText);

    PageResult<Log> getLogsPage(String q, int page, int size);
}
