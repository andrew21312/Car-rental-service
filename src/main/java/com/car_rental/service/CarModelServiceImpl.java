package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.CarModelDAO;
import com.car_rental.entity.CarModel;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of CarModelService providing CRUD operations for car models.
 */
@Service
public class CarModelServiceImpl implements CarModelService {
    private static final Logger log = LoggerFactory.getLogger(CarModelServiceImpl.class);
    private final CarModelDAO carModelDao;
    private final LogService logService;
    private final CurrentUserService currentUserService;
    private final UtilityService utilityService = new UtilityService();

    @Autowired
    public CarModelServiceImpl(CarModelDAO carModelDao, LogService logService, CurrentUserService currentUserService) {
        this.carModelDao = Objects.requireNonNull(carModelDao, "CarModelDAO cannot be null");
        this.logService = Objects.requireNonNull(logService, "LogService cannot be null");
        this.currentUserService = Objects.requireNonNull(currentUserService, "CurrentUserService cannot be null");
    }

    /**
     * Adds a new car model to the system.
     *
     * @param carModel The car model to add
     * @throws DataAccessException If there's an error adding the car model
     */
    @Override
    public void addCarModel(CarModel carModel) {
        if (carModel == null) {
            throw new IllegalArgumentException("CarModel cannot be null");
        }

        try {
            carModelDao.addCarModel(carModel);
            logService.logEvent(currentUserService.getCurrentUserId(), Log.AuditEventType.ADD_CAR_MODEL,
                    "Added car model: " + carModel.getCarModelInfo());
        } catch (Exception e) {
            String errorMessage = "Failed to add car model: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Updates an existing car model.
     *
     * @param carModel The car model to update
     * @throws DataAccessException If there's an error updating the car model
     */
    @Override
    public void updateCarModel(CarModel carModel) {
        if (carModel == null) {
            throw new IllegalArgumentException("CarModel cannot be null");
        }

        try {
            carModelDao.updateCarModel(carModel);
            logService.logEvent(currentUserService.getCurrentUserId(),
                    Log.AuditEventType.UPDATE_CAR_MODEL,
                    "Updated car model: " + carModel.getCarModelInfo());
        } catch (Exception e) {
            String errorMessage = "Failed to update car model: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    @Override
    public List<Integer> getAvailableSeatCounts() {
        return carModelDao.getDistinctSeatCounts();
    }


    /**
     * Deletes a car model by its ID.
     *
     * @param id The ID of the car model to delete
     * @throws DataAccessException If there's an error deleting the car model
     */
    @Override
    public void deleteCarModel(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Car model ID must be positive");
        }

        try {
            carModelDao.deleteCarModel(id);
            logService.logEvent(currentUserService.getCurrentUserId(),
                    Log.AuditEventType.DELETE_CAR_MODEL,
                    "Deleted car model with ID: " + id);
        } catch (Exception e) {
            String errorMessage = "Failed to delete car model: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a car model by its ID.
     *
     * @param id The ID of the car model to retrieve
     * @return The car model with the specified ID
     * @throws DataAccessException If there's an error retrieving the car model
     */
    @Override
    public CarModel getCarModelById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Car model ID must be positive");
        }

        try {
            return carModelDao.getCarModelById(id);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve car model with ID " + id + ": " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves all car models.
     *
     * @return List of all car models
     * @throws DataAccessException If there's an error retrieving the car models
     */
    @Override
    public List<CarModel> getAllCarModels() {
        try {
            return carModelDao.getAllCarModels();
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve all car models: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a paginated list of car models based on search criteria.
     *
     * @param engineType Engine type filter
     * @param modelId    Model ID filter
     * @param page       Page number
     * @param size       Page size
     * @param sortBy     Sort field
     * @return Paginated result of car models
     * @throws DataAccessException If there's an error retrieving the car models
     */
    @Override
    public PageResult<CarModel> getCarModelsPage(String engineType, Integer modelId, String modelQuery, Integer seats,
                                                 int page, int size, String sortBy) {
        try {
            return carModelDao.getCarModelsPage(engineType, modelId, modelQuery, seats, page, size, sortBy);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve car models page: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a paginated list of available car models.
     *
     * @param engineType Engine type filter
     * @param page       Page number
     * @param size       Page size
     * @return Paginated result of available car models
     * @throws DataAccessException If there's an error retrieving the car models
     */
    @Override
    public PageResult<CarModel> getAvailableCarModelsPage(String engineType, String modelQuery, Integer seats, int page,
                                                          int size, String sortBy) {
        try {
            return carModelDao.getAvailableCarModelsPage(engineType, modelQuery, seats, page, size, sortBy);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve available car models page: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }
}
