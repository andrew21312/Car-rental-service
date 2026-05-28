package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.CarModelDAO;
import com.car_rental.dao.mapper.CarModelRowMapper;
import com.car_rental.entity.CarModel;
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
public class MySqlCarModelDAO extends BasePaginationDAO implements CarModelDAO {

    // SQL Queries
    private static final String SELECT_ALL_CAR_MODELS = """
            SELECT
                id as car_model_id,
                price as car_model_price,
                brand as car_model_brand,
                name as car_model_name,
                year as car_model_year,
                engine_type as car_model_engine_type,
                seats as car_model_seats,
                transmission as car_model_transmission
            FROM car_models
            """;

    private static final String INSERT_CAR_MODEL = """
            INSERT INTO car_models (price, brand, name, year, engine_type, seats, transmission) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_CAR_MODEL = """
            UPDATE car_models
            SET price = ?, brand = ?, name = ?, year = ?, engine_type = ?, seats = ?, transmission = ? 
            WHERE id = ?
            """;

    private static final String DELETE_CAR_MODEL = "DELETE FROM car_models WHERE id = ?";

    private static final String SELECT_CAR_MODEL_BY_ID = SELECT_ALL_CAR_MODELS + " WHERE id = ?";

    private static final String COUNT_CAR_MODELS = "SELECT COUNT(*) FROM car_models";

    private static final String SELECT_AVAILABLE_CAR_MODELS = """
            SELECT DISTINCT cm.* FROM (%s) AS cm
            JOIN cars c ON cm.car_model_id = c.model_id
            JOIN car_statuses cs ON c.status_id = cs.id
            WHERE cs.name = 'AVAILABLE'
            """.formatted(SELECT_ALL_CAR_MODELS);

    private static final String COUNT_AVAILABLE_CAR_MODELS = """
            SELECT COUNT(DISTINCT cm.car_model_id) FROM (%s) AS cm
            JOIN cars c ON cm.car_model_id = c.model_id
            JOIN car_statuses cs ON c.status_id = cs.id
            WHERE cs.name = 'AVAILABLE'
            """.formatted(SELECT_ALL_CAR_MODELS);

    private static final String SELECT_DISTINCT_SEATS = "SELECT DISTINCT seats FROM car_models ORDER BY seats";
    // Dependencies
    private final CarModelRowMapper carModelRowMapper;
    
    @Autowired
    public MySqlCarModelDAO(JdbcTemplate jdbcTemplate, CarModelRowMapper carModelRowMapper) {
        super(jdbcTemplate);
        this.carModelRowMapper = carModelRowMapper;
    }

    public List<Integer> getDistinctSeatCounts() {
        return jdbcTemplate.queryForList(SELECT_DISTINCT_SEATS, Integer.class);
    }

    @Override
    public void addCarModel(CarModel carModel) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(INSERT_CAR_MODEL, Statement.RETURN_GENERATED_KEYS);
            setCarModelParameters(preparedStatement, carModel);
            return preparedStatement;
        }, keyHolder);

        carModel.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    private void setCarModelParameters(PreparedStatement statement, CarModel carModel) throws SQLException {
        int parameterIndex = 0;
        statement.setDouble(++parameterIndex, carModel.getPrice());
        statement.setString(++parameterIndex, carModel.getBrand());
        statement.setString(++parameterIndex, carModel.getModel());
        statement.setInt(++parameterIndex, carModel.getYear());
        statement.setString(++parameterIndex, carModel.getEngineType().toString());
        statement.setInt(++parameterIndex, carModel.getSeats());
        statement.setString(++parameterIndex, carModel.getTransmission().toString());
    }

    @Override
    public void updateCarModel(CarModel carModel) {
        jdbcTemplate.update(UPDATE_CAR_MODEL,
                            carModel.getPrice(),
                            carModel.getBrand(),
                            carModel.getModel(),
                            carModel.getYear(),
                            carModel.getEngineType().toString(),
                            carModel.getSeats(),
                            carModel.getTransmission().toString(),
                            carModel.getId());
    }

    @Override
    public void deleteCarModel(int carModelId) {
        jdbcTemplate.update(DELETE_CAR_MODEL, carModelId);
    }

    @Override
    public CarModel getCarModelById(int carModelId) {
        return jdbcTemplate.queryForObject(SELECT_CAR_MODEL_BY_ID, carModelRowMapper, carModelId);
    }

    @Override
    public List<CarModel> getAllCarModels() {
        return jdbcTemplate.query(SELECT_ALL_CAR_MODELS, carModelRowMapper);
    }

    @Override
    public PageResult<CarModel> getCarModelsPage(String engineType, Integer modelId, String modelQuery, Integer seats,
                                                 int page, int size, String sortBy) {
        QueryParams queryParams = buildQueryParams(engineType, modelId, modelQuery, seats, "");
        String orderByClause = buildOrderByClause(sortBy);

        return executePaginationQuery(
                SELECT_ALL_CAR_MODELS,
                COUNT_CAR_MODELS,
                carModelRowMapper,
                queryParams,
                page,
                size,
                orderByClause
                                     );
    }

    @Override
    public PageResult<CarModel> getAvailableCarModelsPage(String engineType, String modelQuery, Integer seats, int page,
                                                          int size, String sortBy) {
        QueryParams queryParams = buildQueryParams(engineType, null, modelQuery, seats, "cm.car_model_");
        String orderByClause = buildOrderByClause(sortBy);

        return executePaginationQuery(
                SELECT_AVAILABLE_CAR_MODELS,
                COUNT_AVAILABLE_CAR_MODELS,
                carModelRowMapper,
                queryParams,
                page,
                size,
                orderByClause
                                     );
    }

    private String buildOrderByClause(String sortBy) {
        return switch (sortBy) {
            case "priceAsc" -> "car_model_price ASC";
            case "priceDesc" -> "car_model_price DESC";
            case "yearAsc" -> "car_model_year ASC";
            case "yearDesc" -> "car_model_year DESC";
            case "modelIdAsc" -> "car_model_id ASC";
            default -> "car_model_id DESC";
        };
    }

    private QueryParams buildQueryParams(String engineType, Integer modelId, String modelQuery, Integer seats,
                                         String columnPrefix) {
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (modelId != null && modelId != 0) {
            conditions.add(columnPrefix + "id = ?");
            parameters.add(modelId);
        }

        if (modelQuery != null && !modelQuery.trim().isEmpty()) {
            conditions.add("CONCAT(" + columnPrefix + "brand, ' ', " + columnPrefix + "name) LIKE ?");
            parameters.add("%" + modelQuery.trim() + "%");
        }

        if (engineType != null && !engineType.trim().isEmpty()) {
            conditions.add(columnPrefix + "engine_type = ?");
            parameters.add(engineType.trim());
        }

        if (seats != null && seats != 0) {
            conditions.add(columnPrefix + "seats = ?");
            parameters.add(seats);
        }
        return new QueryParams(conditions, parameters);
    }
}