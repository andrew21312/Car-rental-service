package com.car_rental.form.rental;

import com.car_rental.entity.Rental;
import com.car_rental.entity.RentalStatuses;
import java.util.Objects;

public class RentalStatusUpdateDTO {
    protected int id;

    protected PaymentStatus paymentStatus;

    protected RentalStatuses rentalStatuses;

    public RentalStatusUpdateDTO() {
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // Converting from Rental to RentalStatusUpdateDTO
    public RentalStatusUpdateDTO(Rental rental) {
        this.paymentStatus = rental.getPaymentStatus();
        this.rentalStatuses = rental.getRentalStatuses();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public RentalStatuses getRentalStatuses() {
        return rentalStatuses;
    }

    public void setRentalStatuses(RentalStatuses rentalStatuses) {
        this.rentalStatuses = rentalStatuses;
    }

    public String getRentalStatusName() {
        return this.rentalStatuses.getName();
    }

    public int getRentalStatusId() {
        return this.rentalStatuses.getId();
    }

    @Override
    public String toString() {
        return "RentalStatusUpdateDTO{" +
               "id=" + id +
               ", paymentStatus=" + paymentStatus +
               ", rentalStatuses=" + rentalStatuses +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RentalStatusUpdateDTO that = (RentalStatusUpdateDTO) o;
        return id == that.id && paymentStatus == that.paymentStatus &&
               Objects.equals(rentalStatuses, that.rentalStatuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paymentStatus, rentalStatuses);
    }

    public enum PaymentStatus {
        PENDING, PAID, CANCELLED;

        public String getLowercaseName() {
            return this.name().toLowerCase();
        }
    }
}
