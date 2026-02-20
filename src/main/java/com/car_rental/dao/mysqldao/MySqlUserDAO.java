package com.car_rental.dao.mysqldao;

import com.car_rental.dao.daointerface.UserDAO;
import com.car_rental.dao.mapper.RoleRowMapper;
import com.car_rental.dao.mapper.UserRowMapper;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
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
public class MySqlUserDAO extends BasePaginationDAO implements UserDAO {

    private static final String SELECT_ALL_USERS = """
            SELECT
                users.id as user_id,
                users.username as user_username,
                users.first_name as user_first_name,
                users.last_name as user_last_name,
                users.password_hash as user_password_hash,
                users.created_at as user_created_at,
                users.phone_number as user_phone_number,
                roles.id as role_id,
                roles.name as role_name,
                roles.description as role_description
            FROM users
            JOIN roles ON roles.id = users.role_id
            """;
    private static final String INSERT_USER_QUERY =
            "INSERT INTO users (username, first_name, last_name, password_hash, role_id, created_at, phone_number) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_USER_PASSWORD_QUERY = "UPDATE users SET password_hash = ? WHERE id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String SELECT_USER_BY_USERNAME = SELECT_ALL_USERS + " WHERE username LIKE ?";
    private static final String SELECT_USER_BY_ID = SELECT_ALL_USERS + " WHERE users.id = ?";

    private static final String SELECT_ALL_ROLES =
            "SELECT id as role_id, name as role_name, description as role_description FROM roles";
    private static final String SELECT_ROLE_BY_ID = SELECT_ALL_ROLES + " WHERE id = ?";
    private static final String SELECT_ROLE_BY_NAME = SELECT_ALL_ROLES + " WHERE name = ?";

    private final RoleRowMapper roleRowMapper;
    private final UserRowMapper userRowMapper;

    @Autowired
    public MySqlUserDAO(JdbcTemplate jdbcTemplate,
                        RoleRowMapper roleRowMapper,
                        UserRowMapper userRowMapper) {
        super(jdbcTemplate);
        this.roleRowMapper = roleRowMapper;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public void createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            setUserParameters(ps, user);
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    private void setUserParameters(PreparedStatement stmt, User user) throws SQLException {
        int parameterIndex = 0;
        stmt.setString(++parameterIndex, user.getUsername());
        stmt.setString(++parameterIndex, user.getFirstName());
        stmt.setString(++parameterIndex, user.getLastName());
        stmt.setString(++parameterIndex, user.getPassword());
        stmt.setInt(++parameterIndex, user.getRole().getId());
        stmt.setTimestamp(++parameterIndex, user.getCreatedAt());
        stmt.setString(++parameterIndex, user.getPhoneNumber());
    }

    @Override
    public void updateUser(User oldUser, User newUser) {
        StringBuilder query = new StringBuilder("UPDATE users SET ");
        List<Object> parameters = new ArrayList<>();

        if (!Objects.equals(oldUser.getUsername(), newUser.getUsername())) {
            query.append("username = ?, ");
            parameters.add(newUser.getUsername());
        }
        if (!Objects.equals(oldUser.getFirstName(), newUser.getFirstName())) {
            query.append("first_name = ?, ");
            parameters.add(newUser.getFirstName());
        }
        if (!Objects.equals(oldUser.getLastName(), newUser.getLastName())) {
            query.append("last_name = ?, ");
            parameters.add(newUser.getLastName());
        }
        if (!Objects.equals(oldUser.getPhoneNumber(), newUser.getPhoneNumber())) {
            query.append("phone_number = ?, ");
            parameters.add(newUser.getPhoneNumber());
        }
        if (newUser.getRole() != null && oldUser.getRole() != null &&
            newUser.getRole().getId() != oldUser.getRole().getId()) {
            query.append("role_id = ?, ");
            parameters.add(newUser.getRole().getId());
        }

        if (parameters.isEmpty()) {
            return;
        }

        if (query.toString().endsWith(", ")) {
            query.setLength(query.length() - 2);
        }

        query.append(" WHERE id = ?");
        parameters.add(oldUser.getId());

        jdbcTemplate.update(query.toString(), parameters.toArray());
    }

    @Override
    public void updateUserPassword(int id, String newPassword) {
        jdbcTemplate.update(UPDATE_USER_PASSWORD_QUERY, newPassword, id);
    }

    @Override
    public void deleteUser(int id) {
        jdbcTemplate.update(DELETE_USER_QUERY, id);
    }

    @Override
    public User getUserByUsername(String username) {
        return jdbcTemplate.queryForObject(SELECT_USER_BY_USERNAME, userRowMapper, username);
    }

    @Override
    public User getUserById(int id) {
        return jdbcTemplate.queryForObject(SELECT_USER_BY_ID, userRowMapper, id);
    }

    @Override
    public Role getRoleById(int id) {
        return jdbcTemplate.queryForObject(SELECT_ROLE_BY_ID, roleRowMapper, id);
    }

    @Override
    public Role getRoleByName(String name) {
        return jdbcTemplate.queryForObject(SELECT_ROLE_BY_NAME, roleRowMapper, name);
    }

    @Override
    public List<Role> getAllRoles() {
        return jdbcTemplate.query(SELECT_ALL_ROLES, roleRowMapper);
    }

    @Override
    public PageResult<User> getUsersPage(Integer roleId, Integer userId, String username, String lastName, int page,
                                         int pageSize, String sortBy) {
        QueryParams queryParams = buildQueryParams(roleId, username, lastName, userId);

        String orderByClause = switch (sortBy) {
            case "usernameAsc" -> "users.username ASC";
            case "usernameDesc" -> "users.username DESC";
            case "userIdDesc" -> "users.id DESC";
            default -> "users.id ASC";
        };

        String countQuery = "SELECT COUNT(*) FROM users";

        return executePaginationQuery(
                SELECT_ALL_USERS, countQuery,
                userRowMapper, queryParams, page, pageSize,
                orderByClause);
    }

    /**
     * Допоміжний метод для побудови умов і параметрів динамічного запиту
     *
     * @param roleId   ID ролі
     * @param username username
     * @param lastName lastName
     * @return об'єкт з умовними рядками і відповідними параметрами@
     */
    private QueryParams buildQueryParams(Integer roleId, String username, String lastName, Integer id) {
        List<Object> parameters = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (username != null && !username.trim().isEmpty()) {
            conditions.add("users.username LIKE ?");
            parameters.add("%" + username.trim() + "%");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            conditions.add("users.last_name LIKE ?");
            parameters.add("%" + lastName.trim() + "%");
        }

        if (id != null && id != 0) {
            conditions.add("users.id = ?");
            parameters.add(id);
        }

        if (roleId != null && roleId != 0) {
            conditions.add("users.role_id = ?");
            parameters.add(roleId);
        }

        return new QueryParams(conditions, parameters);
    }
}