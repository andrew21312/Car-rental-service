package com.car_rental.dao.mapper;

import com.car_rental.entity.Rental;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CarBlockedDatesRowMapper implements RowMapper<Rental> {

    @Override
    public Rental mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Rental rental = new Rental();
        rental.setCarId(rs.getInt("rental_car_id"));
        rental.setStartDate(rs.getDate("rental_start_date").toLocalDate());
        rental.setEndDate(rs.getDate("rental_end_date").toLocalDate());
        return rental;
    }
}
