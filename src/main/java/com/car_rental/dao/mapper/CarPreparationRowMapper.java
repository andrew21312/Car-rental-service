package com.car_rental.dao.mapper;

import static com.car_rental.constants.ControllerConstants.APPROVED;

import com.car_rental.entity.RentalExtra;
import com.car_rental.form.car.CarPreparationDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CarPreparationRowMapper implements RowMapper<CarPreparationDTO> {

    private static final int PREPARE_BUTTON_THRESHOLD_DAYS = 2;
    private static final int RENTAL_EXTRA_PARTS_COUNT = 3;
    private static final String EXTRAS_DELIMITER = ";";
    private static final String EXTRA_PARTS_DELIMITER = ":";

    @Override
    public CarPreparationDTO mapRow(@NonNull ResultSet resultSet, int rowNumber) throws SQLException {
        CarPreparationDTO preparationDTO = createCarPreparationDTO(resultSet);

        String status = resultSet.getString("status");
        preparationDTO.setStatus(status);

        boolean shouldShowPrepareButton = shouldShowPrepareButton(resultSet, status);
        preparationDTO.setShowPrepareButton(shouldShowPrepareButton);

        List<RentalExtra> rentalExtras = parseRentalExtras(resultSet.getString("extras"));
        preparationDTO.setRentalExtras(rentalExtras);

        return preparationDTO;
    }

    private CarPreparationDTO createCarPreparationDTO(ResultSet resultSet) throws SQLException {
        return new CarPreparationDTO(
                resultSet.getInt("car_id"),
                resultSet.getString("car_plate_number"),
                resultSet.getString("name"),
                resultSet.getInt("rental_id"),
                resultSet.getDate("next_rental_date").toLocalDate()
        );
    }

    private boolean shouldShowPrepareButton(ResultSet resultSet, String status) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate rentalDate = resultSet.getDate("next_rental_date").toLocalDate();
        long daysUntilRental = ChronoUnit.DAYS.between(today, rentalDate);

        return daysUntilRental <= PREPARE_BUTTON_THRESHOLD_DAYS &&
               APPROVED.equalsIgnoreCase(status);
    }

    private List<RentalExtra> parseRentalExtras(String extrasString) {
        if (extrasString == null || extrasString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(extrasString.split(EXTRAS_DELIMITER))
                .map(this::parseRentalExtra)
                .filter(Objects::nonNull)
                .toList();
    }

    private RentalExtra parseRentalExtra(String extraString) {
        if (extraString == null || extraString.trim().isEmpty()) {
            return null;
        }

        try {
            return getRentalExtra(extraString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid number format in rental extra: %s", extraString), e
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse rental extra: %s", extraString), e
            );
        }
    }

    private RentalExtra getRentalExtra(String extraString) {
        String[] parts = extraString.split(EXTRA_PARTS_DELIMITER);
        if (parts.length != RENTAL_EXTRA_PARTS_COUNT) {
            throw new IllegalArgumentException(
                    String.format("Expected %d parts but got %d in: %s",
                                  RENTAL_EXTRA_PARTS_COUNT, parts.length, extraString)
            );
        }

        RentalExtra rentalExtra = new RentalExtra();
        rentalExtra.setId(Integer.parseInt(parts[0].trim()));
        rentalExtra.setName(parts[1].trim());
        rentalExtra.setPrice(Double.parseDouble(parts[2].trim()));
        return rentalExtra;
    }
}