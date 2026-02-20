package com.car_rental.dao.daointerface;

import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;

public interface LogDAO {
    void saveLog(Log log);

    PageResult<Log> getLogsPage(String q, int page, int size);
}
