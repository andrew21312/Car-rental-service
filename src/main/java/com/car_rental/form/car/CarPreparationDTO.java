package com.car_rental.form.car;

import com.car_rental.entity.RentalExtra;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CarPreparationDTO {
    private int carId;
    private String plateNumber;
    private String modelName;
    private int rentalId;
    private LocalDate pickupDate;
    private List<RentalExtra> rentalExtras;
    private boolean showPrepareButton;
    private String status;

    public CarPreparationDTO() {
    }

    public CarPreparationDTO(int carId, String plateNumber, String modelName, int rentalId, LocalDate pickupDate) {
        this.carId = carId;
        this.plateNumber = plateNumber;
        this.modelName = modelName;
        this.rentalId = rentalId;
        this.pickupDate = pickupDate;
    }

    public List<RentalExtra> getRentalExtras() {
        return rentalExtras;
    }

    public void setRentalExtras(List<RentalExtra> rentalExtras) {
        this.rentalExtras = rentalExtras;
    }

    public int getCarId() {
        return carId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public int getRentalId() {
        return rentalId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public String getFormattedNextRentalDate() {
        return pickupDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public String getAvailableDate() {
        LocalDate availableDate = pickupDate.minusDays(2);
        return "Available from " + availableDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public boolean isShowPrepareButton() {
        return showPrepareButton;
    }

    public void setShowPrepareButton(boolean showPrepareButton) {
        this.showPrepareButton = showPrepareButton;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
