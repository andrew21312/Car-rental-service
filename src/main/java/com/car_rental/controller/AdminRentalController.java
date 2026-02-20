package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Rental;
import com.car_rental.entity.RentalExtra;
import com.car_rental.entity.RentalStatuses;
import com.car_rental.form.rental.RentalStatusUpdateDTO;
import com.car_rental.service.RentalExtraService;
import com.car_rental.service.RentalService;
import com.car_rental.service.RentalStatusService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
public class AdminRentalController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRentalController.class);

    // View paths
    private static final String RENTALS_PAGE = "adminRental/rentalsPage";
    private static final String VIEW_RENTAL_PAGE = "adminRental/viewRentalPage";
    private static final String RENTAL_EXTRAS_PAGE = "adminRental/rentalExtrasPage";

    // Redirect paths
    private static final String REDIRECT_TO_ALL_RENTALS = "redirect:/rentals";
    private static final String REDIRECT_TO_RENTAL_VIEW_PAGE = "redirect:/viewRental?id=";
    private static final String REDIRECT_TO_RENTAL_EXTRAS_PAGE = "redirect:/extras";

    // Model attributes
    private static final String RENTAL = "rental";

    private final RentalService rentalService;
    private final RentalStatusService rentalStatusService;
    private final RentalExtraService rentalExtraService;

    public AdminRentalController(RentalService rentalService,
                                 RentalStatusService rentalStatusService,
                                 RentalExtraService rentalExtraService) {
        this.rentalService = rentalService;
        this.rentalStatusService = rentalStatusService;
        this.rentalExtraService = rentalExtraService;
    }

    // ---------- Model Attributes ----------

    @ModelAttribute("rentalStatuses")
    public List<RentalStatuses> rentalStatuses() {
        return rentalStatusService.getStatusList();
    }

    @ModelAttribute("rentalExtras")
    public List<RentalExtra> rentalExtras() {
        return rentalExtraService.getExtrasList();
    }

    @ModelAttribute("rentalPaymentStatuses")
    public List<RentalStatusUpdateDTO.PaymentStatus> rentalPaymentStatuses() {
        return Arrays.asList(RentalStatusUpdateDTO.PaymentStatus.values());
    }

    // ---------- Rental Management Endpoints ----------

    @GetMapping("/rentals")
    public String getAllRentals(@RequestParam(required = false) Integer statusId,
                                @RequestParam(required = false) Integer rentalId,
                                @RequestParam(required = false) String carPlateNumber,
                                @RequestParam(required = false) String clientLastName,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(defaultValue = "createdAtDesc") String sortBy,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            PageResult<Rental> rentalPage = rentalService.getRentalsPage(
                    statusId, rentalId, carPlateNumber, clientLastName, page, pageSize, sortBy);

            model.addAttribute(PAGE, rentalPage);
            model.addAttribute("rentalIdSearch", rentalId);
            model.addAttribute("carPlateNumberSearch", carPlateNumber);
            model.addAttribute("clientLastNameSearch", clientLastName);
            model.addAttribute("statusId", statusId);
            model.addAttribute("sortBy", sortBy);

            return RENTALS_PAGE;
        } catch (Exception e) {
            logger.error("Error retrieving rentals: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving rentals: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @GetMapping("/viewRental")
    public String viewRental(@RequestParam int id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.getRentalById(id);
            model.addAttribute(RENTAL, rental);
            model.addAttribute("rentalDTO", new RentalStatusUpdateDTO(rental));
            return VIEW_RENTAL_PAGE;
        } catch (Exception e) {
            logger.error("Error retrieving rental with ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving rental: " + e.getMessage());
            return REDIRECT_TO_ALL_RENTALS;
        }
    }

    @PostMapping("/updateRental")
    public String updateRental(@RequestParam int id,
                               @Valid @ModelAttribute("rentalDTO") RentalStatusUpdateDTO rentalStatusUpdateDTO,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            reloadRentalInModel(id, model);
            return VIEW_RENTAL_PAGE;
        }

        try {
            Rental rentalToUpdate = new Rental(rentalStatusUpdateDTO);
            rentalToUpdate.setId(id);
            rentalService.updateRentalStatus(rentalToUpdate, rentalStatusUpdateDTO.getRentalStatusName());
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Rental updated successfully.");
            return REDIRECT_TO_RENTAL_VIEW_PAGE + id;
        } catch (Exception e) {
            logger.error("Error updating rental with ID {}: {}", id, e.getMessage(), e);
            reloadRentalInModel(id, model);
            model.addAttribute(ERROR_MSG, "Error updating rental: " + e.getMessage());
            return VIEW_RENTAL_PAGE;
        }
    }

    @PostMapping("/updateRentalStatus")
    public String updateRentalStatus(@RequestParam int rentalId,
                                     @RequestParam String newStatus,
                                     RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.getRentalById(rentalId);

            if (!rentalService.isValidStatusTransition(rental.getRentalStatusName(), newStatus)) {
                redirectAttributes.addFlashAttribute(ERROR_MSG,
                                                     String.format("Invalid status transition from %s to %s.",
                                                                   rental.getRentalStatusName(), newStatus));
                return REDIRECT_TO_RENTAL_VIEW_PAGE + rentalId;
            }

            rentalService.updateRentalStatus(rental, newStatus);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Status updated successfully.");
            return REDIRECT_TO_RENTAL_VIEW_PAGE + rentalId;
        } catch (Exception e) {
            logger.error("Error updating rental status for ID {}: {}", rentalId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error updating rental status: " + e.getMessage());
            return REDIRECT_TO_ALL_RENTALS;
        }
    }

    @PostMapping("/updateRentalPaymentStatus")
    public String updateRentalPaymentStatus(@RequestParam int rentalId,
                                            @RequestParam String newPaymentStatus,
                                            RedirectAttributes redirectAttributes) {
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            rental.setPaymentStatus(RentalStatusUpdateDTO.PaymentStatus.valueOf(newPaymentStatus));
            rentalService.updateRentalPaymentStatus(rental);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Payment status updated successfully.");
            return REDIRECT_TO_RENTAL_VIEW_PAGE + rentalId;
        } catch (Exception e) {
            logger.error("Error updating payment status for rental ID {}: {}", rentalId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error updating rental payment status: " + e.getMessage());
            return REDIRECT_TO_ALL_RENTALS;
        }
    }

    @PostMapping("/updateRentalExtras")
    public String updateRentalExtras(@RequestParam int rentalId,
                                     @RequestParam(value = "selectedExtras", required = false)
                                     List<Integer> selectedExtras,
                                     RedirectAttributes redirectAttributes) {
        try {
            rentalService.updateRentalExtras(rentalId, selectedExtras);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Extras updated successfully.");
            return REDIRECT_TO_RENTAL_VIEW_PAGE + rentalId;
        } catch (Exception e) {
            logger.error("Error updating extras for rental ID {}: {}", rentalId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error updating rental extras: " + e.getMessage());
            return REDIRECT_TO_ALL_RENTALS;
        }
    }

    // ---------- Rental Extras Management Endpoints ----------

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping("/extras")
    public String getRentalExtras(@RequestParam(required = false) String searchQuery,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            PageResult<RentalExtra> extrasPage = rentalExtraService.getExtrasPage(searchQuery, page, pageSize);
            model.addAttribute(PAGE, extrasPage);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("extraModel", new RentalExtra());
            return RENTAL_EXTRAS_PAGE;
        } catch (Exception e) {
            logger.error("Error retrieving rental extras: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving rental extras: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @PostAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/addExtras")
    public String addRentalExtra(@ModelAttribute @Valid RentalExtra extra,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute(ERROR_MSG,
                                                     "Error adding extra: " +
                                                     bindingResult.getAllErrors().getFirst().getDefaultMessage());
                return REDIRECT_TO_RENTAL_EXTRAS_PAGE;
            }

            rentalExtraService.addExtra(extra);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG,
                                                 String.format("Extra '%s' added successfully.", extra.getName()));
        } catch (Exception e) {
            logger.error("Error adding rental extra: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error adding rental extra: " + e.getMessage());
        }
        return REDIRECT_TO_RENTAL_EXTRAS_PAGE;
    }

    @PostAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/updateExtras")
    public String updateRentalExtra(@ModelAttribute @Valid RentalExtra extra,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute(ERROR_MSG,
                                                     "Error updating extra: " +
                                                     bindingResult.getAllErrors().getFirst().getDefaultMessage());
                return REDIRECT_TO_RENTAL_EXTRAS_PAGE;
            }

            rentalExtraService.updateExtra(extra);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG,
                                                 String.format("Extra '%s' updated successfully.", extra.getName()));
        } catch (Exception e) {
            logger.error("Error updating rental extra: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error updating rental extra: " + e.getMessage());
        }
        return REDIRECT_TO_RENTAL_EXTRAS_PAGE;
    }

    @PostAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/deleteExtras")
    public String deleteRentalExtra(@RequestParam int id, RedirectAttributes redirectAttributes) {
        try {
            rentalExtraService.deleteExtra(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Extra deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting rental extra with ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error deleting extra: " + e.getMessage());
        }
        return REDIRECT_TO_RENTAL_EXTRAS_PAGE;
    }

    // ---------- Private Helper Methods ----------

    private void reloadRentalInModel(int rentalId, Model model) {
        Rental rental = rentalService.getRentalById(rentalId);
        model.addAttribute(RENTAL, rental);
    }
}