package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.RentalDAO;
import com.car_rental.dao.mapper.CarBlockedDatesRowMapper;
import com.car_rental.dao.mapper.RentalExtraRowMapper;
import com.car_rental.dao.mapper.RentalRowMapper;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.Rental;
import com.car_rental.entity.RentalExtra;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.car_rental.form.rental.FavoriteCarModelStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MySqlRentalDAO extends BasePaginationDAO implements RentalDAO {

    private static final Logger logger = LoggerFactory.getLogger(MySqlRentalDAO.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // SQL Queries
    private static final String SELECT_ALL_RENTALS = """
            SELECT
                rentals.id as rental_id,
                rentals.client_id as rental_client_id,
                rentals.car_id as rental_car_id,
                rentals.plate_number as rental_plate_number,
                rentals.car_model_name as rental_car_model,
                rentals.car_brand as rental_car_brand,
                rentals.car_year as rental_car_year,
                rentals.daily_rate as rental_daily_rate,
                rentals.start_date as rental_start_date,
                rentals.end_date as rental_end_date,
                rentals.payment_status as rental_payment_status,
                rentals.total_cost as rental_total_cost,
                rentals.created_at as rental_created_at,
                rentals.updated_at as rental_updated_at,
            
                users.id as user_id,
                users.username as user_username,
                users.first_name as user_first_name,
                users.last_name as user_last_name,
                users.password_hash as user_password_hash,
                users.created_at as user_created_at,
                users.phone_number as user_phone_number,
            
                roles.id as role_id,
                roles.name as role_name,
                roles.description as role_description,
            
                rental_statuses.id as rental_status_id,
                rental_statuses.name as rental_status_name
            
            FROM rentals
            LEFT JOIN users ON users.id = rentals.client_id
            LEFT JOIN roles ON users.role_id = roles.id
            JOIN rental_statuses ON rentals.status_id = rental_statuses.id
            """;

    private static final String SELECT_RENTAL_EXTRAS_BY_RENTAL_ID = """
            SELECT
                rental_extra_assignments.extra_id,
                rental_extra_assignments.extra_name,
                rental_extra_assignments.extra_price
            FROM rental_extra_assignments
            WHERE rental_id = ?
            """;

    private static final String INSERT_RENTAL = """
            INSERT INTO rentals (client_id, car_id, plate_number, car_model_name,
                car_brand, car_year, daily_rate, start_date, end_date,
                payment_status, status_id, total_cost)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_RENTAL_EXTRA_IDS_BY_RENTAL_ID =
            "SELECT extra_id FROM rental_extra_assignments WHERE rental_id = ?";

    private static final String UPDATE_RENTAL_STATUS =
            "UPDATE rentals SET payment_status = ?, status_id = ? WHERE id = ?";

    private static final String UPDATE_RENTAL_PAYMENT_STATUS =
            "UPDATE rentals SET payment_status = ? WHERE id = ?";

    private static final String SELECT_RENTAL_BY_ID = SELECT_ALL_RENTALS + " WHERE rentals.id = ?";

    private static final String SELECT_BLOCKED_DATES_BY_CAR_ID = """
            SELECT start_date as rental_start_date,
                   end_date as rental_end_date,
                   car_id as rental_car_id
            FROM rentals
            WHERE car_id = ? AND status_id NOT IN (?, ?)
            """;

    private static final String SELECT_RENTALS_BY_CLIENT_ID = SELECT_ALL_RENTALS + " WHERE rentals.client_id = ?";

    private static final String SELECT_CAR_STATUS_BY_CAR_ID = """
            SELECT car_statuses.name
            FROM cars
            JOIN car_statuses ON cars.status_id = car_statuses.id
            WHERE cars.id = ?
            """;

    private static final String INSERT_RENTAL_EXTRA_ASSIGNMENT =
            "INSERT INTO rental_extra_assignments (rental_id, extra_id, extra_name, extra_price) VALUES (?, ?, ?, ?)";

    private static final String DELETE_RENTAL_EXTRA_ASSIGNMENT =
            "DELETE FROM rental_extra_assignments WHERE rental_id = ? AND extra_id = ?";

    private static final String UPDATE_RENTAL_TOTAL_COST =
            "UPDATE rentals SET total_cost = ? WHERE id = ?";

    private static final String COUNT_RENTALS_QUERY = """
            SELECT COUNT(*) FROM rentals
            LEFT JOIN users ON rentals.client_id = users.id
            """;

    private static final String COUNT_CLIENT_RENTALS_QUERY =
            "SELECT COUNT(*) FROM rentals WHERE client_id = ?";

    private static final String FAVORITE_CAR_MODELS_LAST_MONTH = """
                SELECT
                r.car_model_name AS modelName,
                cm.year AS year,
                COUNT(*) AS rentalCount,
                SUM(DATEDIFF(r.end_date, r.start_date) + 1) AS totalRentalDays
                FROM rentals r
                JOIN car_models cm ON r.car_model_name = cm.name
                WHERE 
                    r.start_date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
                    AND r.status_id = 6
                GROUP BY r.car_model_name, cm.year
                ORDER BY rentalCount DESC, totalRentalDays DESC
            """;
    // Dependencies
    private final RentalRowMapper rentalRowMapper;
    private final RentalExtraRowMapper rentalExtraRowMapper;
    private final CarBlockedDatesRowMapper carBlockedDatesRowMapper;

    @Autowired
    public MySqlRentalDAO(JdbcTemplate jdbcTemplate,
                          RentalRowMapper rentalRowMapper,
                          RentalExtraRowMapper rentalExtraRowMapper,
                          CarBlockedDatesRowMapper carBlockedDatesRowMapper) {
        super(jdbcTemplate);
        this.rentalRowMapper = rentalRowMapper;
        this.rentalExtraRowMapper = rentalExtraRowMapper;
        this.carBlockedDatesRowMapper = carBlockedDatesRowMapper;
    }

    @Override
    public List<FavoriteCarModelStat> findFavoriteCarModelsLastMonth() {
        return jdbcTemplate.query(FAVORITE_CAR_MODELS_LAST_MONTH,
                (rs, rowNum) -> new FavoriteCarModelStat(
                        rs.getString("modelName"),
                        rs.getInt("year"),
                        rs.getInt("rentalCount"),
                        rs.getInt("totalRentalDays")
                )
        );
    }

    @Override
    @Transactional
    public void addRental(Rental rental) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(INSERT_RENTAL, Statement.RETURN_GENERATED_KEYS);
            setRentalParameters(preparedStatement, rental);
            return preparedStatement;
        }, keyHolder);

        rental.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        addRentalExtras(rental);
    }

    private void setRentalParameters(PreparedStatement statement, Rental rental) throws SQLException {
        int parameterIndex = 0;
        statement.setInt(++parameterIndex, rental.getClientId());
        statement.setInt(++parameterIndex, rental.getCarId());
        statement.setString(++parameterIndex, rental.getPlateNumber());
        statement.setString(++parameterIndex, rental.getModel());
        statement.setString(++parameterIndex, rental.getBrand());
        statement.setInt(++parameterIndex, rental.getYear());
        statement.setDouble(++parameterIndex, rental.getPrice());
        statement.setDate(++parameterIndex, Date.valueOf(rental.getStartDate()));
        statement.setDate(++parameterIndex, Date.valueOf(rental.getEndDate()));
        statement.setString(++parameterIndex, rental.getPaymentStatus().name());
        statement.setInt(++parameterIndex, rental.getRentalStatuses().getId());
        statement.setDouble(++parameterIndex, rental.getTotalCost());
    }

    private void addRentalExtras(Rental rental) {
        if (!rental.getRentalExtras().isEmpty()) {
            for (RentalExtra rentalExtra : rental.getRentalExtras()) {
                jdbcTemplate.update(INSERT_RENTAL_EXTRA_ASSIGNMENT,
                        rental.getId(), rentalExtra.getId(), rentalExtra.getName(), rentalExtra.getPrice());
            }
        }
    }

    @Override
    public void updateRentalStatus(Rental rental) {
        jdbcTemplate.update(UPDATE_RENTAL_STATUS,
                rental.getPaymentStatus().name(),
                rental.getRentalStatuses().getId(),
                rental.getId());
    }

    @Override
    public void updateRentalPaymentStatus(Rental rental) {
        jdbcTemplate.update(UPDATE_RENTAL_PAYMENT_STATUS,
                rental.getPaymentStatus().name(),
                rental.getId());
    }

    @Override
    public Rental getRentalById(int rentalId) {
        Rental rental = jdbcTemplate.queryForObject(SELECT_RENTAL_BY_ID, rentalRowMapper, rentalId);

        List<RentalExtra> extras =
                jdbcTemplate.query(SELECT_RENTAL_EXTRAS_BY_RENTAL_ID, rentalExtraRowMapper, rentalId);

        if (!extras.isEmpty()) {
            rental.setRentalExtras(extras);
        }

        return rental;
    }

    @Override
    public PageResult<Rental> getRentalsPage(Integer statusId, Integer rentalId, String carPlateNumber,
                                             String clientLastName, int page, int size, String sortBy) {
        QueryParams queryParams = buildQueryParams(statusId, rentalId, carPlateNumber, clientLastName);
        String orderByClause = buildOrderByClause(sortBy);

        return executePaginationQuery(
                SELECT_ALL_RENTALS,
                COUNT_RENTALS_QUERY,
                rentalRowMapper,
                queryParams,
                page,
                size,
                orderByClause
        );
    }

    private String buildOrderByClause(String sortBy) {
        return switch (sortBy) {
            case "createdAtAsc" -> "rentals.created_at ASC";
            case "startDateDesc" -> "rentals.start_date DESC";
            case "startDateAsc" -> "rentals.start_date ASC";
            case "rentalIdDesc" -> "rentals.id DESC";
            case "rentalIdAsc" -> "rentals.id ASC";
            default -> "rentals.created_at DESC";
        };
    }

    @Override
    public List<String> getBlockedDatesForCar(int carId, int excludedStatus1, int excludedStatus2) {
        List<Rental> rentals = getRentalDaysForCar(carId, excludedStatus1, excludedStatus2);
        Set<String> blockedDates = new HashSet<>();

        for (Rental rental : rentals) {
            LocalDate currentDate = rental.getStartDate();
            LocalDate endDate = rental.getEndDate();

            while (!currentDate.isAfter(endDate)) {
                blockedDates.add(currentDate.format(DATE_FORMATTER));
                currentDate = currentDate.plusDays(1);
            }
        }

        return new ArrayList<>(blockedDates);
    }

    @Override
    public PageResult<Rental> getClientRentalsPage(int clientId, int page, int size) {
        int offset = page * size;

        List<Rental> rentals = jdbcTemplate.query(
                SELECT_RENTALS_BY_CLIENT_ID + " ORDER BY rental_id DESC LIMIT ? OFFSET ?",
                rentalRowMapper, clientId, size, offset);
        loadRentalExtrasForRentals(rentals);

        Long totalElements = jdbcTemplate.queryForObject(COUNT_CLIENT_RENTALS_QUERY, Long.class, clientId);

        return new PageResult<>(rentals, page, size, totalElements);
    }

    private void loadRentalExtrasForRentals(List<Rental> rentals) {
        for (Rental rental : rentals) {
            List<RentalExtra> extras = jdbcTemplate.query(
                    SELECT_RENTAL_EXTRAS_BY_RENTAL_ID,
                    rentalExtraRowMapper,
                    rental.getId());
            rental.setRentalExtras(extras);
        }
    }

    @Override
    public boolean isCarAvailable(int carId, LocalDate startDate, LocalDate endDate,
                                  int excludedStatus1, int excludedStatus2) {
        if (!isCarStatusAvailable(carId)) {
            return false;
        }

        List<Rental> rentals = getRentalDaysForCar(carId, excludedStatus1, excludedStatus2);

        for (Rental rental : rentals) {
            if (isDateRangeOverlapping(startDate, endDate, rental.getStartDate(), rental.getEndDate())) {
                return false;
            }
        }

        return true;
    }

    private boolean isCarStatusAvailable(int carId) {
        String carStatus = jdbcTemplate.queryForObject(SELECT_CAR_STATUS_BY_CAR_ID, String.class, carId);
        return carStatus != null && carStatus.equalsIgnoreCase("AVAILABLE");
    }

    private boolean isDateRangeOverlapping(LocalDate requestedStart, LocalDate requestedEnd,
                                           LocalDate existingStart, LocalDate existingEnd) {
        return !(existingEnd.isBefore(requestedStart) || existingStart.isAfter(requestedEnd));
    }

    private List<Rental> getRentalDaysForCar(int carId, int excludedStatus1, int excludedStatus2) {
        return jdbcTemplate.query(SELECT_BLOCKED_DATES_BY_CAR_ID, carBlockedDatesRowMapper,
                carId, excludedStatus1, excludedStatus2);
    }

    @Transactional
    @Override
    public void updateRentalExtras(int rentalId, List<Integer> newExtraIds) {
        List<Integer> currentExtraIds = jdbcTemplate.queryForList(
                SELECT_RENTAL_EXTRA_IDS_BY_RENTAL_ID, Integer.class, rentalId);

        Set<Integer> newExtrasSet = new HashSet<>(newExtraIds);
        Set<Integer> currentExtrasSet = new HashSet<>(currentExtraIds);

        Set<Integer> extrasToAdd = new HashSet<>(newExtrasSet);
        extrasToAdd.removeAll(currentExtrasSet);

        Set<Integer> extrasToDelete = new HashSet<>(currentExtrasSet);
        extrasToDelete.removeAll(newExtrasSet);

        deleteRentalExtras(rentalId, extrasToDelete);
        addRentalExtras(rentalId, extrasToAdd);

        recalculateAndUpdateTotalCost(rentalId);
    }

    private void deleteRentalExtras(int rentalId, Set<Integer> extraIds) {
        for (Integer extraId : extraIds) {
            jdbcTemplate.update(DELETE_RENTAL_EXTRA_ASSIGNMENT, rentalId, extraId);
        }
    }

    private void addRentalExtras(int rentalId, Set<Integer> extraIds) {
        for (Integer extraId : extraIds) {
            jdbcTemplate.update(INSERT_RENTAL_EXTRA_ASSIGNMENT, rentalId, extraId);
        }
    }

    private void recalculateAndUpdateTotalCost(int rentalId) {
        Rental rental = getRentalById(rentalId);

        if (rental.getStartDate() == null || rental.getEndDate() == null) {
            throw new IllegalArgumentException("Rental must have start and end dates set to calculate total cost.");
        }

        rental.calculateTotalCost();
        updateRentalTotalCost(rental);
    }

    private void updateRentalTotalCost(Rental rental) {
        jdbcTemplate.update(UPDATE_RENTAL_TOTAL_COST, rental.getTotalCost(), rental.getId());
    }

    private QueryParams buildQueryParams(Integer statusId, Integer rentalId, String carPlateNumber,
                                         String clientLastName) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (rentalId != null) {
            conditions.add("rentals.id = ?");
            parameters.add(rentalId);
        }

        if (carPlateNumber != null && !carPlateNumber.trim().isEmpty()) {
            conditions.add("rentals.plate_number LIKE ?");
            parameters.add("%" + carPlateNumber.trim() + "%");
        }

        if (clientLastName != null && !clientLastName.trim().isEmpty()) {
            conditions.add("users.last_name LIKE ?");
            parameters.add("%" + clientLastName.trim() + "%");
        }

        if (statusId != null) {
            conditions.add("rentals.status_id = ?");
            parameters.add(statusId);
        }

        return new QueryParams(conditions, parameters);
    }
}