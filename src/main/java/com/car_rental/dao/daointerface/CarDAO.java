package com.car_rental.dao.daointerface;

import com.car_rental.entity.Car;
import com.car_rental.entity.PageResult;
import com.car_rental.form.car.CarPreparationDTO;
import com.car_rental.service.RentalStatusService;
import java.util.List;

public interface CarDAO {
    void addCar(Car car);

    void updateCar(Car car);

    void deleteCar(int id);

    Car getCarById(int id);

    CarPreparationDTO getCarToPrepare(int rentalId, RentalStatusService statusService);

    PageResult<Car> getCarsPage(Integer modelId, Integer statusId, Integer carId, String carPlateNumber, int page,
                                int size, String sortBy);

    PageResult<CarPreparationDTO> getCarsToPreparePage(int page, int size, String statusFilter,
                                                       RentalStatusService statusService);

    void prepareCar(int approvedId, int rentalId);

    List<Car> getCarsByModelId(int modelId);

    List<Car> getAvailableCarsByModelId(int modelId);
}
