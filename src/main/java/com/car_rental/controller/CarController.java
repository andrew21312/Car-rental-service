package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.Car;
import com.car_rental.entity.CarModel;
import com.car_rental.entity.CarStatuses;
import com.car_rental.entity.PageResult;
import com.car_rental.service.CarModelService;
import com.car_rental.service.CarService;
import com.car_rental.service.CarStatusService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAuthority('FLEET_MANAGER')")
public class CarController {

    private static final Logger logger = LoggerFactory.getLogger(CarController.class);

    private static final String ALL_CARS_VIEW = "fleetManagerCar/carsPage";
    private static final String ADD_CAR_VIEW = "fleetManagerCar/addCarPage";
    private static final String MANAGE_CAR_VIEW = "fleetManagerCar/manageCarPage";
    private static final String REDIRECT_TO_ALL_CARS = "redirect:/cars";
    private static final String CAR = "car";

    private final CarService carService;
    private final CarStatusService carStatusService;
    private final CarModelService carModelService;

    public CarController(CarService carService, CarStatusService carStatusService, CarModelService carModelService) {
        this.carService = carService;
        this.carStatusService = carStatusService;
        this.carModelService = carModelService;
    }

    @ModelAttribute("statusTypes")
    public List<CarStatuses> populateStatusTypes() {
        return carStatusService.getStatusList();
    }

    @ModelAttribute("allCarModels")
    public List<CarModel> populateAllCarModels() {
        return carModelService.getAllCarModels();
    }

    @RequestMapping(value = "/cars", method = {RequestMethod.GET, RequestMethod.POST})
    public String getAllCars(@RequestParam(required = false) Integer modelId,
                             @RequestParam(required = false) String carPlateNumber,
                             @RequestParam(required = false) Integer carId,
                             @RequestParam(required = false) Integer statusId,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int pageSize,
                             @RequestParam(defaultValue = "carIdAsc") String sortBy,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            PageResult<Car> carPage = carService.getCarsPage(
                    modelId, statusId, carId, carPlateNumber, page, pageSize, sortBy);

            model.addAttribute(PAGE, carPage);
            model.addAttribute("carIdSearch", carId);
            model.addAttribute("carPlateNumberSearch", carPlateNumber);
            model.addAttribute("statusId", statusId);
            model.addAttribute("modelId", modelId);
            model.addAttribute("sortBy", sortBy);

            return ALL_CARS_VIEW;
        } catch (Exception e) {
            logger.error("Error retrieving cars: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving cars: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @GetMapping("/addCar")
    public String showAddCarForm(Model model) {
        model.addAttribute(CAR, new Car());
        return ADD_CAR_VIEW;
    }

    @PostMapping("/addCar")
    public String addCar(@ModelAttribute(CAR) @Valid Car car,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return ADD_CAR_VIEW;
        }

        try {
            carService.addCar(car);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car added successfully.");
            return REDIRECT_TO_ALL_CARS;
        } catch (Exception e) {
            logger.error("Error adding car: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, e.getMessage());
            return ADD_CAR_VIEW;
        }
    }

    @GetMapping("/manageCar")
    public String showManageCarForm(@RequestParam int id,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            Car car = carService.getCarById(id);
            model.addAttribute(CAR, car);
            return MANAGE_CAR_VIEW;
        } catch (Exception e) {
            logger.error("Error retrieving car: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving car: " + e.getMessage());
            return REDIRECT_TO_ALL_CARS;
        }
    }

    @PostMapping("/updateCar")
    public String updateCar(@ModelAttribute(CAR) @Valid Car car,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return reloadCarForUpdate(car.getId(), model);
        }

        try {
            carService.updateCar(car);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car updated successfully.");
            return REDIRECT_TO_ALL_CARS;
        } catch (Exception e) {
            logger.error("Error updating car: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, "Error updating car: " + e.getMessage());
            return reloadCarForUpdate(car.getId(), model);
        }
    }

    @PostMapping("/deleteCar")
    public String deleteCar(@RequestParam int id, RedirectAttributes redirectAttributes) {
        try {
            carService.deleteCar(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting car: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
        }
        return REDIRECT_TO_ALL_CARS;
    }

    private String reloadCarForUpdate(int carId, Model model) {
        Car car = carService.getCarById(carId);
        model.addAttribute(CAR, car);
        return MANAGE_CAR_VIEW;
    }
}