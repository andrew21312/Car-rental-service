package com.car_rental.dao.mapper;

import com.car_rental.entity.CarStatuses;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class CarStatusesRowMapper implements RowMapper<CarStatuses> {
    @Override
    public CarStatuses mapRow(ResultSet rs, int rowNum) throws SQLException {
        CarStatuses carStatuses = new CarStatuses();
        carStatuses.setId(rs.getInt("car_status_id"));
        carStatuses.setCarStatusName(rs.getString("car_status_name"));
        return carStatuses;
    }
}
