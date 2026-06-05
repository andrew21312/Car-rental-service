package com.car_rental.entity;

import com.car_rental.form.rental.RentalDTO;
import com.car_rental.form.rental.RentalStatusUpdateDTO;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Rental extends RentalStatusUpdateDTO {


    private User client;
    private int carId;

    // Додаткові поля-знімки інформації про автомобіль на момент оренди
    private String plateNumber;
    private String brand;
    private String model;
    private int year;
    private double price;

    @NotNull(message = "Start date must be specified")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date must be specified")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    private List<RentalExtra> rentalExtras;

    private double totalCost;

    private Timestamp createdAt;

    private Timestamp updatedAt;


    public Rental() {
        this.paymentStatus = PaymentStatus.PENDING;
        this.totalCost = 0;
        this.rentalExtras = new ArrayList<>();
    }

    public Rental(RentalStatusUpdateDTO rentalStatusUpdateDTO) {
        this.id = rentalStatusUpdateDTO.getId();
        this.paymentStatus = rentalStatusUpdateDTO.getPaymentStatus();
        this.rentalStatuses = rentalStatusUpdateDTO.getRentalStatuses();
    }

    public Rental(RentalDTO rentalDTO) {
        this.id = rentalDTO.getId();
        this.client = rentalDTO.getClient();
        this.paymentStatus = rentalDTO.getPaymentStatus();
        this.totalCost = rentalDTO.getTotalCost();
        this.rentalStatuses = rentalDTO.getRentalStatuses();
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public int getClientId() {
        return client.getId();
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    // Snapshot
    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCarModelInfo() {
        return brand + " " + model + " " + year;
    }

    //---------------------------------

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public List<RentalExtra> getRentalExtras() {
        return rentalExtras;
    }

    public void setRentalExtras(List<RentalExtra> rentalExtras) {
        this.rentalExtras = rentalExtras;
    }

    public List<Integer> getRentalExtrasIds() {
        return rentalExtras.stream().map(RentalExtra::getId).toList();
    }

    public String getExtrasTotalCost() {
        double extrasCost = 0.0;
        for (RentalExtra extra : this.rentalExtras) {
            extrasCost += extra.getPrice();
        }
        return String.format("%.2f", extrasCost);
    }

    public void calculateTotalCost() {
        // Обчислюємо кількість днів між датами
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        // Розраховуємо загальну вартість, враховуючи кількість днів + вартість додаткових послуг
        if (!rentalExtras.isEmpty()) {
            this.totalCost =
                    (getPrice() + rentalExtras.stream().mapToDouble(RentalExtra::getPrice).sum()) *
                    daysBetween;
        } else {
            this.totalCost = getPrice() * daysBetween;
        }
    }

    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate); // включно з обома
    }

    public String getStartDateFormated() {
        return startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public String getEndDateFormated() {
        return endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public String getFormattedCreatedAt() {
        return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String getFormattedUpdatedAt() {
        return updatedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getClientFullName() {
        return client.getFirstName() + " " + client.getLastName();
    }

    @Override
    public String toString() {
        return "Rental{" +
               "client=" + client +
               ", carId=" + carId +
               ", plateNumber='" + plateNumber + '\'' +
               ", brand='" + brand + '\'' +
               ", model='" + model + '\'' +
               ", year=" + year +
               ", price=" + price +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", rentalExtras=" + rentalExtras +
               ", totalCost=" + totalCost +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", id=" + id +
               ", paymentStatus=" + paymentStatus +
               ", rentalStatuses=" + rentalStatuses +
               "}; ";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Rental rental = (Rental) o;
        return carId == rental.carId && year == rental.year && Double.compare(price, rental.price) == 0 &&
               Double.compare(totalCost, rental.totalCost) == 0 &&
               Objects.equals(client, rental.client) &&
               Objects.equals(plateNumber, rental.plateNumber) && Objects.equals(brand, rental.brand) &&
               Objects.equals(model, rental.model) && Objects.equals(startDate, rental.startDate) &&
               Objects.equals(endDate, rental.endDate) &&
               Objects.equals(rentalExtras, rental.rentalExtras) &&
               Objects.equals(createdAt, rental.createdAt) &&
               Objects.equals(updatedAt, rental.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), client, carId, plateNumber, brand, model, year, price, startDate, endDate,
                            rentalExtras, totalCost, createdAt, updatedAt);
    }
}
