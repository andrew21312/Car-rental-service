package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.CarModel;
import com.car_rental.entity.PageResult;
import com.car_rental.service.CarModelService;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private static final String MAIN_PAGE = "mainPage";
    private static final String CAR_DETAILS_PAGE = "clientRental/carModelDetailsPage";

    private final CarModelService carModelService;

    @Autowired
    public MainController(CarModelService carModelService) {
        this.carModelService = carModelService;
    }

    @ModelAttribute("engineTypes")
    public List<CarModel.EngineType> populateEngineTypes() {
        return Arrays.asList(CarModel.EngineType.values());
    }

    @ModelAttribute("seatsTypes")
    public List<Integer> populateSeats() {
        return carModelService.getAvailableSeatCounts();
    }

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String showMainPage(@RequestParam(required = false) String engineType,
                               @RequestParam(required = false) String modelQuery,
                               @RequestParam(required = false) Integer seats,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int pageSize,
                               @RequestParam(defaultValue = "priceDesc") String sortBy,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            PageResult<CarModel> carModelPage =
                    carModelService.getAvailableCarModelsPage(engineType, modelQuery, seats, page, pageSize, sortBy);
            model.addAttribute(PAGE, carModelPage);
            model.addAttribute("engineType", engineType);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("modelQuery", modelQuery);
            model.addAttribute("seats", seats);
            return MAIN_PAGE;
        } catch (Exception e) {
            logger.error("Error retrieving car models: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving car models: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @GetMapping("/carDetails/{id}")
    public String showCarDetails(@PathVariable int id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            CarModel carModel = carModelService.getCarModelById(id);
            model.addAttribute("carModel", carModel);
            return CAR_DETAILS_PAGE;
        } catch (Exception e) {
            logger.error("Error retrieving car model: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving car model: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }


}