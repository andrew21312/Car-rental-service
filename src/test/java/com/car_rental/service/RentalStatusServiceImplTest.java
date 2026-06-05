package com.car_rental.service;

import com.car_rental.dao.daointerface.RentalStatusDAO;
import com.car_rental.entity.RentalStatuses;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalStatusServiceImplTest {

    private final RentalStatusDAO rentalStatusDAO = mock(RentalStatusDAO.class);

    private RentalStatusServiceImpl createServiceWithStatuses(List<RentalStatuses> statuses) {
        when(rentalStatusDAO.findAllStatuses()).thenReturn(statuses);
        return new RentalStatusServiceImpl(rentalStatusDAO);
    }

    @Test
    void getStatusId_ShouldReturnId_WhenStatusExists() {
        RentalStatuses pending = new RentalStatuses();
        pending.setId(1);
        pending.setName("PENDING");

        RentalStatusServiceImpl service = createServiceWithStatuses(List.of(pending));

        int result = service.getStatusId("PENDING");

        assertEquals(1, result);
    }

    @Test
    void getStatusId_ShouldThrowIllegalArgumentException_WhenStatusDoesNotExist() {
        RentalStatusServiceImpl service = createServiceWithStatuses(List.of());

        assertThrows(IllegalArgumentException.class, () ->
                service.getStatusId("UNKNOWN")
        );
    }

    @Test
    void getStatusList_ShouldReturnAllStatuses() {
        RentalStatuses pending = new RentalStatuses();
        pending.setId(1);
        pending.setName("PENDING");

        RentalStatuses cancelled = new RentalStatuses();
        cancelled.setId(2);
        cancelled.setName("CANCELLED");

        RentalStatusServiceImpl service =
                createServiceWithStatuses(List.of(pending, cancelled));

        List<RentalStatuses> result = service.getStatusList();

        assertEquals(2, result.size());
        assertEquals("PENDING", result.get(0).getName());
        assertEquals("CANCELLED", result.get(1).getName());
    }

    @Test
    void getRentalStatusById_ShouldReturnStatus_WhenIdExists() {
        RentalStatuses pending = new RentalStatuses();
        pending.setId(1);
        pending.setName("PENDING");

        RentalStatusServiceImpl service = createServiceWithStatuses(List.of(pending));

        RentalStatuses result = service.getRentalStatusById(1);

        assertEquals(pending, result);
    }

    @Test
    void getRentalStatusById_ShouldReturnNull_WhenIdDoesNotExist() {
        RentalStatusServiceImpl service = createServiceWithStatuses(List.of());

        RentalStatuses result = service.getRentalStatusById(99);

        assertNull(result);
    }

    @Test
    void getRentalStatusByName_ShouldReturnStatus_WhenNameExists() {
        RentalStatuses approved = new RentalStatuses();
        approved.setId(2);
        approved.setName("APPROVED");

        RentalStatusServiceImpl service = createServiceWithStatuses(List.of(approved));

        RentalStatuses result = service.getRentalStatusByName("APPROVED");

        assertEquals(approved, result);
    }

    @Test
    void getRentalStatusByName_ShouldReturnNull_WhenNameDoesNotExist() {
        RentalStatusServiceImpl service = createServiceWithStatuses(List.of());

        RentalStatuses result = service.getRentalStatusByName("ACTIVE");

        assertNull(result);
    }
}