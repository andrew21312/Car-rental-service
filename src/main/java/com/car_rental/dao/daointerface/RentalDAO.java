package com.car_rental.dao.daointerface;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Rental;
import com.car_rental.form.rental.FavoriteCarModelStat;

import java.time.LocalDate;
import java.util.List;

public interface RentalDAO {
    void addRental(Rental rental);

    void updateRentalStatus(Rental rental);

    void updateRentalPaymentStatus(Rental rental);

    Rental getRentalById(int id);

    PageResult<Rental> getRentalsPage(Integer status, Integer rentalId, String carPlateNumber, String clientLastName,
                                      int page, int size, String sortBy);

    List<String> getBlockedDatesForCar(int carId, int status1, int status2);

    PageResult<Rental> getClientRentalsPage(int clientId, String filter, int page, int size);

    List<Rental> getClientReportRentals(int clientId, LocalDate startDate, LocalDate endDate);

    LocalDate getClientEarliestReportableRentalDate(int clientId);

    boolean isCarAvailable(int carId, LocalDate startDate, LocalDate endDate, int status1, int status2);

    void updateRentalExtras(int rentalId, List<Integer> newExtraIds);

    List<FavoriteCarModelStat> findFavoriteCarModelsLastMonth();
}
