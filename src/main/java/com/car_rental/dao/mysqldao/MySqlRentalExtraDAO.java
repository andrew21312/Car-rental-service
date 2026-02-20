/**
 * MySQL implementation of the RentalExtraDAO interface for managing rental extras.
 * This class handles CRUD operations for rental extras in the database.
 */

package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.RentalExtraDAO;
import com.car_rental.dao.mapper.RentalExtraRowMapper;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.RentalExtra;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MySqlRentalExtraDAO extends BasePaginationDAO implements RentalExtraDAO {

    private static final String SELECT_ALL_RENTAL_EXTRAS = """
            SELECT
                id as extra_id,
                name as extra_name,
                price as extra_price
            FROM rental_extras
            """;


    private static final String ADD_EXTRA_QUERY = "INSERT INTO rental_extras (name, price) VALUES (?, ?)";

    private static final String DELETE_EXTRA_QUERY = "DELETE FROM rental_extras WHERE id = ?";

    private final RentalExtraRowMapper rentalExtraRowMapper;

    @Autowired
    public MySqlRentalExtraDAO(JdbcTemplate jdbcTemplate,
                               RentalExtraRowMapper rentalExtraRowMapper) {
        super(jdbcTemplate);
        this.rentalExtraRowMapper = rentalExtraRowMapper;
    }

    @Override
    public List<RentalExtra> getAllExtras() {
        return jdbcTemplate.query(SELECT_ALL_RENTAL_EXTRAS, rentalExtraRowMapper);
    }

    @Override
    public void addExtra(RentalExtra extra) {
        jdbcTemplate.update(ADD_EXTRA_QUERY, extra.getName(), extra.getPrice());
    }

    /**
     * Updates an existing rental extra in the database
     *
     * @param oldExtra Original RentalExtra object
     * @param newExtra Updated RentalExtra object
     */
    @Override
    public void updateExtra(RentalExtra oldExtra, RentalExtra newExtra) {
        StringBuilder query = new StringBuilder("UPDATE rental_extras SET ");
        List<Object> parameters = new ArrayList<>();

        // Update name if provided
        if (newExtra.getName() != null && !newExtra.getName().trim().isEmpty()) {
            query.append("name = ?, ");
            parameters.add(newExtra.getName());
        }

        // Update price if changed
        if (newExtra.getPrice() != oldExtra.getPrice()) {
            query.append("price = ?, ");
            parameters.add(newExtra.getPrice());
        }

        // Remove trailing comma if present
        if (query.toString().endsWith(", ")) {
            query.setLength(query.length() - 2);
        }

        if (parameters.isEmpty()) return;

        query.append(" WHERE id = ?");
        parameters.add(oldExtra.getId());

        jdbcTemplate.update(query.toString(), parameters.toArray());
    }

    @Override
    public void deleteExtra(int id) {
        jdbcTemplate.update(DELETE_EXTRA_QUERY, id);
    }

    /**
     * Retrieves a paginated list of rental extras with optional search
     *
     * @param query Search term for filtering
     * @param page  Page number
     * @param size  Page size
     * @return PageResult containing the paginated list of rental extras
     */
    @Override
    public PageResult<RentalExtra> getExtrasPage(String query, int page, int size) {
        QueryParams queryParams = buildQueryParams(query);
        return executePaginationQuery(SELECT_ALL_RENTAL_EXTRAS,
                                      "SELECT COUNT(*) FROM rental_extras",
                                      rentalExtraRowMapper,
                                      queryParams,
                                      page,
                                      size,
                                      "rental_extras.id");
    }

    /**
     * Builds query parameters for filtering rental extras
     *
     * @param query Search term
     * @return QueryParams object containing conditions and parameters
     */
    private QueryParams buildQueryParams(String query) {
        List<Object> parameters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            try {
                Integer.parseInt(query);
                conditions.add("rental_extras.id = ?");
                parameters.add(query.trim());
            } catch (NumberFormatException e) {
                String keyword = "%" + query.trim() + "%";
                conditions.add("rental_extras.name LIKE ?");
                parameters.add(keyword);
            }
        }
        return new QueryParams(conditions, parameters);
    }
}
