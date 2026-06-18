package com.car_rental.form.rental;

import java.time.LocalDate;

/**
 * Read-only projection summarizing a client's rental expenses for a selected period.
 * Includes only rentals with ACTIVE or COMPLETED statuses.
 */
public class ExpensesReport {
    private final int rentalCount;
    private final double totalSpent;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ExpensesReport(int rentalCount, double totalSpent, LocalDate startDate, LocalDate endDate) {
        this.rentalCount = rentalCount;
        this.totalSpent = totalSpent;
        this.startDate = startDate;
        this.endDate = endDate;
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
}
