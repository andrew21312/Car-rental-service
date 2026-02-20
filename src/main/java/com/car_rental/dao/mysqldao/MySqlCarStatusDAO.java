package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.CarStatusDAO;
import com.car_rental.entity.CarStatuses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MySqlCarStatusDAO implements CarStatusDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MySqlCarStatusDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CarStatuses> findAllStatuses() {
        return jdbcTemplate.query(
                "SELECT id as car_status_id, name as car_status_name FROM car_statuses",
                (rs, rowNum) -> new CarStatuses(
                        rs.getInt("car_status_id"),
                        rs.getString("car_status_name")
                )
                                 );
    }
}
