package com.car_rental.service;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Rental;
import com.car_rental.form.rental.FavoriteCarModelStat;

import java.time.LocalDate;
import java.util.List;

public interface RentalService {
    void addRental(Rental rental);

    void updateRentalStatus(Rental rental, String newStatus);

    void updateRentalPaymentStatus(Rental rental);

    Rental getRentalById(int id);

    PageResult<Rental> getRentalsPage(Integer status, Integer rentalId, String carPlateNumber, String clientLastName,
                                      int page, int size, String sortBy);

    List<String> getBlockedDatesForCar(int carId);

    PageResult<Rental> getClientRentalsPage(int clientId, int page, int size);

    boolean isValidStatusTransition(String currentStatus, String newStatus);

    boolean isCarAvailable(int carId, LocalDate startDate, LocalDate endDate);

    void cancelRental(int rentalId, Integer currentUserId);

    void updateRentalExtras(int rentalId, List<Integer> newExtraIds);

    public List<FavoriteCarModelStat> getFavoriteCarModelsLastMonth();

}
