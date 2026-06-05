package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.RentalDAO;
import com.car_rental.entity.*;
import com.car_rental.form.rental.RentalStatusUpdateDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.car_rental.constants.ControllerConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalServiceImplTest {

    private final RentalDAO rentalDAO = mock(RentalDAO.class);
    private final RentalStatusService rentalStatusService = mock(RentalStatusService.class);
    private final LogService logService = mock(LogService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private final RentalServiceImpl service =
            new RentalServiceImpl(rentalDAO, rentalStatusService, logService, currentUserService);

    private RentalStatuses status(int id, String name) {
        RentalStatuses status = new RentalStatuses();
        status.setId(id);
        status.setName(name);
        return status;
    }

    @Test
    void addRental_ShouldSetPendingStatusSaveRentalAndWriteLog() {
        Rental rental = new Rental();
        rental.setId(1);

        RentalStatuses pending = status(1, PENDING);

        when(rentalStatusService.getRentalStatusByName(PENDING)).thenReturn(pending);
        when(currentUserService.getCurrentUserId()).thenReturn(5);

        service.addRental(rental);

        assertEquals(pending, rental.getRentalStatuses());
        verify(rentalDAO).addRental(rental);
        verify(logService).logEvent(
                5,
                Log.AuditEventType.ADD_RENTAL,
                "Rental #1 added successfully"
        );
    }

    @Test
    void addRental_ShouldThrowDataAccessException_WhenDaoThrowsException() {
        Rental rental = new Rental();

        when(rentalStatusService.getRentalStatusByName(PENDING))
                .thenReturn(status(1, PENDING));
        doThrow(new RuntimeException("DB error")).when(rentalDAO).addRental(rental);

        assertThrows(DataAccessException.class, () -> service.addRental(rental));
    }

    @Test
    void isCarAvailable_ShouldReturnTrue_WhenDaoReturnsTrue() {
        LocalDate start = LocalDate.of(2026, 6, 10);
        LocalDate end = LocalDate.of(2026, 6, 13);

        when(rentalStatusService.getRentalStatusByName(CANCELLED))
                .thenReturn(status(2, CANCELLED));
        when(rentalStatusService.getRentalStatusByName(REJECTED_TECHNICAL))
                .thenReturn(status(3, REJECTED_TECHNICAL));
        when(rentalDAO.isCarAvailable(1, start, end, 2, 3)).thenReturn(true);

        boolean result = service.isCarAvailable(1, start, end);

        assertTrue(result);
        verify(rentalDAO).isCarAvailable(1, start, end, 2, 3);
    }

    @Test
    void getBlockedDatesForCar_ShouldReturnBlockedDates() {
        List<String> blockedDates = List.of("2026-06-10", "2026-06-11");

        when(rentalStatusService.getRentalStatusByName(CANCELLED))
                .thenReturn(status(2, CANCELLED));
        when(rentalStatusService.getRentalStatusByName(REJECTED_TECHNICAL))
                .thenReturn(status(3, REJECTED_TECHNICAL));
        when(rentalDAO.getBlockedDatesForCar(1, 2, 3)).thenReturn(blockedDates);

        List<String> result = service.getBlockedDatesForCar(1);

        assertEquals(blockedDates, result);
        verify(rentalDAO).getBlockedDatesForCar(1, 2, 3);
    }

    @Test
    void getClientRentalsPage_ShouldReturnClientRentals() {
        PageResult<Rental> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(rentalDAO.getClientRentalsPage(5, 0, 10)).thenReturn(pageResult);

        PageResult<Rental> result = service.getClientRentalsPage(5, 0, 10);

        assertEquals(pageResult, result);
        verify(rentalDAO).getClientRentalsPage(5, 0, 10);
    }

    @Test
    void updateRentalStatus_ShouldSetNewStatusAndWriteLog() {
        Rental rental = new Rental();
        rental.setId(1);

        RentalStatuses approved = status(4, APPROVED);

        when(rentalStatusService.getRentalStatusByName(APPROVED)).thenReturn(approved);
        when(currentUserService.getCurrentUserId()).thenReturn(5);

        service.updateRentalStatus(rental, APPROVED);

        assertEquals(approved, rental.getRentalStatuses());
        verify(rentalDAO).updateRentalStatus(rental);
        verify(logService).logEvent(
                5,
                Log.AuditEventType.UPDATE_RENTAL_STATUS,
                "Rental #1 status updated to APPROVED"
        );
    }

    @Test
    void updateRentalStatus_ShouldSetPaymentCancelled_WhenStatusCancelled() {
        Rental rental = new Rental();
        rental.setId(1);

        RentalStatuses cancelled = status(2, CANCELLED);

        when(rentalStatusService.getRentalStatusByName(CANCELLED)).thenReturn(cancelled);
        when(currentUserService.getCurrentUserId()).thenReturn(5);

        service.updateRentalStatus(rental, CANCELLED);

        assertEquals(cancelled, rental.getRentalStatuses());
        assertEquals(RentalStatusUpdateDTO.PaymentStatus.CANCELLED, rental.getPaymentStatus());
        verify(rentalDAO).updateRentalStatus(rental);
    }

    @Test
    void cancelRental_ShouldCancel_WhenRentalBelongsToUserAndStatusPending() {
        Rental rental = mock(Rental.class);
        User client = new User();
        client.setId(5);

        when(rentalDAO.getRentalById(1)).thenReturn(rental);
        when(rental.getClient()).thenReturn(client);
        when(rental.getRentalStatusName()).thenReturn(PENDING);
        when(rentalStatusService.getRentalStatusByName(CANCELLED))
                .thenReturn(status(2, CANCELLED));

        service.cancelRental(1, 5);

        verify(rentalDAO).updateRentalStatus(rental);
    }

    @Test
    void cancelRental_ShouldThrowDataAccessException_WhenRentalNotFound() {
        when(rentalDAO.getRentalById(1)).thenReturn(null);

        assertThrows(DataAccessException.class, () ->
                service.cancelRental(1, 5)
        );
    }

    @Test
    void cancelRental_ShouldThrowDataAccessException_WhenRentalBelongsToAnotherUser() {
        Rental rental = mock(Rental.class);
        User client = new User();
        client.setId(10);

        when(rentalDAO.getRentalById(1)).thenReturn(rental);
        when(rental.getClient()).thenReturn(client);

        assertThrows(DataAccessException.class, () ->
                service.cancelRental(1, 5)
        );

        verify(rentalDAO, never()).updateRentalStatus(any());
    }

    @Test
    void isValidStatusTransition_ShouldReturnTrue_ForValidTransitions() {
        assertTrue(service.isValidStatusTransition(PENDING, APPROVED));
        assertTrue(service.isValidStatusTransition(PENDING, CANCELLED));
        assertTrue(service.isValidStatusTransition(APPROVED, READY_FOR_PICKUP));
        assertTrue(service.isValidStatusTransition(READY_FOR_PICKUP, ACTIVE));
        assertTrue(service.isValidStatusTransition(ACTIVE, COMPLETED));
    }

    @Test
    void isValidStatusTransition_ShouldReturnFalse_ForInvalidTransitions() {
        assertFalse(service.isValidStatusTransition(PENDING, COMPLETED));
        assertFalse(service.isValidStatusTransition(ACTIVE, CANCELLED));
        assertFalse(service.isValidStatusTransition(COMPLETED, ACTIVE));
    }

    @Test
    void updateRentalExtras_ShouldReplaceNullWithEmptyList() {
        when(currentUserService.getCurrentUserId()).thenReturn(5);

        service.updateRentalExtras(1, null);

        verify(rentalDAO).updateRentalExtras(1, List.of());
        verify(logService).logEvent(
                5,
                Log.AuditEventType.UPDATE_RENTAL_EXTRAS,
                "Rental #1 extras updated"
        );
    }

    @Test
    void getRentalById_ShouldReturnRental() {
        Rental rental = new Rental();
        rental.setId(1);

        when(rentalDAO.getRentalById(1)).thenReturn(rental);

        Rental result = service.getRentalById(1);

        assertEquals(rental, result);
        verify(rentalDAO).getRentalById(1);
    }
}