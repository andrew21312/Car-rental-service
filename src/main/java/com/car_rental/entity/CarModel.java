package com.car_rental.entity;

import jakarta.validation.constraints.*;
import java.util.Objects;

public class CarModel {
    private int id;

    @NotNull(message = "Price must be specified")
    @Positive(message = "Price must be positive")
    @Digits(integer = 6, fraction = 2, message = "Price must have at most 6 digits and 2 decimal places")
    private double price;

    @NotBlank(message = "Brand cannot be blank")
    @Size(min = 2, max = 50, message = "Brand name must be between 2 and 50 characters")
    private String brand;

    @NotBlank(message = "Model cannot be blank")
    @Size(min = 1, max = 50, message = "Model name must be between 1 and 50 characters")
    private String model;

    @Min(value = 1886, message = "Year must be 1886 or later") // Перший автомобіль у 1886 році
    private int year;

    @NotNull(message = "Engine type must be specified")
    private EngineType engineType;

    @Min(value = 1, message = "Car must have at least 1 seat")
    private int seats;

    @NotNull(message = "Transmission must be specified")
    private Transmission transmission;


    public CarModel() {
    }

    public CarModel(double price, String brand, String model, int year, EngineType engineType, int seats,
                    Transmission transmission) {
        this.price = price;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.engineType = engineType;
        this.seats = seats;
        this.transmission = transmission;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public EngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(EngineType engineType) {
        this.engineType = engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = EngineType.valueOf(engineType);
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = Transmission.valueOf(transmission);
    }

    public String getCarModelInfo() {
        return brand + " " + model + " (" + year + ")";
    }

    @Override
    public String toString() {
        return "CarModel{" +
               "id=" + id +
               ", price=" + price +
               ", brand='" + brand + '\'' +
               ", model='" + model + '\'' +
               ", year=" + year +
               ", engineType=" + engineType +
               ", seats=" + seats +
               ", transmission=" + transmission +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CarModel carModel = (CarModel) o;
        return id == carModel.id && Double.compare(price, carModel.price) == 0 && year == carModel.year &&
               seats == carModel.seats && Objects.equals(brand, carModel.brand) &&
               Objects.equals(model, carModel.model) && engineType == carModel.engineType &&
               transmission == carModel.transmission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, brand, model, year, engineType, seats, transmission);
    }

    public enum EngineType {
        PETROL, DIESEL, ELECTRIC, HYBRID;

        public String getCapitalizedName() {
            String name = this.name().replace("_", " ").toLowerCase();
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public enum Transmission {
        MANUAL, AUTOMATIC;

        public String getCapitalizedName() {
            String name = this.name().replace("_", " ").toLowerCase();
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }
}
