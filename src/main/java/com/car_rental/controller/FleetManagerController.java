package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.Car;
import com.car_rental.entity.PageResult;
import com.car_rental.form.car.CarIssueReport;
import com.car_rental.form.car.CarPreparationDTO;
import com.car_rental.service.FleetManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAuthority('FLEET_MANAGER')")
public class FleetManagerController {

    private static final Logger logger = LoggerFactory.getLogger(FleetManagerController.class);

    private static final String CARS_TO_PREPARE = "fleetManagerCar/carsToPrepare";
    private static final String CARS_ISSUES = "fleetManagerCar/carsIssues";
    private static final String REDIRECT_TO_CARS_TO_PREPARE = "redirect:/carsToPrepare";
    private static final String REDIRECT_TO_CARS_ISSUES = "redirect:/carsIssues";

    private final FleetManagerService fleetManagerService;

    public FleetManagerController(FleetManagerService fleetManagerService) {
        this.fleetManagerService = fleetManagerService;
    }

    @GetMapping("/carsToPrepare")
    public String getCarsToPrepare(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String status,
                                   Model model) {
        if (status != null && !status.equals("all") &&
            !status.equals(APPROVED) &&
            !status.equals(READY_FOR_PICKUP)) {
            status = "all";
        }

        PageResult<CarPreparationDTO> carsPage = fleetManagerService.getCarsToPreparePage(page, size, status);
        model.addAttribute("page", carsPage);
        model.addAttribute("currentStatus", status != null ? status : "all");
        model.addAttribute("statuses", fleetManagerService.getPreparationStatuses());
        model.addAttribute("ready", READY_FOR_PICKUP);

        return CARS_TO_PREPARE;
    }

    @PostMapping("/reportIssue")
    public String reportIssue(@ModelAttribute("report") CarIssueReport report,
                              @RequestParam(required = false) Integer edit,
                              @RequestParam int carId,
                              RedirectAttributes redirectAttributes) {
        try {
            Car car = new Car();
            car.setId(carId);
            report.setCar(car);
            fleetManagerService.reportCarIssue(report);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Issue reported successfully.");

            if (edit == 1) {
                return REDIRECT_TO_MAIN_PAGE;
            }
        } catch (Exception e) {
            logger.error("Error reporting issue: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error reporting issue: " + e.getMessage());
        }
        return REDIRECT_TO_CARS_TO_PREPARE;
    }

    @PostMapping("/prepareCar")
    public String prepareCar(@RequestParam int rentalId, RedirectAttributes redirectAttributes) {
        try {
            fleetManagerService.prepareCar(rentalId);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Car prepared successfully.");
        } catch (Exception e) {
            logger.error("Error preparing car: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error preparing car: " + e.getMessage());
        }
        return REDIRECT_TO_CARS_TO_PREPARE;
    }

    @GetMapping("/carsIssues")
    public String viewCarsIssues(@RequestParam(required = false) String reportStatus,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            PageResult<CarIssueReport> carIssueReportPage =
                    fleetManagerService.getCarsIssuesPage(reportStatus, page, size);
            model.addAttribute(PAGE, carIssueReportPage);
            model.addAttribute("reportStatus", reportStatus);
            return CARS_ISSUES;
        } catch (Exception e) {
            logger.error("Error retrieving car issues: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @PostMapping("/resolveIssue")
    public String resolveIssue(@RequestParam int reportId, RedirectAttributes redirectAttributes) {
        try {
            fleetManagerService.markResolved(reportId);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Issue resolved successfully.");
        } catch (Exception e) {
            logger.error("Error resolving issue: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error resolving issue: " + e.getMessage());
        }
        return REDIRECT_TO_CARS_ISSUES;
    }
}