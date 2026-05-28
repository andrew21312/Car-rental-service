package com.car_rental.dao.daointerface;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import java.util.List;

public interface UserDAO {
    void createUser(User user);

    void updateUser(User oldUser, User newUser);

    void updateUserPassword(int id, String newPassword);

    void deleteUser(int id);

    User getUserByUsername(String username);

    User getUserById(int id);

    Role getRoleById(int id);

    Role getRoleByName(String name);

    PageResult<User> getUsersPage(Integer roleId, Integer userId, String username, String lastName, int page,
                                  int pageSize, String sortBy);

    List<Role> getAllRoles();
}
