package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.LogDAO;
import com.car_rental.dao.mapper.LogRowMapper;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MySqlLogDAO extends BasePaginationDAO implements LogDAO {

    // SQL Queries
    private static final String SELECT_ALL_LOGS = """
            SELECT
                id as log_id,
                created_at as log_created_at,
                user_id as log_user_id,
                event_type as log_event_type,
                text as log_text
            FROM logs
            """;

    private static final String INSERT_LOG =
            "INSERT INTO logs (user_id, event_type, text) VALUES (?, ?, ?)";

    private static final String COUNT_LOGS =
            "SELECT COUNT(*) FROM logs";

    private static final String DEFAULT_ORDER_BY = "log_created_at DESC";

    // Dependencies
    private final LogRowMapper logRowMapper;

    @Autowired
    public MySqlLogDAO(JdbcTemplate jdbcTemplate, LogRowMapper logRowMapper) {
        super(jdbcTemplate);
        this.logRowMapper = logRowMapper;
    }

    @Override
    public void saveLog(Log log) {
        jdbcTemplate.update(INSERT_LOG,
                            log.getUserId(),
                            log.getEventType().name(),
                            log.getLogText());
    }

    @Override
    public PageResult<Log> getLogsPage(String searchQuery, int page, int size) {
        QueryParams queryParams = buildQueryParams(searchQuery);

        return executePaginationQuery(
                SELECT_ALL_LOGS,
                COUNT_LOGS,
                logRowMapper,
                queryParams,
                page,
                size,
                DEFAULT_ORDER_BY
                                     );
    }

    private QueryParams buildQueryParams(String searchQuery) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            conditions.add("logs.text LIKE ?");
            parameters.add("%" + searchQuery.trim() + "%");
        }

        return new QueryParams(conditions, parameters);
    }
}