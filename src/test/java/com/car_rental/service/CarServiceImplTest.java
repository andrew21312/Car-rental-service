package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.CarDAO;
import com.car_rental.entity.Car;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarServiceImplTest {

    private final CarDAO carDAO = mock(CarDAO.class);
    private final LogService logService = mock(LogService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private final CarServiceImpl carService =
            new CarServiceImpl(carDAO, logService, currentUserService);

    @Test
    void addCar_ShouldSaveCarAndWriteLog_WhenCarIsValid() {
        Car car = new Car();
        car.setPlateNumber("AA1234BB");

        when(currentUserService.getCurrentUserId()).thenReturn(1);

        carService.addCar(car);

        verify(carDAO).addCar(car);
        verify(logService).logEvent(
                1,
                Log.AuditEventType.ADD_CAR,
                "Added car with plate number: AA1234BB"
        );
    }

    @Test
    void addCar_ShouldThrowIllegalArgumentException_WhenCarIsNull() {
        assertThrows(IllegalArgumentException.class, () -> carService.addCar(null));

        verify(carDAO, never()).addCar(any(Car.class));
    }

    @Test
    void addCar_ShouldThrowDataAccessException_WhenDaoThrowsException() {
        Car car = new Car();
        car.setPlateNumber("AA1234BB");

        doThrow(new RuntimeException("DB error")).when(carDAO).addCar(car);

        assertThrows(DataAccessException.class, () -> carService.addCar(car));

        verify(carDAO).addCar(car);
    }

    @Test
    void updateCar_ShouldUpdateCarAndWriteLog_WhenCarIsValid() {
        Car car = new Car();
        car.setPlateNumber("BB5678CC");

        when(currentUserService.getCurrentUserId()).thenReturn(2);

        carService.updateCar(car);

        verify(carDAO).updateCar(car);
        verify(logService).logEvent(
                2,
                Log.AuditEventType.UPDATE_CAR,
                "Updated car with plate number: BB5678CC"
        );
    }

    @Test
    void updateCar_ShouldThrowIllegalArgumentException_WhenCarIsNull() {
        assertThrows(IllegalArgumentException.class, () -> carService.updateCar(null));

        verify(carDAO, never()).updateCar(any(Car.class));
    }

    @Test
    void deleteCar_ShouldDeleteCarAndWriteLog_WhenIdIsValid() {
        when(currentUserService.getCurrentUserId()).thenReturn(3);

        carService.deleteCar(10);

        verify(carDAO).deleteCar(10);
        verify(logService).logEvent(
                3,
                Log.AuditEventType.DELETE_CAR,
                "Deleted car with ID: 10"
        );
    }

    @Test
    void deleteCar_ShouldThrowIllegalArgumentException_WhenIdIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> carService.deleteCar(0));

        verify(carDAO, never()).deleteCar(anyInt());
    }

    @Test
    void getCarById_ShouldReturnCar_WhenIdIsValid() {
        Car car = new Car();
        car.setId(5);

        when(carDAO.getCarById(5)).thenReturn(car);

        Car result = carService.getCarById(5);

        assertEquals(car, result);
        verify(carDAO).getCarById(5);
    }

    @Test
    void getCarById_ShouldThrowIllegalArgumentException_WhenIdIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> carService.getCarById(0));

        verify(carDAO, never()).getCarById(anyInt());
    }

    @Test
    void getCarsPage_ShouldReturnPageResultFromDao() {
        PageResult<Car> expectedPage = new PageResult<>(List.of(), 0, 10, 0);

        when(carDAO.getCarsPage(1, 2, 3, "AA", 0, 10, "carIdAsc"))
                .thenReturn(expectedPage);

        PageResult<Car> result =
                carService.getCarsPage(1, 2, 3, "AA", 0, 10, "carIdAsc");

        assertEquals(expectedPage, result);
        verify(carDAO).getCarsPage(1, 2, 3, "AA", 0, 10, "carIdAsc");
    }

    @Test
    void getAvailableCarsByModelId_ShouldReturnCars_WhenModelIdIsValid() {
        List<Car> cars = List.of(new Car(), new Car());

        when(carDAO.getAvailableCarsByModelId(1)).thenReturn(cars);

        List<Car> result = carService.getAvailableCarsByModelId(1);

        assertEquals(cars, result);
        verify(carDAO).getAvailableCarsByModelId(1);
    }

    @Test
    void getAvailableCarsByModelId_ShouldThrowIllegalArgumentException_WhenModelIdIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> carService.getAvailableCarsByModelId(0));

        verify(carDAO, never()).getAvailableCarsByModelId(anyInt());
    }

}