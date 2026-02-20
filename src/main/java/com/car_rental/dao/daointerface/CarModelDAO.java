package com.car_rental.dao.daointerface;

import com.car_rental.entity.CarModel;
import com.car_rental.entity.PageResult;

import java.util.List;

public interface CarModelDAO {
    void addCarModel(CarModel carModel);

    void updateCarModel(CarModel carModel);

    void deleteCarModel(int id);

    CarModel getCarModelById(int id);

    List<Integer> getDistinctSeatCounts();

    List<CarModel> getAllCarModels();

    PageResult<CarModel> getCarModelsPage(String engineType, Integer modelId, String modelQuery, Integer seats, int page, int size,
                                          String sortBy);

    PageResult<CarModel> getAvailableCarModelsPage(String engineType, String modelQuery, Integer seats, int page, int size, String sortBy);
}
