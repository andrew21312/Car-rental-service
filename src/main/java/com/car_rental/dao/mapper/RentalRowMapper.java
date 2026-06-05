package com.car_rental.dao.mapper;

import com.car_rental.entity.Rental;
import com.car_rental.entity.RentalStatuses;
import com.car_rental.entity.User;
import com.car_rental.form.rental.RentalStatusUpdateDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RentalRowMapper implements RowMapper<Rental> {
    private final UserRowMapper userRowMapper;
    private final RentalStatusesRowMapper rentalStatusesRowMapper;

    @Autowired
    public RentalRowMapper(UserRowMapper userRowMapper,
                           RentalStatusesRowMapper rentalStatusesRowMapper) {
        this.userRowMapper = userRowMapper;
        this.rentalStatusesRowMapper = rentalStatusesRowMapper;
    }

    @Override
    public Rental mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Rental rental = new Rental();
        RentalStatuses rentalStatuses = rentalStatusesRowMapper.mapRow(rs, rowNum);
        User client;
        if (rs.getInt("rental_client_id") != 0) {
            client = userRowMapper.mapRow(rs, rowNum);
        } else {
            client = new User();
            client.markAsDeleted();
        }

        rental.setId(rs.getInt("rental_id"));
        rental.setClient(client);
        rental.setCarId(rs.getInt("rental_car_id"));

        // Snapshots
        rental.setPlateNumber(rs.getString("rental_plate_number"));
        rental.setModel(rs.getString("rental_car_model"));
        rental.setBrand(rs.getString("rental_car_brand"));
        rental.setYear(rs.getInt("rental_car_year"));
        rental.setPrice(rs.getDouble("rental_daily_rate"));

        rental.setStartDate(rs.getDate("rental_start_date").toLocalDate());
        rental.setEndDate(rs.getDate("rental_end_date").toLocalDate());
        rental.setTotalCost(rs.getDouble("rental_total_cost"));
        rental.setCreatedAt(rs.getTimestamp("rental_created_at"));
        rental.setUpdatedAt(rs.getTimestamp("rental_updated_at"));
        rental.setPaymentStatus(RentalStatusUpdateDTO.PaymentStatus.valueOf(rs.getString("rental_payment_status")));
        rental.setRentalStatuses(rentalStatuses);
        return rental;
    }
}
