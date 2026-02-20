package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.CarDAO;
import com.car_rental.entity.Car;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing car-related operations.
 */
@Service
public class CarServiceImpl implements CarService {
    private static final Logger log = LoggerFactory.getLogger(CarServiceImpl.class);
    private final CarDAO carDao;
    private final LogService logService;
    private final CurrentUserService currentUserService;
    private final UtilityService utilityService = new UtilityService();

    /**
     * Constructor for dependency injection.
     *
     * @param carDao             Car data access object
     * @param logService         Logging service
     * @param currentUserService Current user service
     */
    @Autowired
    public CarServiceImpl(CarDAO carDao, LogService logService, CurrentUserService currentUserService) {
        this.carDao = carDao;
        this.logService = logService;
        this.currentUserService = currentUserService;
    }

    /**
     * Adds a new car to the system.
     *
     * @param car Car object to add
     * @throws DataAccessException if there's an error adding the car
     */
    @Override
    public void addCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null");
        }
        try {
            carDao.addCar(car);
            logService.logEvent(
                    currentUserService.getCurrentUserId(),
                    Log.AuditEventType.ADD_CAR,
                    "Added car with plate number: " + car.getPlateNumber()
                               );
        } catch (Exception e) {
            log.error("Error adding car with plate number {}: {}",
                      car.getPlateNumber(), e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Updates an existing car in the system.
     *
     * @param car Car object to update
     * @throws DataAccessException if there's an error updating the car
     */
    @Override
    public void updateCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null");
        }
        try {
            carDao.updateCar(car);
            logService.logEvent(
                    currentUserService.getCurrentUserId(),
                    Log.AuditEventType.UPDATE_CAR,
                    "Updated car with plate number: " + car.getPlateNumber()
                               );
        } catch (Exception e) {
            log.error("Error updating car with plate number {}: {}",
                      car.getPlateNumber(), e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Deletes a car from the system by its ID.
     *
     * @param carId ID of the car to delete
     * @throws DataAccessException if there's an error deleting the car
     */
    @Override
    public void deleteCar(int carId) {
        if (carId <= 0) {
            throw new IllegalArgumentException("Car ID must be positive");
        }
        try {
            carDao.deleteCar(carId);
            logService.logEvent(
                    currentUserService.getCurrentUserId(),
                    Log.AuditEventType.DELETE_CAR,
                    "Deleted car with ID: " + carId
                               );
        } catch (Exception e) {
            log.error("Error deleting car with ID {}: {}", carId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a car by its ID.
     *
     * @param carId ID of the car to retrieve
     * @return Car object if found, null otherwise
     * @throws DataAccessException if there's an error retrieving the car
     */
    @Override
    public Car getCarById(int carId) {
        if (carId <= 0) {
            throw new IllegalArgumentException("Car ID must be positive");
        }
        try {
            return carDao.getCarById(carId);
        } catch (Exception e) {
            log.error("Error retrieving car with ID {}: {}", carId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a paginated list of cars based on various filters.
     *
     * @param modelId        Optional model ID filter
     * @param statusId       Optional status ID filter
     * @param carId          Optional car ID filter
     * @param carPlateNumber Optional plate number filter
     * @param page           Page number
     * @param size           Page size
     * @param sortBy         Column to sort by
     * @return PageResult containing the filtered cars
     * @throws DataAccessException if there's an error retrieving the cars
     */
    @Override
    public PageResult<Car> getCarsPage(Integer modelId, Integer statusId, Integer carId,
                                       String carPlateNumber, int page, int size, String sortBy) {
        try {
            return carDao.getCarsPage(modelId, statusId, carId, carPlateNumber, page, size, sortBy);
        } catch (Exception e) {
            log.error("Error retrieving cars with filters: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves all available cars for a specific model.
     *
     * @param modelId ID of the car model
     * @return List of available cars
     * @throws DataAccessException if there's an error retrieving the cars
     */
    @Override
    public List<Car> getAvailableCarsByModelId(int modelId) {
        if (modelId <= 0) {
            throw new IllegalArgumentException("Model ID must be positive");
        }
        try {
            return carDao.getAvailableCarsByModelId(modelId);
        } catch (Exception e) {
            log.error("Error retrieving available cars for model {}: {}", modelId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }
}
