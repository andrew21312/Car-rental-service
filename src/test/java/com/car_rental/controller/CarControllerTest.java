package com.car_rental.controller;

import com.car_rental.entity.Car;
import com.car_rental.entity.CarModel;
import com.car_rental.entity.PageResult;
import com.car_rental.service.CarModelService;
import com.car_rental.service.CarService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.car_rental.constants.ControllerConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CarControllerTest {

    private final CarService carService = mock(CarService.class);
    private final CarModelService carModelService = mock(CarModelService.class);
    private final Model model = mock(Model.class);
    private final BindingResult bindingResult = mock(BindingResult.class);
    private final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

    private final CarController controller =
            new CarController(carService, carModelService);


    @Test
    void populateAllCarModels_ShouldReturnCarModels() {
        List<CarModel> models = List.of(new CarModel());

        when(carModelService.getAllCarModels()).thenReturn(models);

        List<CarModel> result = controller.populateAllCarModels();

        assertEquals(models, result);
        verify(carModelService).getAllCarModels();
    }

    @Test
    void getAllCars_ShouldReturnCarsPage_WhenServiceWorks() {
        PageResult<Car> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(carService.getCarsPage(1, 2, 3, "AA", 0, 10, "carIdAsc"))
                .thenReturn(pageResult);

        String result = controller.getAllCars(
                1,
                "AA",
                3,
                2,
                0,
                10,
                "carIdAsc",
                model,
                redirectAttributes
        );

        assertEquals("fleetManagerCar/carsPage", result);

        verify(model).addAttribute(PAGE, pageResult);
        verify(model).addAttribute("carIdSearch", 3);
        verify(model).addAttribute("carPlateNumberSearch", "AA");
        verify(model).addAttribute("statusId", 2);
        verify(model).addAttribute("modelId", 1);
        verify(model).addAttribute("sortBy", "carIdAsc");
    }

    @Test
    void getAllCars_ShouldRedirectToMainPage_WhenServiceThrowsException() {
        when(carService.getCarsPage(null, null, null, null, 0, 10, "carIdAsc"))
                .thenThrow(new RuntimeException("DB error"));

        String result = controller.getAllCars(
                null,
                null,
                null,
                null,
                0,
                10,
                "carIdAsc",
                model,
                redirectAttributes
        );

        assertEquals(REDIRECT_TO_MAIN_PAGE, result);
        verify(redirectAttributes).addFlashAttribute(
                ERROR_MSG,
                "Error retrieving cars: DB error"
        );
    }

    @Test
    void showAddCarForm_ShouldReturnAddCarViewAndAddCarToModel() {
        String result = controller.showAddCarForm(model);

        assertEquals("fleetManagerCar/addCarPage", result);
        verify(model).addAttribute(eq("car"), any(Car.class));
    }

    @Test
    void addCar_ShouldAddCarAndRedirect_WhenDataIsValid() {
        Car car = new Car();
        car.setPlateNumber("AA1234BB");

        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.addCar(car, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/cars", result);
        verify(carService).addCar(car);
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Car added successfully."
        );
    }

    @Test
    void addCar_ShouldReturnAddCarView_WhenValidationHasErrors() {
        Car car = new Car();

        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.addCar(car, bindingResult, model, redirectAttributes);

        assertEquals("fleetManagerCar/addCarPage", result);
        verify(carService, never()).addCar(any(Car.class));
    }

    @Test
    void addCar_ShouldReturnAddCarView_WhenServiceThrowsException() {
        Car car = new Car();
        car.setPlateNumber("AA1234BB");

        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Plate already exists")).when(carService).addCar(car);

        String result = controller.addCar(car, bindingResult, model, redirectAttributes);

        assertEquals("fleetManagerCar/addCarPage", result);
        verify(model).addAttribute(ERROR_MSG, "Plate already exists");
    }

    @Test
    void showManageCarForm_ShouldReturnManageCarView_WhenCarExists() {
        Car car = new Car();
        car.setId(1);

        when(carService.getCarById(1)).thenReturn(car);

        String result = controller.showManageCarForm(1, model, redirectAttributes);

        assertEquals("fleetManagerCar/manageCarPage", result);
        verify(model).addAttribute("car", car);
    }

    @Test
    void showManageCarForm_ShouldRedirectToCars_WhenServiceThrowsException() {
        when(carService.getCarById(1)).thenThrow(new RuntimeException("Car not found"));

        String result = controller.showManageCarForm(1, model, redirectAttributes);

        assertEquals("redirect:/cars", result);
        verify(redirectAttributes).addFlashAttribute(
                ERROR_MSG,
                "Error retrieving car: Car not found"
        );
    }

    @Test
    void updateCar_ShouldUpdateCarAndRedirect_WhenDataIsValid() {
        Car car = new Car();
        car.setId(1);
        car.setPlateNumber("AA1234BB");

        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.updateCar(car, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/cars", result);
        verify(carService).updateCar(car);
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Car updated successfully."
        );
    }

    @Test
    void updateCar_ShouldReturnManageCarView_WhenValidationHasErrors() {
        Car car = new Car();
        car.setId(1);

        Car reloadedCar = new Car();
        reloadedCar.setId(1);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(carService.getCarById(1)).thenReturn(reloadedCar);

        String result = controller.updateCar(car, bindingResult, model, redirectAttributes);

        assertEquals("fleetManagerCar/manageCarPage", result);
        verify(model).addAttribute("car", reloadedCar);
        verify(carService, never()).updateCar(any(Car.class));
    }

    @Test
    void updateCar_ShouldReturnManageCarView_WhenServiceThrowsException() {
        Car car = new Car();
        car.setId(1);

        Car reloadedCar = new Car();
        reloadedCar.setId(1);

        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Update error")).when(carService).updateCar(car);
        when(carService.getCarById(1)).thenReturn(reloadedCar);

        String result = controller.updateCar(car, bindingResult, model, redirectAttributes);

        assertEquals("fleetManagerCar/manageCarPage", result);
        verify(model).addAttribute(ERROR_MSG, "Error updating car: Update error");
        verify(model).addAttribute("car", reloadedCar);
    }

    @Test
    void deleteCar_ShouldDeleteCarAndRedirect_WhenServiceWorks() {
        String result = controller.deleteCar(1, redirectAttributes);

        assertEquals("redirect:/cars", result);
        verify(carService).deleteCar(1);
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Car deleted successfully."
        );
    }

    @Test
    void deleteCar_ShouldRedirectWithError_WhenServiceThrowsException() {
        doThrow(new RuntimeException("Delete error")).when(carService).deleteCar(1);

        String result = controller.deleteCar(1, redirectAttributes);

        assertEquals("redirect:/cars", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MSG, "Delete error");
    }
}