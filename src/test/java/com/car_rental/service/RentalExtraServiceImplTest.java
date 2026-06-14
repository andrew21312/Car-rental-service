package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.RentalExtraDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.RentalExtra;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalExtraServiceImplTest {

    private final RentalExtraDAO rentalExtraDAO = mock(RentalExtraDAO.class);
    private final LogService logService = mock(LogService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private RentalExtraServiceImpl createServiceWithExtras(List<RentalExtra> extras) {
        when(rentalExtraDAO.getAllExtras()).thenReturn(extras);
        return new RentalExtraServiceImpl(rentalExtraDAO, logService, currentUserService);
    }

    @Test
    void getExtraId_ShouldReturnId_WhenExtraExists() {
        RentalExtra gps = new RentalExtra();
        gps.setId(1);
        gps.setName("GPS");

        RentalExtraServiceImpl service = createServiceWithExtras(List.of(gps));

        int result = service.getExtraId("GPS");

        assertEquals(1, result);
    }

    @Test
    void getExtraId_ShouldThrowDataAccessException_WhenExtraDoesNotExist() {
        RentalExtraServiceImpl service = createServiceWithExtras(List.of());

        assertThrows(DataAccessException.class, () ->
                service.getExtraId("Child Seat")
        );
    }

    @Test
    void getExtrasList_ShouldReturnCachedExtras() {
        RentalExtra gps = new RentalExtra();
        gps.setId(1);
        gps.setName("GPS");

        RentalExtraServiceImpl service = createServiceWithExtras(List.of(gps));

        List<RentalExtra> result = service.getExtrasList();

        assertEquals(1, result.size());
        assertEquals("GPS", result.get(0).getName());
    }

    @Test
    void getExtraById_ShouldReturnExtra_WhenIdExists() {
        RentalExtra charger = new RentalExtra();
        charger.setId(3);
        charger.setName("Phone Charger");

        RentalExtraServiceImpl service = createServiceWithExtras(List.of(charger));

        RentalExtra result = service.getExtraById(3);

        assertEquals(charger, result);
    }

    @Test
    void getExtraById_ShouldReturnNull_WhenIdDoesNotExist() {
        RentalExtraServiceImpl service = createServiceWithExtras(List.of());

        RentalExtra result = service.getExtraById(99);

        assertNull(result);
    }

    @Test
    void getExtraByName_ShouldReturnExtra_WhenNameExists() {
        RentalExtra baggage = new RentalExtra();
        baggage.setId(4);
        baggage.setName("Extra Baggage");

        RentalExtraServiceImpl service = createServiceWithExtras(List.of(baggage));

        RentalExtra result = service.getExtraByName("Extra Baggage");

        assertEquals(baggage, result);
    }

    @Test
    void getExtrasPage_ShouldReturnPageResult() {
        RentalExtraServiceImpl service = createServiceWithExtras(List.of());
        PageResult<RentalExtra> pageResult = new PageResult<>(List.of(), 0, 10, 0);

        when(rentalExtraDAO.getExtrasPage("GPS", 0, 10)).thenReturn(pageResult);

        PageResult<RentalExtra> result = service.getExtrasPage("GPS", 0, 10);

        assertEquals(pageResult, result);
        verify(rentalExtraDAO).getExtrasPage("GPS", 0, 10);
    }

    @Test
    void getExtrasPage_ShouldThrowDataAccessException_WhenDaoThrowsException() {
        RentalExtraServiceImpl service = createServiceWithExtras(List.of());

        when(rentalExtraDAO.getExtrasPage("GPS", 0, 10))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DataAccessException.class, () ->
                service.getExtrasPage("GPS", 0, 10)
        );
    }

    @Test
    void addExtra_ShouldAddExtraRefreshCacheAndWriteLog() {
        RentalExtra newExtra = new RentalExtra();
        newExtra.setId(1);
        newExtra.setName("GPS");
        newExtra.setPrice(5.0);

        when(rentalExtraDAO.getAllExtras())
                .thenReturn(List.of())
                .thenReturn(List.of(newExtra));
        when(currentUserService.getCurrentUserId()).thenReturn(10);

        RentalExtraServiceImpl service =
                new RentalExtraServiceImpl(rentalExtraDAO, logService, currentUserService);

        service.addExtra(newExtra);

        verify(rentalExtraDAO).addExtra(newExtra);
        verify(rentalExtraDAO, times(2)).getAllExtras();
        verify(logService).logEvent(
                10,
                Log.AuditEventType.ADD_EXTRA,
                "Added extra: GPS | price: 5,00"
        );

        assertEquals(newExtra, service.getExtraByName("GPS"));
    }

    @Test
    void updateExtra_ShouldUpdateExtraRefreshCacheAndWriteLog() {
        RentalExtra existingExtra = new RentalExtra();
        existingExtra.setId(1);
        existingExtra.setName("GPS");
        existingExtra.setPrice(5.0);

        RentalExtra updatedExtra = new RentalExtra();
        updatedExtra.setId(1);
        updatedExtra.setName("GPS Plus");
        updatedExtra.setPrice(7.5);

        when(rentalExtraDAO.getAllExtras())
                .thenReturn(List.of(existingExtra))
                .thenReturn(List.of(updatedExtra));
        when(currentUserService.getCurrentUserId()).thenReturn(10);

        RentalExtraServiceImpl service =
                new RentalExtraServiceImpl(rentalExtraDAO, logService, currentUserService);

        service.updateExtra(updatedExtra);

        verify(rentalExtraDAO).updateExtra(existingExtra, updatedExtra);
        verify(logService).logEvent(
                10,
                Log.AuditEventType.UPDATE_EXTRA,
                "Updated extra #1: GPS Plus | price: 7,50"
        );

        assertEquals(updatedExtra, service.getExtraByName("GPS Plus"));
    }

    @Test
    void deleteExtra_ShouldDeleteExtraRefreshCacheAndWriteLog() {
        RentalExtra gps = new RentalExtra();
        gps.setId(1);
        gps.setName("GPS");

        when(rentalExtraDAO.getAllExtras())
                .thenReturn(List.of(gps))
                .thenReturn(List.of());
        when(currentUserService.getCurrentUserId()).thenReturn(10);

        RentalExtraServiceImpl service =
                new RentalExtraServiceImpl(rentalExtraDAO, logService, currentUserService);

        service.deleteExtra(1);

        verify(rentalExtraDAO).deleteExtra(1);
        verify(logService).logEvent(
                10,
                Log.AuditEventType.DELETE_EXTRA,
                "Deleted extra with ID: 1"
        );

        assertNull(service.getExtraByName("GPS"));
    }
}