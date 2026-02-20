package com.car_rental.service;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.CarDAO;
import com.car_rental.dao.daointerface.CarIssueReportDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.form.car.CarIssueReport;
import com.car_rental.form.car.CarPreparationDTO;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing fleet operations
 */
@Service
public class FleetManagerServiceImpl implements FleetManagerService {
    private static final Logger log = LoggerFactory.getLogger(FleetManagerServiceImpl.class);

    private final CarDAO carDAO;
    private final CarIssueReportDAO reportDAO;
    private final RentalStatusService rentalStatusService;
    private final LogService logService;
    private final CurrentUserService currentUserService;
    private final CarStatusService carStatusService;
    private final UtilityService utilityService = new UtilityService();

    @Autowired
    public FleetManagerServiceImpl(CarDAO carDAO,
                                   CarIssueReportDAO reportDAO,
                                   RentalStatusService rentalStatusService,
                                   LogService logService,
                                   CurrentUserService currentUserService,
                                   CarStatusService carStatusService) {
        this.carDAO = carDAO;
        this.reportDAO = reportDAO;
        this.rentalStatusService = rentalStatusService;
        this.logService = logService;
        this.currentUserService = currentUserService;
        this.carStatusService = carStatusService;
    }

    /**
     * Retrieves a paginated list of cars that need preparation
     *
     * @param page         Page number
     * @param size         Page size
     * @param statusFilter Optional status filter
     * @return Paginated result of cars to prepare
     */
    @Override
    public PageResult<CarPreparationDTO> getCarsToPreparePage(int page, int size, String statusFilter) {
        try {
            return carDAO.getCarsToPreparePage(page, size, statusFilter, rentalStatusService);
        } catch (Exception e) {
            log.error("Error retrieving cars to prepare: {}", e.getMessage());
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Marks a car as prepared for pickup
     *
     * @param rentalId ID of the rental
     * @throws IllegalArgumentException if car cannot be prepared
     */
    @Override
    public void prepareCar(int rentalId) {
        try {
            CarPreparationDTO car = carDAO.getCarToPrepare(rentalId, rentalStatusService);
            if (!canPrepareCar(car)) {
                throw new IllegalArgumentException("Car cannot be prepared");
            }

            int readyStatusId = rentalStatusService.getStatusId(READY_FOR_PICKUP);
            carDAO.prepareCar(readyStatusId, rentalId);

            logService.logEvent(
                    currentUserService.getCurrentUserId(),
                    Log.AuditEventType.PREPARE_CAR,
                    "Prepared car for rental id: " + rentalId
                               );
        } catch (Exception e) {
            log.error("Error preparing car for rental id {}: {}", rentalId, e.getMessage());
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Checks if a car can be prepared based on pickup date
     *
     * @param car Car preparation details
     * @return true if car can be prepared within 2 days
     */
    private boolean canPrepareCar(CarPreparationDTO car) {
        LocalDate today = LocalDate.now();
        long daysUntilRental = ChronoUnit.DAYS.between(today, car.getPickupDate());
        return daysUntilRental <= 2;
    }

    /**
     * Creates a new car issue report
     *
     * @param report Car issue report details
     */
    @Override
    public void reportCarIssue(CarIssueReport report) {
        try {
            report.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            report.setStatus(CarIssueReport.ReportStatus.PENDING);

            int rejectedStatusId = rentalStatusService.getStatusId(REJECTED_TECHNICAL);
            int readyForPickupStatusId = rentalStatusService.getStatusId(READY_FOR_PICKUP);
            int approvedStatusId = rentalStatusService.getStatusId(APPROVED);
            int maintenanceStatusId = carStatusService.getStatusId(MAINTENANCE);

            reportDAO.createReport(
                    report,
                    rejectedStatusId,
                    approvedStatusId,
                    readyForPickupStatusId,
                    maintenanceStatusId
                                  );

            logService.logEvent(
                    currentUserService.getCurrentUserId(),
                    Log.AuditEventType.REPORT_CAR_ISSUE,
                    "Reported car issue for car id: " + report.getId()
                               );
        } catch (Exception e) {
            log.error("Error reporting car issue: {}", e.getMessage());
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a paginated list of car issues
     *
     * @param status Issue status filter
     * @param page   Page number
     * @param size   Page size
     * @return Paginated result of car issues
     */
    @Override
    public PageResult<CarIssueReport> getCarsIssuesPage(String status, int page, int size) {
        try {
            return reportDAO.getCarsIssuesPage(status, page, size);
        } catch (Exception e) {
            log.error("Error retrieving car issues: {}", e.getMessage());
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Marks a car issue report as resolved
     *
     * @param reportId ID of the report to mark as resolved
     */
    @Override
    public void markResolved(int reportId) {
        try {
            int availableStatusId = carStatusService.getStatusId(AVAILABLE);
            reportDAO.markResolved(reportId, availableStatusId);

            logService.logEvent(
                    currentUserService.getCurrentUserId(),
                    Log.AuditEventType.MARK_REPORT_RESOLVED,
                    "Marked report " + reportId + " as resolved"
                               );
        } catch (Exception e) {
            log.error("Error marking report {} as resolved: {}", reportId, e.getMessage());
            throw new DataAccessException(utilityService.handleException(e));
        }
    }
}
