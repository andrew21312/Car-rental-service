package com.car_rental.entity;

import java.util.Objects;

public class CarStatuses {
    private int id;
    private String carStatusName;

    public CarStatuses() {
    }

    public CarStatuses(int id, String carStatusName) {
        this.id = id;
        this.carStatusName = carStatusName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCarStatusName() {
        return carStatusName;
    }

    public void setCarStatusName(String carStatusName) {
        this.carStatusName = carStatusName;
    }

    public String getCarStatusNameLowerCase() {
        return carStatusName.toLowerCase();
    }

    public String getCarStatusNameCapitalized() {
        return carStatusName.substring(0, 1).toUpperCase() + carStatusName.substring(1).toLowerCase();
    }

    @Override
    public String toString() {
        return "CarStatuses{" +
               "id=" + id +
               ", carStatusName='" + carStatusName + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CarStatuses that = (CarStatuses) o;
        return id == that.id && Objects.equals(carStatusName, that.carStatusName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, carStatusName);
    }
}
