package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.RentalStatusDAO;
import com.car_rental.entity.RentalStatuses;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MySqlRentalStatusDAO implements RentalStatusDAO {
    private final JdbcTemplate jdbcTemplate;

    public MySqlRentalStatusDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<RentalStatuses> findAllStatuses() {
        return jdbcTemplate.query(
                "SELECT id as rental_status_id, name as rental_status_name FROM rental_statuses",
                (rs, rowNum) -> new RentalStatuses(
                        rs.getInt("rental_status_id"),
                        rs.getString("rental_status_name")
                )
                                 );
    }
}
