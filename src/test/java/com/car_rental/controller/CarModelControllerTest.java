package com.car_rental.controller;

import com.car_rental.entity.CarModel;
import com.car_rental.entity.PageResult;
import com.car_rental.service.CarModelService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.car_rental.constants.ControllerConstants.PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CarModelControllerTest {

    private final CarModelService carModelService = mock(CarModelService.class);
    private final Model model = mock(Model.class);
    private final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

    private final CarModelController controller =
            new CarModelController(carModelService);

    @Test
    void getAllCarModels_ShouldSearchByModelName() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelService.getCarModelsPage(
                null, null, "Toyota", null, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        String result = controller.getAllCarModels(
                null, null, "Toyota", null, 0, 10, "modelIdAsc",
                model, redirectAttributes
        );

        assertEquals("fleetManagerCarModel/carModelsPage", result);

        verify(carModelService).getCarModelsPage(
                null, null, "Toyota", null, 0, 10, "modelIdAsc"
        );
        verify(model).addAttribute(PAGE, pageResult);
        verify(model).addAttribute("modelNameSearch", "Toyota");
    }

    @Test
    void getAllCarModels_ShouldSearchByEngineType() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelService.getCarModelsPage(
                "PETROL", null, null, null, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        String result = controller.getAllCarModels(
                "PETROL", null, null, null, 0, 10, "modelIdAsc",
                model, redirectAttributes
        );

        assertEquals("fleetManagerCarModel/carModelsPage", result);

        verify(carModelService).getCarModelsPage(
                "PETROL", null, null, null, 0, 10, "modelIdAsc"
        );
        verify(model).addAttribute("engineType", "PETROL");
    }

    @Test
    void getAllCarModels_ShouldSearchBySeats() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelService.getCarModelsPage(
                null, null, null, 5, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        String result = controller.getAllCarModels(
                null, null, null, 5, 0, 10, "modelIdAsc",
                model, redirectAttributes
        );

        assertEquals("fleetManagerCarModel/carModelsPage", result);

        verify(carModelService).getCarModelsPage(
                null, null, null, 5, 0, 10, "modelIdAsc"
        );
        verify(model).addAttribute("seats", 5);
    }

    @Test
    void getAllCarModels_ShouldSearchByModelNameEngineTypeAndSeatsTogether() {
        PageResult<CarModel> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelService.getCarModelsPage(
                "DIESEL", null, "BMW", 5, 0, 10, "modelIdAsc"
        )).thenReturn(pageResult);

        String result = controller.getAllCarModels(
                "DIESEL", null, "BMW", 5, 0, 10, "modelIdAsc",
                model, redirectAttributes
        );

        assertEquals("fleetManagerCarModel/carModelsPage", result);

        verify(carModelService).getCarModelsPage(
                "DIESEL", null, "BMW", 5, 0, 10, "modelIdAsc"
        );
        verify(model).addAttribute("modelNameSearch", "BMW");
        verify(model).addAttribute("engineType", "DIESEL");
        verify(model).addAttribute("seats", 5);
    }

    @Test
    void getAllCarModels_ShouldReturnEmptyPage_WhenNoVehiclesFound() {
        PageResult<CarModel> emptyPage = new PageResult<>(List.of(), 0, 10, 0);

        when(carModelService.getCarModelsPage(
                null, null, "UnknownModel", null, 0, 10, "modelIdAsc"
        )).thenReturn(emptyPage);

        String result = controller.getAllCarModels(
                null, null, "UnknownModel", null, 0, 10, "modelIdAsc",
                model, redirectAttributes
        );

        assertEquals("fleetManagerCarModel/carModelsPage", result);

        verify(carModelService).getCarModelsPage(
                null, null, "UnknownModel", null, 0, 10, "modelIdAsc"
        );
        verify(model).addAttribute(PAGE, emptyPage);
        verify(model).addAttribute("modelNameSearch", "UnknownModel");
    }
}