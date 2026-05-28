package com.car_rental.service;

import com.car_rental.entity.Car;
import com.car_rental.entity.PageResult;
import java.util.List;

public interface CarService {
    void addCar(Car car);

    void updateCar(Car car);

    void deleteCar(int id);

    Car getCarById(int id);

    PageResult<Car> getCarsPage(Integer modelId, Integer statusId, Integer carId, String carPlateNumber, int page,
                                int size, String sortBy);

    List<Car> getAvailableCarsByModelId(int modelId);
}
