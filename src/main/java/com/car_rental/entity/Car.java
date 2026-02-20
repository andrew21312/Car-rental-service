package com.car_rental.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public class Car {
    private int id;

    @NotNull(message = "Car model must be specified")
    private CarModel model;

    @NotBlank(message = "Plate number cannot be blank")
    @Size(min = 1, max = 8, message = "Plate number must be between 1 and 8 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid plate number format")
    private String plateNumber;

    @NotBlank(message = "Color cannot be blank")
    @Size(min = 2, max = 50, message = "Color must be between 2 and 50 characters")
    private String color;

    @NotNull(message = "Status must be specified")
    private CarStatuses carStatuses;

    public Car() {
    }

    public Car(CarModel model, String plateNumber, String color, CarStatuses carStatuses) {
        this.model = model;
        this.plateNumber = plateNumber;
        this.color = color;
        this.carStatuses = carStatuses;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CarModel getModel() {
        return model;
    }

    public void setModel(CarModel model) {
        this.model = model;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public CarStatuses getCarStatuses() {
        return carStatuses;
    }

    public void setCarStatuses(CarStatuses carStatuses) {
        this.carStatuses = carStatuses;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return id == car.id && Objects.equals(model, car.model) &&
               Objects.equals(plateNumber, car.plateNumber) && Objects.equals(color, car.color) &&
               carStatuses == car.carStatuses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, model, plateNumber, color, carStatuses);
    }

    @Override
    public String toString() {
        return "Car{" +
               "id=" + id +
               ", model=" + model +
               ", plateNumber='" + plateNumber + '\'' +
               ", color='" + color + '\'' +
               ", carStatuses=" + carStatuses +
               '}';
    }
}
