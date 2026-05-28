package com.car_rental.dao.mapper;

import com.car_rental.entity.CarModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class CarModelRowMapper implements RowMapper<CarModel> {
    @Override
    public CarModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        CarModel carModel = new CarModel();
        carModel.setId(rs.getInt("car_model_id"));
        carModel.setPrice(rs.getDouble("car_model_price"));
        carModel.setBrand(rs.getString("car_model_brand"));
        carModel.setModel(rs.getString("car_model_name"));
        carModel.setYear(rs.getInt("car_model_year"));
        carModel.setEngineType(rs.getString("car_model_engine_type"));
        carModel.setSeats(rs.getInt("car_model_seats"));
        carModel.setTransmission(rs.getString("car_model_transmission"));
        return carModel;
    }
}
