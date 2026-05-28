package com.car_rental.dao.mapper;

import com.car_rental.entity.Car;
import com.car_rental.entity.CarModel;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CarRowMapper implements RowMapper<Car> {
    private final CarModelRowMapper carModelRowMapper;

    public CarRowMapper(CarModelRowMapper carModelRowMapper) {
        this.carModelRowMapper = carModelRowMapper;
    }

    @Override
    public Car mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Car car = new Car();

        CarModel carModel = carModelRowMapper.mapRow(rs, rowNum);
        car.setModel(carModel);

        car.setId(rs.getInt("car_id"));
        car.setPlateNumber(rs.getString("car_plate_number"));
        car.setColor(rs.getString("car_color"));

        return car;
    }
}
