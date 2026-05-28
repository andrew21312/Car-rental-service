package com.car_rental.dao.mapper;

import com.car_rental.entity.Log;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LogRowMapper implements RowMapper<Log> {
    @Override
    public Log mapRow(ResultSet rs, int rowNum) throws SQLException {
        Log log = new Log();
        log.setLogId(rs.getInt("log_id"));
        log.setLogCreatedAt(rs.getTimestamp("log_created_at"));
        log.setUserId(rs.getInt("log_user_id"));
        log.setEventType(Log.AuditEventType.valueOf(rs.getString("log_event_type")));
        log.setLogText(rs.getString("log_text"));
        return log;
    }
}
