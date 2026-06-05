package com.car_rental.form.rental;

import com.car_rental.entity.Car;
import com.car_rental.entity.User;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RentalDTO extends RentalStatusUpdateDTO {

    private User client;

    private Car car;

    @NotBlank
    private String dateRange;

    private List<Integer> selectedExtraIds;

    private double totalCost;


    public RentalDTO() {
        this.paymentStatus = PaymentStatus.PENDING;
        this.totalCost = 0;
        this.selectedExtraIds = new ArrayList<>();
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

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public List<Integer> getSelectedExtraIds() {
        return selectedExtraIds;
    }

    public void setSelectedExtraIds(List<Integer> selectedExtraIds) {
        this.selectedExtraIds = selectedExtraIds;
    }

    public String getClientFullName() {
        return client.getFirstName() + " " + client.getLastName();
    }

    @Override
    public String toString() {
        return "RentalDTO{" +
               "client=" + client +
               ", car=" + car +
               ", dateRange='" + dateRange + '\'' +
               ", selectedExtraIds=" + selectedExtraIds +
               ", totalCost=" + totalCost +
               ", id=" + id +
               ", paymentStatus=" + paymentStatus +
               ", rentalStatuses=" + rentalStatuses +
               "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RentalDTO rentalDTO = (RentalDTO) o;
        return Double.compare(totalCost, rentalDTO.totalCost) == 0 &&
               Objects.equals(client, rentalDTO.client) && Objects.equals(car, rentalDTO.car) &&
               Objects.equals(dateRange, rentalDTO.dateRange) &&
               Objects.equals(selectedExtraIds, rentalDTO.selectedExtraIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), client, car, dateRange, selectedExtraIds, totalCost);
    }
}
