package com.car_rental.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UtilityService {
    private static final Logger logger = LoggerFactory.getLogger(UtilityService.class);

    // Compiled patterns for better performance
    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.+?)' for key '(.+?)'");
    private static final Pattern NULL_CONSTRAINT_PATTERN = Pattern.compile("Column '(.+?)' cannot be null");

    // Default error messages
    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred";
    private static final String DATABASE_ERROR_MESSAGE =
            "An error occurred while accessing the database. Please try again later.";
    private static final String FOREIGN_KEY_ERROR_MESSAGE = "Operation failed due to related data constraints.";

    public String handleException(Exception exception) {
        if (exception == null) {
            return DEFAULT_ERROR_MESSAGE;
        }

        logger.error("Handling exception: {}", exception.getMessage(), exception);

        String errorMessage = getErrorMessage(exception);

        if (errorMessage.contains("Duplicate entry")) {
            return handleDuplicateEntry(errorMessage);
        }

        if (exception instanceof DataAccessException) {
            return handleDataAccessException(errorMessage);
        }

        if (exception instanceof IllegalArgumentException) {
            return "Invalid input: " + exception.getMessage();
        }

        if (errorMessage.contains("cannot be null")) {
            return handleNullConstraint(errorMessage);
        }

        if (errorMessage.contains("foreign key constraint fails")) {
            return FOREIGN_KEY_ERROR_MESSAGE;
        }

        if (exception.getCause() != null && exception.getCause().getMessage() != null) {
            return DEFAULT_ERROR_MESSAGE + ": " + exception.getCause().getMessage();
        }

        return DEFAULT_ERROR_MESSAGE + (errorMessage.isEmpty() ? "" : ": " + errorMessage);
    }

    private String getErrorMessage(Exception exception) {
        return exception.getMessage() != null ? exception.getMessage() : "";
    }

    private String handleDuplicateEntry(String errorMessage) {
        try {
            Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(errorMessage);
            if (matcher.find()) {
                String duplicateValue = matcher.group(1);
                String keyName = matcher.group(2);
                String fieldName = extractFieldName(keyName);
                return String.format("A record with %s '%s' already exists.", fieldName, duplicateValue);
            }
        } catch (Exception ex) {
            logger.error("Error parsing duplicate entry message: {}", errorMessage, ex);
        }
        return "A record with this value already exists.";
    }

    private String extractFieldName(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return "field";
        }

        int dotIndex = keyName.indexOf('.');
        return dotIndex != -1 ? keyName.substring(dotIndex + 1) : keyName;
    }

    private String handleDataAccessException(String errorMessage) {
        if (errorMessage.contains("Incorrect result size")) {
            if (errorMessage.contains("expected 1, actual 0")) {
                return "The requested item was not found. Please check the details and try again.";
            }
            if (errorMessage.contains("expected 1")) {
                return "Multiple items found when expecting only one. Please contact support.";
            }
        }

        if (errorMessage.contains("Connection") || errorMessage.contains("timeout")) {
            return "Database connection issue. Please try again later.";
        }

        return DATABASE_ERROR_MESSAGE;
    }

    private String handleNullConstraint(String errorMessage) {
        try {
            Matcher matcher = NULL_CONSTRAINT_PATTERN.matcher(errorMessage);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                return "Missing required field: " + fieldName;
            }
        } catch (Exception ex) {
            logger.error("Error parsing null constraint message: {}", errorMessage, ex);
        }
        return "A required field is missing.";
    }
}