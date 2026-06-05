package com.car_rental.dao.mapper;

import com.car_rental.entity.RentalStatuses;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class RentalStatusesRowMapper implements RowMapper<RentalStatuses> {
    @Override
    public RentalStatuses mapRow(ResultSet rs, int rowNum) throws SQLException {
        RentalStatuses rentalStatuses = new RentalStatuses();
        rentalStatuses.setId(rs.getInt("rental_status_id"));
        rentalStatuses.setName(rs.getString("rental_status_name"));
        return rentalStatuses;
    }
}
