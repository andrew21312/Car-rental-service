package com.car_rental.service;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import java.util.List;

public interface UserService {
    void createUser(User user);

    void updateUser(User oldUser, User newUser);

    void updateUserPassword(int id, UserPasswordDTO userPasswordDTO);

    void deleteUser(int id);

    User getUserByUsername(String username);

    User getUserById(int id);

    Role getRoleById(int id);

    Role getRoleByName(String name);

    PageResult<User> getUsersPage(Integer roleId, Integer userId, String username, String lastName, int page,
                                  int pageSize, String sortBy);

    List<Role> getAllRoles();

    void registerClient(User user);
}
