package com.car_rental.dao.mapper;

import com.car_rental.entity.RentalExtra;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class RentalExtraRowMapper implements RowMapper<RentalExtra> {

    @Override
    public RentalExtra mapRow(ResultSet rs, int rowNum) throws SQLException {
        RentalExtra extra = new RentalExtra();
        extra.setId(rs.getInt("extra_id"));
        extra.setName(rs.getString("extra_name"));
        extra.setPrice(rs.getDouble("extra_price"));
        return extra;
    }
}
