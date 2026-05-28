package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.CarDAO;
import com.car_rental.dao.mapper.CarRowMapper;
import com.car_rental.entity.Car;
import com.car_rental.entity.PageResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MySqlCarDAO extends BasePaginationDAO implements CarDAO {

    private static final String SELECT_ALL_CARS_QUERY = """
            SELECT
                cars.id as car_id,
                cars.plate_number as car_plate_number,
                cars.color as car_color,
                cars.status_id as car_status_id,
                car_statuses.name as car_status_name,
            
                car_models.id as car_model_id,
                car_models.price as car_model_price,
                car_models.brand as car_model_brand,
                car_models.name as car_model_name,
                car_models.year as car_model_year,
                car_models.engine_type as car_model_engine_type,
                car_models.seats as car_model_seats,
                car_models.transmission as car_model_transmission
            
            FROM cars
                JOIN car_models ON car_models.id = cars.model_id
                JOIN car_statuses ON car_statuses.id = cars.status_id
            """;

    private static final String COUNT_CARS_QUERY =
            "SELECT COUNT(*) FROM cars JOIN car_models ON car_models.id = cars.model_id";

    private static final String INSERT_CAR_QUERY =
            "INSERT INTO cars (model_id, plate_number, color, status_id) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_CAR_QUERY =
            "UPDATE cars SET model_id = ?, plate_number = ?, color = ?, status_id = ? WHERE id = ?";

    private static final String DELETE_CAR_QUERY =
            "DELETE FROM cars WHERE id = ?";

    private static final String SELECT_CAR_BY_ID_QUERY =
            SELECT_ALL_CARS_QUERY + " WHERE cars.id = ?";

    private static final String SELECT_CARS_BY_MODEL_QUERY =
            SELECT_ALL_CARS_QUERY + " WHERE model_id = ? ORDER BY cars.id";

    private static final String SELECT_AVAILABLE_CARS_BY_MODEL_QUERY =
            SELECT_ALL_CARS_QUERY + " WHERE model_id = ? AND car_statuses.name = 'AVAILABLE' ORDER BY cars.id";

    private static final String SELECT_CARS_WITH_RENTALS_QUERY = """
            SELECT
                cars.id AS car_id,
                cars.plate_number AS car_plate_number,
                CONCAT(car_models.brand, ' ', car_models.name, ' ', car_models.year) AS name,
                rentals.id AS rental_id,
                MIN(rentals.start_date) AS next_rental_date,
                rs.name AS status,
                GROUP_CONCAT(DISTINCT CONCAT(re.id, ':', re.name, ':', re.price) SEPARATOR ';') AS extras
            FROM cars
                JOIN car_models ON cars.model_id = car_models.id
                JOIN rentals ON cars.id = rentals.car_id
                JOIN rental_statuses rs ON rentals.status_id = rs.id
                LEFT JOIN rental_extra_assignments rea ON rentals.id = rea.rental_id
                LEFT JOIN rental_extras re ON rea.extra_id = re.id
            WHERE rentals.start_date >= CURDATE()
                AND NOT EXISTS (
                    SELECT 1
                    FROM car_issue_reports
                    WHERE car_issue_reports.car_id = cars.id
                      AND car_issue_reports.status = 'PENDING'
                )
                AND rentals.status_id IN (:statusIds)
            GROUP BY cars.id, cars.plate_number, rentals.id, car_models.brand, car_models.name, car_models.year
            """;

    private static final String SELECT_CAR_TO_PREPARE_QUERY = """
            SELECT
                cars.id AS car_id,
                cars.plate_number AS car_plate_number,
                CONCAT(car_models.brand, ' ', car_models.name, ' ', car_models.year) AS name,
                rentals.id AS rental_id,
                MIN(rentals.start_date) AS next_rental_date,
                rs.name AS status,
                GROUP_CONCAT(DISTINCT CONCAT(re.id, ':', re.name, ':', re.price) SEPARATOR ';') AS extras
            FROM cars
                JOIN car_models ON cars.model_id = car_models.id
                JOIN rentals ON cars.id = rentals.car_id
                JOIN rental_statuses rs ON rentals.status_id = rs.id
                LEFT JOIN rental_extra_assignments rea ON rentals.id = rea.rental_id
                LEFT JOIN rental_extras re ON rea.extra_id = re.id
            WHERE rentals.start_date >= CURDATE()
                AND NOT EXISTS (
                    SELECT 1
                    FROM car_issue_reports
                    WHERE car_issue_reports.car_id = cars.id
                      AND car_issue_reports.status = 'PENDING'
                )
                AND rentals.status_id = ?
                AND rentals.id = ?
            GROUP BY cars.id, cars.plate_number, rentals.id, car_models.brand, car_models.name, car_models.year
            """;

    private static final String COUNT_PREPARATION_CARS_QUERY = """
            SELECT COUNT(DISTINCT rentals.id)
            FROM cars
                JOIN car_models ON cars.model_id = car_models.id
                JOIN rentals ON cars.id = rentals.car_id
            WHERE rentals.start_date >= CURDATE()
                AND NOT EXISTS (
                    SELECT 1
                    FROM car_issue_reports
                    WHERE car_issue_reports.car_id = cars.id
                      AND car_issue_reports.status = 'PENDING'
                )
                AND rentals.status_id IN (%s)
            """;

    private static final String UPDATE_RENTAL_STATUS_QUERY =
            "UPDATE rentals SET status_id = ? WHERE id = ?";

    private static final String DEFAULT_ORDER_BY_CAR_ID = "cars.id DESC";
    private static final String ORDER_BY_CAR_ID_ASC = "cars.id ASC";
    private static final String ORDER_BY_NEXT_RENTAL_DATE = "next_rental_date";
    private static final String ALL_FILTER = "all";

    private final CarRowMapper carRowMapper;

    @Autowired
    public MySqlCarDAO(JdbcTemplate jdbcTemplate, CarRowMapper carRowMapper) {
        super(jdbcTemplate);
        this.carRowMapper = carRowMapper;
    }

    @Override
    public void addCar(Car car) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement =
                    connection.prepareStatement(INSERT_CAR_QUERY, Statement.RETURN_GENERATED_KEYS);
            setCarParameters(statement, car);
            return statement;
        }, keyHolder);
        car.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @Override
    public void updateCar(Car car) {
        jdbcTemplate.update(UPDATE_CAR_QUERY,
                car.getModel().getId(),
                car.getPlateNumber(),
                car.getColor(),
                1,
                car.getId());
    }

    @Override
    public void deleteCar(int carId) {
        jdbcTemplate.update(DELETE_CAR_QUERY, carId);
    }

    @Override
    public Car getCarById(int carId) {
        return jdbcTemplate.queryForObject(SELECT_CAR_BY_ID_QUERY, carRowMapper, carId);
    }

    @Override
    public List<Car> getAvailableCarsByModelId(int modelId) {
        return jdbcTemplate.query(SELECT_AVAILABLE_CARS_BY_MODEL_QUERY, carRowMapper, modelId);
    }

    @Override
    public PageResult<Car> getCarsPage(Integer modelId, Integer statusId, Integer carId, String carPlateNumber,
                                       int page, int size, String sortBy) {
        QueryParams queryParams = buildCarFilterQuery(modelId, statusId, carId, carPlateNumber);
        String orderByClause = buildOrderByClause(sortBy);

        return executePaginationQuery(
                SELECT_ALL_CARS_QUERY,
                COUNT_CARS_QUERY,
                carRowMapper,
                queryParams,
                page,
                size,
                orderByClause
        );
    }

    private void setCarParameters(PreparedStatement statement, Car car) throws SQLException {
        int parameterIndex = 0;
        statement.setInt(++parameterIndex, car.getModel().getId());
        statement.setString(++parameterIndex, car.getPlateNumber());
        statement.setString(++parameterIndex, car.getColor());
        statement.setInt(++parameterIndex, 1);
    }

    private String buildOrderByClause(String sortBy) {
        return switch (sortBy) {
            case "carIdAsc" -> ORDER_BY_CAR_ID_ASC;
            default -> DEFAULT_ORDER_BY_CAR_ID;
        };
    }

    private List<Object> buildStatusIdList(String statusFilter) {
        List<Object> statusIds = new ArrayList<>();

        return statusIds;
    }

    /**
     * Builds query conditions and parameters for filtering cars.
     *
     * @param modelId        car model ID filter
     * @param statusId       car status ID filter
     * @param carId          car ID filter
     * @param carPlateNumber car plate number filter
     * @return QueryParams object containing conditions and parameters
     */
    private QueryParams buildCarFilterQuery(Integer modelId, Integer statusId, Integer carId, String carPlateNumber) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (carId != null && carId != 0) {
            conditions.add("cars.id = ?");
            parameters.add(carId);
        }

        if (carPlateNumber != null && !carPlateNumber.trim().isEmpty()) {
            conditions.add("plate_number LIKE ?");
            parameters.add("%" + carPlateNumber.trim() + "%");
        }

        if (modelId != null && modelId != 0) {
            conditions.add("cars.model_id = ?");
            parameters.add(modelId);
        }

        if (statusId != null && statusId != 0) {
            conditions.add("cars.status_id = ?");
            parameters.add(statusId);
        }

        return new QueryParams(conditions, parameters);
    }
}