/**
 * Service implementation for managing rental extras.
 * Provides CRUD operations for rental extras and maintains a cache for efficient access.
 */

package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.RentalExtraDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.RentalExtra;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of RentalExtraService that provides business logic for managing rental extras.
 */
@Service
public class RentalExtraServiceImpl implements RentalExtraService {
    private static final Logger log = LoggerFactory.getLogger(RentalExtraServiceImpl.class);
    private final RentalExtraDAO rentalExtraDao;
    private final Map<String, RentalExtra> extraCache;
    private final LogService auditLogService;
    private final CurrentUserService currentUserService;
    private List<RentalExtra> extrasList;

    @Autowired
    public RentalExtraServiceImpl(RentalExtraDAO rentalExtraDao,
                                  LogService auditLogService,
                                  CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.rentalExtraDao = rentalExtraDao;
        this.extraCache = new HashMap<>();
        initializeExtrasCache();
    }

    /**
     * Initializes the extras cache by loading all extras from the database.
     */
    private void initializeExtrasCache() {
        extrasList = rentalExtraDao.getAllExtras();
        extraCache.clear();
        extrasList.forEach(extra -> extraCache.put(extra.getName(), extra));
    }

    @Override
    public int getExtraId(String name) {
        RentalExtra extra = extraCache.get(name);
        if (extra == null) {
            log.warn("Attempt to get non-existent extra: {}", name);
            throw new DataAccessException("Extra not found: " + name);
        }
        return extra.getId();
    }

    @Override
    public List<RentalExtra> getExtrasList() {
        return extrasList;
    }

    @Override
    public RentalExtra getExtraById(int id) {
        return extrasList.stream()
                .filter(extra -> extra.getId() == id)
                .findFirst().orElse(null);
    }

    @Override
    public RentalExtra getExtraByName(String name) {
        return extraCache.get(name);
    }

    @Override
    public PageResult<RentalExtra> getExtrasPage(String query, int page, int size) {
        try {
            return rentalExtraDao.getExtrasPage(query, page, size);
        } catch (Exception e) {
            log.error("Failed to retrieve extras page: {}", e.getMessage(), e);
            throw new DataAccessException("Failed to retrieve extras page", e);
        }
    }

    @Override
    public void deleteExtra(int id) {
        try {
            rentalExtraDao.deleteExtra(id);
            initializeExtrasCache();
            logAuditEvent(Log.AuditEventType.DELETE_EXTRA, "Deleted extra with ID: " + id);
        } catch (Exception e) {
            log.error("Failed to delete extra: {}", e.getMessage(), e);
            throw new DataAccessException("Failed to delete extra", e);
        }
    }

    @Override
    public void updateExtra(RentalExtra updatedExtra) {
        try {
            RentalExtra existingExtra = getExtraById(updatedExtra.getId());
            rentalExtraDao.updateExtra(existingExtra, updatedExtra);
            initializeExtrasCache();
            logAuditEvent(Log.AuditEventType.UPDATE_EXTRA,
                          String.format("Updated extra #%d: %s | price: %.2f",
                                        updatedExtra.getId(),
                                        updatedExtra.getName(),
                                        updatedExtra.getPrice()));
        } catch (Exception e) {
            log.error("Failed to update extra: {}", e.getMessage(), e);
            throw new DataAccessException("Failed to update extra", e);
        }
    }

    @Override
    public void addExtra(RentalExtra newExtra) {
        try {
            rentalExtraDao.addExtra(newExtra);
            initializeExtrasCache();
            logAuditEvent(Log.AuditEventType.ADD_EXTRA,
                          String.format("Added extra: %s | price: %.2f",
                                        newExtra.getName(),
                                        newExtra.getPrice()));
        } catch (Exception e) {
            log.error("Failed to add extra: {}", e.getMessage(), e);
            throw new DataAccessException("Failed to add extra", e);
        }
    }

    /**
     * Helper method to log audit events with consistent formatting.
     */
    private void logAuditEvent(Log.AuditEventType eventType, String message) {
        auditLogService.logEvent(
                currentUserService.getCurrentUserId(),
                eventType,
                message
                                );
    }
}
