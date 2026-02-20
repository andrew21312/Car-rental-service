package com.car_rental.service;

import static com.car_rental.constants.ControllerConstants.APPROVED;
import static com.car_rental.constants.ControllerConstants.READY_FOR_PICKUP;

import com.car_rental.entity.PageResult;
import com.car_rental.form.car.CarIssueReport;
import com.car_rental.form.car.CarPreparationDTO;
import java.util.LinkedHashMap;
import java.util.Map;

public interface FleetManagerService {
    PageResult<CarPreparationDTO> getCarsToPreparePage(int page, int size, String statusFilter);

    default Map<String, String> getPreparationStatuses() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("all", "All");
        statuses.put(APPROVED, "Approved");
        statuses.put(READY_FOR_PICKUP, "Ready for pickup");
        return statuses;
    }

    void prepareCar(int rentalId);

    void reportCarIssue(CarIssueReport report);

    PageResult<CarIssueReport> getCarsIssuesPage(String status, int page, int size);

    void markResolved(int reportId);
}
