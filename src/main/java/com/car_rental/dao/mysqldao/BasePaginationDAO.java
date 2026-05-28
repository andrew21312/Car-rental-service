package com.car_rental.dao.mysqldao;

import com.car_rental.entity.PageResult;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public abstract class BasePaginationDAO {

    protected final JdbcTemplate jdbcTemplate;

    @Autowired
    protected BasePaginationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static StringBuilder builder(String baseQuery, QueryParams queryParams) {
        StringBuilder queryBuilder = new StringBuilder(baseQuery);
        if (!queryParams.conditions().isEmpty()) {
            if (baseQuery.contains("WHERE")) {
                queryBuilder.append(" AND ");
            } else {
                queryBuilder.append(" WHERE ");
            }
            queryBuilder.append(String.join(" AND ", queryParams.conditions()));
        }
        return queryBuilder;
    }

    public <T> PageResult<T> executePaginationQuery(
            String baseQuery,
            String countQuery,
            RowMapper<T> rowMapper,
            QueryParams queryParams,
            int page,
            int size,
            String orderByColumn
                                                   ) {
        int offset = page * size;

        StringBuilder queryBuilder = builder(baseQuery, queryParams);
        queryBuilder.append(" ORDER BY ").append(orderByColumn);
        queryBuilder.append(" LIMIT ").append(size);
        queryBuilder.append(" OFFSET ").append(offset);

        List<T> content = jdbcTemplate.query(queryBuilder.toString(),
                                             rowMapper,
                                             queryParams.params().toArray());

        // Підрахунок загальної кількості елементів
        StringBuilder countQueryBuilder = builder(countQuery, queryParams);
        Long totalElements =
                jdbcTemplate.queryForObject(countQueryBuilder.toString(), Long.class, queryParams.params().toArray());
        return new PageResult<>(content, page, size, totalElements);
    }
}
