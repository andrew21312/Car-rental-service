package com.car_rental.controller;

import com.car_rental.dao.DataAccessException;
import com.car_rental.entity.*;
import com.car_rental.form.rental.FavoriteCarModelStat;
import com.car_rental.form.rental.RentalDTO;
import com.car_rental.security.UserDetailsImpl;
import com.car_rental.service.CarService;
import com.car_rental.service.RentalExtraService;
import com.car_rental.service.RentalService;
import com.car_rental.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

import static com.car_rental.constants.ControllerConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ClientRentalControllerTest {

    private final UserService userService = mock(UserService.class);
    private final CarService carService = mock(CarService.class);
    private final RentalService rentalService = mock(RentalService.class);
    private final RentalExtraService rentalExtraService = mock(RentalExtraService.class);
    private final Model model = mock(Model.class);
    private final BindingResult bindingResult = mock(BindingResult.class);
    private final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
    private final UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

    private final ClientRentalController controller =
            new ClientRentalController(userService, carService, rentalService, rentalExtraService);

    private Car createCar() {
        CarModel carModel = new CarModel();
        carModel.setBrand("Toyota");
        carModel.setModel("Camry");
        carModel.setYear(2022);
        carModel.setPrice(50.0);

        Car car = new Car();
        car.setId(1);
        car.setPlateNumber("AA1234BB");
        car.setModel(carModel);
        return car;
    }

    @Test
    void rentalExtras_ShouldReturnExtrasList() {
        List<RentalExtra> extras = List.of(new RentalExtra());

        when(rentalExtraService.getExtrasList()).thenReturn(extras);

        List<RentalExtra> result = controller.rentalExtras();

        assertEquals(extras, result);
        verify(rentalExtraService).getExtrasList();
    }

    @Test
    void showRentalForm_ShouldReturnRentalPage_WhenCarAvailable() {
        Car car = createCar();

        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of(car));
        when(rentalService.getBlockedDatesForCar(1)).thenReturn(List.of());

        String result = controller.showRentalForm(1, null, model, redirectAttributes);

        assertEquals("clientRental/rentCarPage", result);
        verify(model).addAttribute("car", car);
        verify(model).addAttribute(eq("rental"), any(RentalDTO.class));
        verify(model).addAttribute("availableCars", List.of(car));
        verify(model).addAttribute("carModel", car.getModel());
    }

    @Test
    void showRentalForm_ShouldRedirect_WhenNoAvailableCars() {
        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of());

        String result = controller.showRentalForm(1, null, model, redirectAttributes);

        assertEquals(REDIRECT_TO_MAIN_PAGE, result);
        verify(redirectAttributes).addFlashAttribute(
                ERROR_MSG,
                "No available cars for the selected model."
        );
    }

    @Test
    void showRentalForm_ShouldRedirect_WhenSelectedCarIsNotAvailable() {
        Car car = createCar();

        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of(car));

        String result = controller.showRentalForm(1, 99, model, redirectAttributes);

        assertEquals(REDIRECT_TO_MAIN_PAGE, result);
        verify(redirectAttributes).addFlashAttribute(
                ERROR_MSG,
                "The selected car is not available. Please choose a different one."
        );
    }

    @Test
    void processRental_ShouldCreateRentalAndRedirect_WhenDataIsValid() {
        Car car = createCar();
        User user = new User();
        user.setId(5);

        RentalDTO rentalDTO = new RentalDTO();
        rentalDTO.setDateRange("2026-06-10 to 2026-06-13");
        rentalDTO.setSelectedExtraIds(List.of());

        when(carService.getCarById(1)).thenReturn(car);
        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of(car));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(rentalService.isCarAvailable(eq(1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(true);
        when(userDetails.getId()).thenReturn(5);
        when(userService.getUserById(5)).thenReturn(user);

        String result = controller.processRental(
                1,
                1,
                rentalDTO,
                bindingResult,
                model,
                userDetails,
                redirectAttributes
        );

        assertEquals("redirect:/rentHistory", result);
        verify(rentalService).addRental(any(Rental.class));
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Your rental has been processed successfully."
        );
    }
    @Test
    void cancelRental_ShouldCancelRental_WhenStatusIsApproved() {
        Rental rental = mock(Rental.class);
        when(rental.getRentalStatusName()).thenReturn("APPROVED");
        when(userDetails.getId()).thenReturn(5);
        when(rentalService.getRentalById(10)).thenReturn(rental);

        String result = controller.cancelRental(10, userDetails, redirectAttributes);

        assertEquals("redirect:/rentHistory", result);
        verify(rentalService).cancelRental(10, 5);
    }

    @Test
    void cancelRental_ShouldCancelRental_WhenStatusIsReadyForPickup() {
        Rental rental = mock(Rental.class);
        when(rental.getRentalStatusName()).thenReturn("READY_FOR_PICKUP");
        when(userDetails.getId()).thenReturn(5);
        when(rentalService.getRentalById(10)).thenReturn(rental);

        String result = controller.cancelRental(10, userDetails, redirectAttributes);

        assertEquals("redirect:/rentHistory", result);
        verify(rentalService).cancelRental(10, 5);
    }
    @Test
    void cancelRental_ShouldRedirectWithError_WhenDataAccessExceptionThrown() {
        Rental rental = mock(Rental.class);
        when(rental.getRentalStatusName()).thenReturn("PENDING");
        when(userDetails.getId()).thenReturn(5);
        when(rentalService.getRentalById(10)).thenReturn(rental);
        doThrow(new DataAccessException("Unauthorized"))
                .when(rentalService).cancelRental(10, 5);

        String result = controller.cancelRental(10, userDetails, redirectAttributes);

        assertEquals("redirect:/rentHistory", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MSG, "Unauthorized");
    }
    @Test
    void viewRentalHistory_ShouldRedirectToMain_WhenServiceThrows() {
        when(userDetails.getUsername()).thenReturn("client");
        when(userService.getUserByUsername("client"))
                .thenThrow(new RuntimeException("DB error"));

        String result = controller.viewRentalHistory(
                0, 10, "all", model, redirectAttributes, userDetails);

        assertEquals(REDIRECT_TO_MAIN_PAGE, result);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR_MSG), any());
    }
    @Test
    void processRental_ShouldReturnForm_WhenServiceThrowsOnAddRental() {
        Car car = createCar();
        User user = new User();
        user.setId(5);

        RentalDTO rentalDTO = new RentalDTO();
        rentalDTO.setDateRange("2026-06-10 to 2026-06-13");
        rentalDTO.setSelectedExtraIds(List.of());

        when(carService.getCarById(1)).thenReturn(car);
        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of(car));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(rentalService.isCarAvailable(eq(1), any(), any())).thenReturn(true);
        when(userDetails.getId()).thenReturn(5);
        when(userService.getUserById(5)).thenReturn(user);
        when(rentalService.getBlockedDatesForCar(1)).thenReturn(List.of());
        doThrow(new RuntimeException("DB error")).when(rentalService).addRental(any());

        String result = controller.processRental(
                1, 1, rentalDTO, bindingResult, model, userDetails, redirectAttributes);

        assertEquals("clientRental/rentCarPage", result);
        verify(model).addAttribute(eq(ERROR_MSG), contains("DB error"));
    }
    @Test
    void processRental_ShouldReturnForm_WhenDateRangeIsInvalid() {
        Car car = createCar();

        RentalDTO rentalDTO = new RentalDTO();
        rentalDTO.setDateRange("bad date");

        when(carService.getCarById(1)).thenReturn(car);
        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of(car));
        when(rentalService.getBlockedDatesForCar(1)).thenReturn(List.of());

        String result = controller.processRental(
                1,
                1,
                rentalDTO,
                bindingResult,
                model,
                userDetails,
                redirectAttributes
        );

        assertEquals("clientRental/rentCarPage", result);
        verify(model).addAttribute(ERROR_MSG, "Invalid date range format.");
        verify(rentalService, never()).addRental(any());
    }

    @Test
    void processRental_ShouldReturnForm_WhenCarIsNotAvailable() {
        Car car = createCar();

        RentalDTO rentalDTO = new RentalDTO();
        rentalDTO.setDateRange("2026-06-10 to 2026-06-13");

        when(carService.getCarById(1)).thenReturn(car);
        when(carService.getAvailableCarsByModelId(1)).thenReturn(List.of(car));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(rentalService.isCarAvailable(eq(1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(false);
        when(rentalService.getBlockedDatesForCar(1)).thenReturn(List.of());

        String result = controller.processRental(
                1,
                1,
                rentalDTO,
                bindingResult,
                model,
                userDetails,
                redirectAttributes
        );

        assertEquals("clientRental/rentCarPage", result);
        verify(model).addAttribute(ERROR_MSG, "The selected car is not available for those dates.");
        verify(rentalService, never()).addRental(any());
    }

    @Test
    void viewRentalHistory_ShouldReturnHistoryPage() {
        User user = new User();
        user.setId(5);

        PageResult<Rental> rentalPage = new PageResult<>(List.of(), 0, 10, 0);

        when(userDetails.getUsername()).thenReturn("client");
        when(userService.getUserByUsername("client")).thenReturn(user);
        when(rentalService.getClientRentalsPage(5, "all", 0, 10)).thenReturn(rentalPage);

        String result = controller.viewRentalHistory(
                0,
                10,
                "all",
                model,
                redirectAttributes,
                userDetails
        );

        assertEquals("clientRental/rentHistoryPage", result);
        verify(model).addAttribute(PAGE, rentalPage);
        verify(model).addAttribute("filter", "all");
    }

    @Test
    void cancelRental_ShouldCancelRental_WhenStatusIsPending() {
        Rental rental = mock(Rental.class);

        when(rental.getRentalStatusName()).thenReturn("PENDING");
        when(userDetails.getId()).thenReturn(5);
        when(rentalService.getRentalById(10)).thenReturn(rental);

        String result = controller.cancelRental(10, userDetails, redirectAttributes);

        assertEquals("redirect:/rentHistory", result);
        verify(rentalService).cancelRental(10, 5);
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MSG, "Your order has been cancelled.");
    }

    @Test
    void cancelRental_ShouldNotCancel_WhenStatusIsCompleted() {
        Rental rental = mock(Rental.class);

        when(rental.getRentalStatusName()).thenReturn("COMPLETED");
        when(rentalService.getRentalById(10)).thenReturn(rental);

        String result = controller.cancelRental(10, userDetails, redirectAttributes);

        assertEquals("redirect:/rentHistory", result);
        verify(rentalService, never()).cancelRental(anyInt(), anyInt());
        verify(redirectAttributes).addFlashAttribute(
                ERROR_MSG,
                "Order cannot be cancelled at this stage."
        );
    }
}