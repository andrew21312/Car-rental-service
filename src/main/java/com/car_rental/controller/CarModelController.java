package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.CarModel;
import com.car_rental.entity.PageResult;
import com.car_rental.service.CarModelService;
import jakarta.validation.Valid;

import java.util.Arrays;
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
public class CarModelController {

    private static final Logger logger = LoggerFactory.getLogger(CarModelController.class);

    private static final String ALL_CAR_MODELS_VIEW = "fleetManagerCarModel/carModelsPage";
    private static final String ADD_CAR_MODEL_VIEW = "fleetManagerCarModel/addCarModelPage";
    private static final String UPDATE_CAR_MODEL_VIEW = "fleetManagerCarModel/updateCarModelPage";
    private static final String REDIRECT_TO_ALL_CAR_MODELS = "redirect:/carModels";
    private static final String CAR_MODEL = "carModel";

    private final CarModelService carModelService;

    public CarModelController(CarModelService carModelService) {
        this.carModelService = carModelService;
    }

    @ModelAttribute("allCarModels")
    public List<CarModel> populateCarModels() {
        return carModelService.getAllCarModels();
    }

    @ModelAttribute("engineTypes")
    public List<CarModel.EngineType> populateEngineTypes() {
        return Arrays.asList(CarModel.EngineType.values());
    }

    @ModelAttribute("seatsTypes")
    public List<Integer> populateSeats() {
        return carModelService.getAvailableSeatCounts();
    }

    @ModelAttribute("transmissionTypes")
    public List<CarModel.Transmission> populateTransmissionTypes() {
        return Arrays.asList(CarModel.Transmission.values());
    }

    @RequestMapping(value = "/carModels", method = {RequestMethod.GET, RequestMethod.POST})
    public String getAllCarModels(@RequestParam(required = false) String engineType,
                                  @RequestParam(required = false) Integer modelId,
                                  @RequestParam(required = false) String modelName,
                                  @RequestParam(required = false) Integer seats,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(defaultValue = "modelIdAsc") String sortBy,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            PageResult<CarModel> carModelPage = carModelService.getCarModelsPage(
                    engineType, modelId, modelName, seats, page, pageSize, sortBy);

            model.addAttribute(PAGE, carModelPage);
            model.addAttribute("modelIdSearch", modelId);
            model.addAttribute("modelNameSearch", modelName);
            model.addAttribute("engineType", engineType);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("seats", seats);

            return ALL_CAR_MODELS_VIEW;
        } catch (Exception e) {
            logger.error("Error retrieving car models: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving car models: " + e.getMessage());
            return REDIRECT_TO_ALL_CAR_MODELS;
        }
    }

    @GetMapping("/addCarModel")
    public String showAddCarModelForm(Model model) {
        model.addAttribute(CAR_MODEL, new CarModel());
        return ADD_CAR_MODEL_VIEW;
    }

    @PostMapping("/addCarModel")
    public String addCarModel(@ModelAttribute(CAR_MODEL) @Valid CarModel carModel,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return ADD_CAR_MODEL_VIEW;
        }

        try {
            carModelService.addCarModel(carModel);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car model added successfully.");
            return REDIRECT_TO_ALL_CAR_MODELS;
        } catch (Exception e) {
            logger.error("Error adding car model: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, "Error adding car model: " + e.getMessage());
            return ADD_CAR_MODEL_VIEW;
        }
    }

    @GetMapping("/updateCarModel")
    public String showUpdateCarModelForm(@RequestParam int id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            CarModel carModel = carModelService.getCarModelById(id);
            if (carModel == null) {
                redirectAttributes.addFlashAttribute(ERROR_MSG, "Car model not found for ID: " + id);
                return REDIRECT_TO_ALL_CAR_MODELS;
            }

            model.addAttribute(CAR_MODEL, carModel);
            return UPDATE_CAR_MODEL_VIEW;
        } catch (Exception e) {
            logger.error("Error retrieving car model: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving car model: " + e.getMessage());
            return REDIRECT_TO_ALL_CAR_MODELS;
        }
    }

    @PostMapping("/updateCarModel")
    public String updateCarModel(@ModelAttribute(CAR_MODEL) @Valid CarModel carModel,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return UPDATE_CAR_MODEL_VIEW;
        }

        try {
            carModelService.updateCarModel(carModel);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car model updated successfully.");
            return REDIRECT_TO_ALL_CAR_MODELS;
        } catch (Exception e) {
            logger.error("Error updating car model: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, "Error updating car model: " + e.getMessage());
            return UPDATE_CAR_MODEL_VIEW;
        }
    }

    @PostMapping("/deleteCarModel")
    public String deleteCarModel(@RequestParam int id, RedirectAttributes redirectAttributes) {
        try {
            carModelService.deleteCarModel(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car model deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting car model: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error deleting car model: " + e.getMessage());
        }
        return REDIRECT_TO_ALL_CAR_MODELS;
    }
}