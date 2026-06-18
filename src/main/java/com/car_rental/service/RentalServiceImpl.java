package com.car_rental.service;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.RentalDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.Rental;
import com.car_rental.entity.RentalStatuses;
import com.car_rental.form.rental.ExpensesReport;
import com.car_rental.form.rental.FavoriteCarModelStat;
import com.car_rental.form.rental.RentalStatusUpdateDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of RentalService providing business logic for rental operations
 */
@Service
public class RentalServiceImpl implements RentalService {
    private static final Logger log = LoggerFactory.getLogger(RentalServiceImpl.class);

    private final RentalDAO rentalDao;
    private final RentalStatusService rentalStatusService;
    private final LogService logService;
    private final CurrentUserService currentUserService;
    private final UtilityService utilityService = new UtilityService();

    /**
     * Constructor for dependency injection
     */
    @Autowired
    public RentalServiceImpl(RentalDAO rentalDao, RentalStatusService rentalStatusService,
                             LogService logService, CurrentUserService currentUserService) {
        this.rentalDao = rentalDao;
        this.rentalStatusService = rentalStatusService;
        this.logService = logService;
        this.currentUserService = currentUserService;
    }

    /**
     * Adds a new rental record
     *
     * @param rental Rental object to be added
     * @throws DataAccessException if there's an error adding the rental
     */
    @Override
    public void addRental(Rental rental) {
        try {
            rental.setRentalStatuses(rentalStatusService.getRentalStatusByName(PENDING));
            rentalDao.addRental(rental);
            logRentalEvent(rental, Log.AuditEventType.ADD_RENTAL, "added successfully");
        } catch (Exception e) {
            log.error("Error adding rental: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Updates rental status
     *
     * @param rental    Rental object to update
     * @param newStatus New rental status
     * @throws DataAccessException if there's an error updating the status
     */
    @Override
    public void updateRentalStatus(Rental rental, String newStatus) {
        try {
            rental.setRentalStatuses(rentalStatusService.getRentalStatusByName(newStatus));
            if (CANCELLED.equals(newStatus)) {
                rental.setPaymentStatus(RentalStatusUpdateDTO.PaymentStatus.CANCELLED);
            }
            rentalDao.updateRentalStatus(rental);
            logRentalEvent(rental, Log.AuditEventType.UPDATE_RENTAL_STATUS,
                    "status updated to " + newStatus);
        } catch (Exception e) {
            log.error("Error updating rental status: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Cancels a rental
     *
     * @param rentalId      ID of the rental to cancel
     * @param currentUserId ID of the user requesting cancellation
     * @throws DataAccessException if rental is not found or unauthorized
     */
    @Override
    public void cancelRental(int rentalId, Integer currentUserId) {
        Rental rental = rentalDao.getRentalById(rentalId);
        if (rental == null) {
            throw new DataAccessException("Rental not found");
        }

        validateRentalCancellation(rental, currentUserId);
        updateRentalStatus(rental, CANCELLED);
    }

    /**
     * Updates rental payment status
     *
     * @param rental Rental object to update
     * @throws DataAccessException if there's an error updating payment status
     */
    @Override
    public void updateRentalPaymentStatus(Rental rental) {
        try {
            rentalDao.updateRentalPaymentStatus(rental);
            logRentalEvent(rental, Log.AuditEventType.UPDATE_RENTAL_PAYMENT_STATUS,
                    "payment status updated");
        } catch (Exception e) {
            log.error("Error updating rental payment status: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves rental by ID
     *
     * @param id Rental ID
     * @return Rental object
     * @throws DataAccessException if there's an error retrieving the rental
     */
    @Override
    public Rental getRentalById(int id) {
        try {
            return rentalDao.getRentalById(id);
        } catch (Exception e) {
            log.error("Error retrieving rental by id {}: {}", id, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves paginated list of rentals with filters
     *
     * @param status         Rental status filter
     * @param rentalId       Specific rental ID filter
     * @param carPlateNumber Car plate number filter
     * @param clientLastName Client last name filter
     * @param page           Page number
     * @param size           Page size
     * @param sortBy         Sort field
     * @return PageResult containing filtered rentals
     * @throws DataAccessException if there's an error retrieving rentals
     */
    @Override
    public PageResult<Rental> getRentalsPage(Integer status, Integer rentalId, String carPlateNumber,
                                             String clientLastName, int page, int size, String sortBy) {
        try {
            return rentalDao.getRentalsPage(status, rentalId, carPlateNumber,
                    clientLastName, page, size, sortBy);
        } catch (Exception e) {
            log.error("Error retrieving rentals: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves blocked dates for a car
     *
     * @param carId Car ID
     * @return List of blocked dates
     * @throws DataAccessException if there's an error retrieving blocked dates
     */
    @Override
    public List<String> getBlockedDatesForCar(int carId) {
        try {
            RentalStatuses cancelledStatus = rentalStatusService.getRentalStatusByName(CANCELLED);
            RentalStatuses rejectedTechnicalStatus = rentalStatusService.getRentalStatusByName(REJECTED_TECHNICAL);
            return rentalDao.getBlockedDatesForCar(carId, cancelledStatus.getId(), rejectedTechnicalStatus.getId());
        } catch (Exception e) {
            log.error("Error retrieving blocked dates for car with id {}: {}", carId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves paginated list of client's rentals
     *
     * @param clientId Client ID
     * @param page     Page number
     * @param size     Page size
     * @return PageResult containing client's rentals
     * @throws DataAccessException if there's an error retrieving rentals
     */
    @Override
    public PageResult<Rental> getClientRentalsPage(int clientId, String filter, int page, int size) {
        try {
            return rentalDao.getClientRentalsPage(clientId, filter, page, size);
        } catch (Exception e) {
            log.error("Error retrieving rentals by client id {}: {}", clientId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves an expenses report for a client over a date range.
     * Only ACTIVE and COMPLETED rentals are included.
     *
     * @param clientId  Client ID
     * @param startDate Start of the reporting period (inclusive)
     * @param endDate   End of the reporting period (inclusive)
     * @return ExpensesReport with rental count and total amount spent
     * @throws DataAccessException if there's an error generating the report
     */
    @Override
    public ExpensesReport getClientExpensesReport(int clientId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Rental> rentals = rentalDao.getClientReportRentals(clientId, startDate, endDate);
            double totalSpent = rentals.stream().mapToDouble(Rental::getTotalCost).sum();
            return new ExpensesReport(rentals.size(), totalSpent, startDate, endDate, rentals);
        } catch (Exception e) {
            log.error("Error generating expenses report for client id {}: {}", clientId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves the earliest start date among a client's reportable (ACTIVE/COMPLETED)
     * rentals, used to default the expenses report range to the client's full history.
     *
     * @param clientId Client ID
     * @return earliest reportable rental start date, or null if the client has none
     * @throws DataAccessException if there's an error retrieving the date
     */
    @Override
    public LocalDate getClientEarliestReportableRentalDate(int clientId) {
        try {
            return rentalDao.getClientEarliestReportableRentalDate(clientId);
        } catch (Exception e) {
            log.error("Error retrieving earliest reportable rental date for client id {}: {}",
                    clientId, e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Checks car availability for a date range
     *
     * @param carId     Car ID
     * @param startDate Start date
     * @param endDate   End date
     * @return true if car is available, false otherwise
     * @throws DataAccessException if there's an error checking availability
     */
    @Override
    public boolean isCarAvailable(int carId, LocalDate startDate, LocalDate endDate) {
        try {
            RentalStatuses cancelledStatus = rentalStatusService.getRentalStatusByName(CANCELLED);
            RentalStatuses rejectedTechnicalStatus = rentalStatusService.getRentalStatusByName(REJECTED_TECHNICAL);
            return rentalDao.isCarAvailable(carId, startDate, endDate, cancelledStatus.getId(),
                    rejectedTechnicalStatus.getId());
        } catch (Exception e) {
            log.error("Error checking car availability: {}", e.getMessage(), e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Validates rental status transitions
     *
     * @param currentStatus Current rental status
     * @param newStatus     New rental status
     * @return true if transition is valid, false otherwise
     */
    @Override
    public boolean isValidStatusTransition(String currentStatus, String newStatus) {
        currentStatus = currentStatus.trim().toUpperCase();
        newStatus = newStatus.trim().toUpperCase();

        return switch (currentStatus) {
            case PENDING -> APPROVED.equals(newStatus) || CANCELLED.equals(newStatus);
            case APPROVED -> READY_FOR_PICKUP.equals(newStatus) || CANCELLED.equals(newStatus);
            case READY_FOR_PICKUP -> ACTIVE.equals(newStatus) || CANCELLED.equals(newStatus);
            case ACTIVE -> COMPLETED.equals(newStatus);
            default -> false;
        };
    }

    /**
     * Updates rental extras
     *
     * @param rentalId    Rental ID
     * @param newExtraIds List of new extra IDs
     * @throws DataAccessException if there's an error updating extras
     */
    @Override
    public void updateRentalExtras(int rentalId, List<Integer> newExtraIds) {
        try {
            if (newExtraIds == null) {
                newExtraIds = Collections.emptyList();
            }
            rentalDao.updateRentalExtras(rentalId, newExtraIds);
            logRentalEvent(rentalId, Log.AuditEventType.UPDATE_RENTAL_EXTRAS, "extras updated");
        } catch (Exception e) {
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Validates rental cancellation
     *
     * @param rental        Rental to cancel
     * @param currentUserId User requesting cancellation
     * @throws DataAccessException if unauthorized or invalid status
     */
    private void validateRentalCancellation(Rental rental, Integer currentUserId) {
        if (rental.getClient().getId() != currentUserId) {
            throw new DataAccessException("Unauthorized: You cannot cancel someone else's rental");
        }

        String rentalStatus = rental.getRentalStatusName();
        if (!PENDING.equalsIgnoreCase(rentalStatus) &&
                !APPROVED.equalsIgnoreCase(rentalStatus) &&
                !READY_FOR_PICKUP.equalsIgnoreCase(rentalStatus)) {
            throw new DataAccessException("Order cannot be cancelled at this stage.");
        }
    }

    /**
     * Logs rental-related events
     *
     * @param rental    Rental object
     * @param eventType Type of event
     * @param message   Event message
     */
    private void logRentalEvent(Rental rental, Log.AuditEventType eventType, String message) {
        logService.logEvent(currentUserService.getCurrentUserId(), eventType,
                "Rental #" + rental.getId() + " " + message);
    }

    /**
     * Logs rental-related events by ID
     *
     * @param rentalId  Rental ID
     * @param eventType Type of event
     * @param message   Event message
     */
    private void logRentalEvent(int rentalId, Log.AuditEventType eventType, String message) {
        logService.logEvent(currentUserService.getCurrentUserId(), eventType,
                "Rental #" + rentalId + " " + message);
    }

    public List<FavoriteCarModelStat> getFavoriteCarModelsLastMonth() {
        return rentalDao.findFavoriteCarModelsLastMonth();
    }
}
