package com.car_rental.form.rental;

import com.car_rental.entity.Rental;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only projection summarizing a client's rental expenses for a selected period.
 * Includes only rentals with ACTIVE or COMPLETED statuses, along with the individual
 * rentals that make up the totals.
 */
public class ExpensesReport {
    private final int rentalCount;
    private final double totalSpent;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<Rental> rentals;

    public ExpensesReport(int rentalCount, double totalSpent, LocalDate startDate, LocalDate endDate,
                          List<Rental> rentals) {
        this.rentalCount = rentalCount;
        this.totalSpent = totalSpent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentals = rentals;
    }

    public int getRentalCount() {
        return rentalCount;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<Rental> getRentals() {
        return rentals;
    }
}
