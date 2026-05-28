package com.car_rental.entity;

import java.sql.Timestamp;

public class Log {
    private Integer logId;
    private Timestamp logCreatedAt;
    private Integer userId;
    private AuditEventType eventType;
    private String logText;

    public Log() {
    }

    public Log(Integer userId, AuditEventType eventType, String logText) {
        this.userId = userId;
        this.eventType = eventType;
        this.logText = logText;
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Timestamp getLogCreatedAt() {
        return logCreatedAt;
    }

    public void setLogCreatedAt(Timestamp logCreatedAt) {
        this.logCreatedAt = logCreatedAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }


    public enum AuditEventType {
        ADD_RENTAL, UPDATE_RENTAL_STATUS, UPDATE_RENTAL_PAYMENT_STATUS, UPDATE_RENTAL_EXTRAS,
        ADD_CAR_MODEL, UPDATE_CAR_MODEL, DELETE_CAR_MODEL,
        ADD_CAR, UPDATE_CAR, DELETE_CAR, PREPARE_CAR,
        REPORT_CAR_ISSUE, MARK_REPORT_RESOLVED,
        CREATE_USER, UPDATE_USER, DELETE_USER, ADD_EXTRA, UPDATE_EXTRA, DELETE_EXTRA;
        
        public String getCategory() {
            return switch (this) {
                case ADD_RENTAL, ADD_CAR_MODEL, ADD_CAR, CREATE_USER, ADD_EXTRA -> "bg-success";
                case UPDATE_RENTAL_STATUS, UPDATE_RENTAL_PAYMENT_STATUS, UPDATE_CAR, UPDATE_USER, UPDATE_EXTRA ->
                        "bg-warning text-dark";
                case DELETE_CAR_MODEL, DELETE_CAR, DELETE_USER, REPORT_CAR_ISSUE, DELETE_EXTRA -> "bg-danger";
                case MARK_REPORT_RESOLVED, PREPARE_CAR -> "bg-primary";
                default -> "bg-secondary";
            };
        }

        public String getCapitalizedName() {
            String name = this.name().replace("_", " ").toLowerCase();
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }
}
