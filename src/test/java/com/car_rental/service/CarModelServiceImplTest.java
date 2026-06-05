package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.CarModelDAO;
import com.car_rental.entity.CarModel;
import com.car_rental.entity.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarModelServiceImplTest {

    private final CarModelDAO carModelDAO = mock(CarModelDAO.class);
    private final LogService logService = mock(LogService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private final CarModelServiceImpl service =
            new CarModelServiceImpl(carModelDAO, logService, currentUserService);

    @Test
    void getCarModelsPage_ShouldSearchByModelName() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                null, null, "Toyota", null, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                null, null, "Toyota", null, 0, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                null, null, "Toyota", null, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldSearchByEngineType() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                "PETROL", null, null, null, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                "PETROL", null, null, null, 0, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                "PETROL", null, null, null, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldSearchBySeats() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                null, null, null, 5, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                null, null, null, 5, 0, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                null, null, null, 5, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldSearchByModelNameEngineTypeAndSeatsTogether() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                "DIESEL", null, "BMW", 5, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                "DIESEL", null, "BMW", 5, 0, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                "DIESEL", null, "BMW", 5, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldReturnEmptyPage_WhenNoVehiclesFound() {
        PageResult<CarModel> emptyPage = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                null, null, "UnknownModel", null, 0, 10, "modelIdAsc"
        )).thenReturn(emptyPage);

        PageResult<CarModel> result = service.getCarModelsPage(
                null, null, "UnknownModel", null, 0, 10, "modelIdAsc"
        );

        assertEquals(emptyPage, result);
        assertTrue(result.getContent().isEmpty());

        verify(carModelDAO).getCarModelsPage(
                null, null, "UnknownModel", null, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldThrowDataAccessException_WhenSearchFails() {
        when(carModelDAO.getCarModelsPage(
                null, null, "Toyota", null, 0, 10, "modelIdAsc"
        )).thenThrow(new RuntimeException("DB error"));

        assertThrows(DataAccessException.class, () ->
                service.getCarModelsPage(
                        null, null, "Toyota", null, 0, 10, "modelIdAsc"
                )
        );
    }
    @Test
    void getCarModelsPage_ShouldFilterByModelId() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                null, 3, null, null, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                null, 3, null, null, 0, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                null, 3, null, null, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldFilterByEngineTypeAndModelId() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                "ELECTRIC", 4, null, null, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                "ELECTRIC", 4, null, null, 0, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                "ELECTRIC", 4, null, null, 0, 10, "modelIdAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldFilterByAllAvailableCriteria() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                "HYBRID", 2, "Prius", 5, 1, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                "HYBRID", 2, "Prius", 5, 1, 10, "modelIdAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                "HYBRID", 2, "Prius", 5, 1, 10, "modelIdAsc"
        );
    }
    @Test
    void getCarModelsPage_ShouldSortByPriceAscending() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                null, null, null, null, 0, 10, "priceAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                null, null, null, null, 0, 10, "priceAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                null, null, null, null, 0, 10, "priceAsc"
        );
    }

    @Test
    void getCarModelsPage_ShouldSortByPriceDescending() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                null, null, null, null, 0, 10, "priceDesc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                null, null, null, null, 0, 10, "priceDesc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                null, null, null, null, 0, 10, "priceDesc"
        );
    }

    @Test
    void getCarModelsPage_ShouldSortTogetherWithFilters() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelDAO.getCarModelsPage(
                "HYBRID", 2, "Prius", 5, 0, 10, "priceAsc"
        )).thenReturn(pageResult);

        PageResult<CarModel> result = service.getCarModelsPage(
                "HYBRID", 2, "Prius", 5, 0, 10, "priceAsc"
        );

        assertEquals(pageResult, result);

        verify(carModelDAO).getCarModelsPage(
                "HYBRID", 2, "Prius", 5, 0, 10, "priceAsc"
        );
    }
}