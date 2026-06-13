package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.dao.DataAccessException;
import com.car_rental.entity.*;
import com.car_rental.form.rental.FavoriteCarModelStat;
import com.car_rental.form.rental.RentalDTO;
import com.car_rental.security.UserDetailsImpl;
import com.car_rental.service.CarService;
import com.car_rental.service.RentalExtraService;
import com.car_rental.service.RentalService;
import com.car_rental.service.UserService;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAuthority('CLIENT')")
public class ClientRentalController {

    private static final Logger logger = LoggerFactory.getLogger(ClientRentalController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String RENTAL_PAGE = "clientRental/rentCarPage";
    private static final String RENTAL_HISTORY_PAGE = "clientRental/rentHistoryPage";
    private static final String REDIRECT_TO_RENTAL_HISTORY_PAGE = "redirect:/rentHistory";

    private final UserService userService;
    private final CarService carService;
    private final RentalService rentalService;
    private final RentalExtraService rentalExtraService;

    public ClientRentalController(UserService userService, CarService carService,
                                  RentalService rentalService, RentalExtraService rentalExtraService) {
        this.userService = userService;
        this.carService = carService;
        this.rentalService = rentalService;
        this.rentalExtraService = rentalExtraService;
    }

    @ModelAttribute("rentalExtras")
    public List<RentalExtra> rentalExtras() {
        return rentalExtraService.getExtrasList();
    }

    @GetMapping("/rentCar")
    public String showRentalForm(@RequestParam int modelId,
                                 @RequestParam(required = false) Integer carId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        List<Car> availableCars = carService.getAvailableCarsByModelId(modelId);
        if (availableCars.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, "No available cars for the selected model.");
            return REDIRECT_TO_MAIN_PAGE;
        }

        Car selectedCar = findSelectedCar(carId, availableCars);
        if (selectedCar == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG,
                    "The selected car is not available. Please choose a different one.");
            return REDIRECT_TO_MAIN_PAGE;
        }

        RentalDTO rentalDTO = new RentalDTO();
        rentalDTO.setCar(selectedCar);

        populateRentalModel(model, selectedCar, rentalDTO, availableCars);
        return RENTAL_PAGE;
    }

    @PostMapping("/rentCar")
    public String processRental(@RequestParam int modelId,
                                @RequestParam int carId,
                                @Valid RentalDTO rentalDTO,
                                BindingResult bindingResult,
                                Model model,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                RedirectAttributes redirectAttributes) {
        Car selectedCar = carService.getCarById(carId);
        rentalDTO.setCar(selectedCar);
        List<Car> availableCars = carService.getAvailableCarsByModelId(modelId);

        LocalDate[] dateRange = parseDateRange(rentalDTO.getDateRange(), bindingResult);
        if (dateRange == null) {
            return handleFormError(model, selectedCar, rentalDTO, availableCars, "Invalid date range format.");
        }

        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        if (bindingResult.hasErrors()) {
            return handleFormError(model, selectedCar, rentalDTO, availableCars,
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if (!rentalService.isCarAvailable(selectedCar.getId(), startDate, endDate)) {
            return handleFormError(model, selectedCar, rentalDTO, availableCars,
                    "The selected car is not available for those dates.");
        }

        try {
            User client = userService.getUserById(userDetails.getId());
            rentalDTO.setClient(client);
            Rental rental = buildRental(rentalDTO, selectedCar, startDate, endDate);
            rentalService.addRental(rental);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Your rental has been processed successfully.");
            return REDIRECT_TO_RENTAL_HISTORY_PAGE;
        } catch (Exception e) {
            logger.error("Failed to process rental: {}", e.getMessage(), e);
            return handleFormError(model, selectedCar, rentalDTO, availableCars,
                    "An error occurred while processing your rental: " + e.getMessage());
        }
    }

    @GetMapping("/rentHistory")
    public String viewRentalHistory(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "all") String filter,
                                    Model model,
                                    RedirectAttributes redirectAttributes,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User client = userService.getUserByUsername(userDetails.getUsername());
            PageResult<Rental> rentalPage = rentalService.getClientRentalsPage(client.getId(), filter, page, size);
            model.addAttribute(PAGE, rentalPage);
            model.addAttribute("filter", filter);
            return RENTAL_HISTORY_PAGE;
        } catch (Exception e) {
            logger.error("Failed to retrieve rentals: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @PostMapping("/cancelRental")
    public String cancelRental(@RequestParam int rentalId,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.getRentalById(rentalId);

            if (!isRentalCancellable(rental.getRentalStatusName())) {
                redirectAttributes.addFlashAttribute(ERROR_MSG, "Order cannot be cancelled at this stage.");
                return REDIRECT_TO_RENTAL_HISTORY_PAGE;
            }

            rentalService.cancelRental(rentalId, userDetails.getId());
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Your order has been cancelled.");
        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error cancelling order: " + e.getMessage());
        }
        return REDIRECT_TO_RENTAL_HISTORY_PAGE;
    }

    private boolean isRentalCancellable(String status) {
        return "PENDING".equalsIgnoreCase(status) ||
                "APPROVED".equalsIgnoreCase(status) ||
                "READY_FOR_PICKUP".equalsIgnoreCase(status);
    }

    private Rental buildRental(RentalDTO rentalDTO, Car selectedCar, LocalDate startDate, LocalDate endDate) {
        Rental rental = new Rental(rentalDTO);

        List<RentalExtra> extras = new ArrayList<>();
        for (Integer extraId : rentalDTO.getSelectedExtraIds()) {
            extras.add(rentalExtraService.getExtraById(extraId));
        }
        rental.setRentalExtras(extras);
        rental.setCarId(selectedCar.getId());

        // Car details snapshot
        rental.setPlateNumber(selectedCar.getPlateNumber());
        rental.setModel(selectedCar.getModel().getModel());
        rental.setBrand(selectedCar.getModel().getBrand());
        rental.setYear(selectedCar.getModel().getYear());
        rental.setPrice(selectedCar.getModel().getPrice());

        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.calculateTotalCost();

        return rental;
    }

    private LocalDate[] parseDateRange(String dateRangeString, BindingResult bindingResult) {
        String[] dateParts = dateRangeString.split(" to ");
        if (dateParts.length != 2) {
            bindingResult.rejectValue("dateRange", "Invalid.format", "Invalid date range format.");
            return null;
        }

        try {
            LocalDate startDate = LocalDate.parse(dateParts[0].trim(), DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(dateParts[1].trim(), DATE_FORMATTER);
            return new LocalDate[]{startDate, endDate};
        } catch (DateTimeParseException e) {
            bindingResult.rejectValue("dateRange", "Invalid.date", "Invalid date format. Expected yyyy-MM-dd.");
            return null;
        }
    }

    private String handleFormError(Model model, Car car, RentalDTO rentalDTO,
                                   List<Car> availableCars, String errorMessage) {
        populateRentalModel(model, car, rentalDTO, availableCars);
        model.addAttribute(ERROR_MSG, errorMessage);
        return RENTAL_PAGE;
    }

    private Car findSelectedCar(Integer carId, List<Car> availableCars) {
        if (carId == null) {
            return availableCars.getFirst();
        }
        return availableCars.stream()
                .filter(car -> car.getId() == carId)
                .findFirst()
                .orElse(null);
    }

    @GetMapping("favorite_cars")
    public String showFavoriteCarModels(Model model) {
        List<FavoriteCarModelStat> favoriteCars = rentalService.getFavoriteCarModelsLastMonth();
        model.addAttribute("favoriteCars", favoriteCars);
        return "clientRental/favoriteCarsPage";
    }

    private void populateRentalModel(Model model, Car selectedCar, RentalDTO rentalDTO, List<Car> availableCars) {
        model.addAttribute("blockedDates", rentalService.getBlockedDatesForCar(selectedCar.getId()));
        model.addAttribute("car", selectedCar);
        model.addAttribute("rental", rentalDTO);
        model.addAttribute("availableCars", availableCars);
        model.addAttribute("carModel", selectedCar.getModel());
    }
}