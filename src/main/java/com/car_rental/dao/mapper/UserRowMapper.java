package com.car_rental.dao.mapper;

import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserRowMapper implements RowMapper<User> {
    private final RoleRowMapper roleRowMapper;

    @Autowired
    public UserRowMapper(RoleRowMapper roleRowMapper) {
        this.roleRowMapper = roleRowMapper;
    }

    @Override
    public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        Role role = roleRowMapper.mapRow(rs, rowNum);
        user.setRole(role);

        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("user_username"));
        user.setFirstName(rs.getString("user_first_name"));
        user.setLastName(rs.getString("user_last_name"));
        user.setPassword(rs.getString("user_password_hash"));
        user.setCreatedAt(rs.getTimestamp("user_created_at"));
        user.setPhoneNumber(rs.getString("user_phone_number"));
        return user;
    }
}
