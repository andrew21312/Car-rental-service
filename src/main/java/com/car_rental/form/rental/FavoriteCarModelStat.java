package com.car_rental.form.rental;

public class FavoriteCarModelStat {
    private String modelName;
    private int year;
    private int rentalCount;
    private int totalRentalDays;

    public FavoriteCarModelStat(String modelName, int year, int rentalCount, int totalRentalDays) {
        this.modelName = modelName;
        this.year = year;
        this.rentalCount = rentalCount;
        this.totalRentalDays = totalRentalDays;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRentalCount() {
        return rentalCount;
    }

    public void setRentalCount(int rentalCount) {
        this.rentalCount = rentalCount;
    }

    public int getTotalRentalDays() {
        return totalRentalDays;
    }

    public void setTotalRentalDays(int totalRentalDays) {
        this.totalRentalDays = totalRentalDays;
    }
}
