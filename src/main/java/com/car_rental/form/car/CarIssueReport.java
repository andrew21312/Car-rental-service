package com.car_rental.form.car;

import com.car_rental.entity.Car;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class CarIssueReport {
    private int id;
    private Car car;
    private String message;
    private Timestamp createdAt;
    private ReportStatus status;
    private Timestamp resolvedAt;

    public enum ReportStatus {
        PENDING, RESOLVED
    }

    public CarIssueReport() {
        this.status = ReportStatus.PENDING;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getFormattedCreatedAt() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return dateFormat.format(createdAt);
    }

    public String getFormattedResolvedAt() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return dateFormat.format(resolvedAt);
    }

}
