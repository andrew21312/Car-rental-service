package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.UserDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private final UserDAO userDAO = mock(UserDAO.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final LogService logService = mock(LogService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private final UserServiceImpl userService =
            new UserServiceImpl(userDAO, passwordEncoder, logService, currentUserService);

    @Test
    void createUser_ShouldEncodePasswordSaveUserAndWriteLog() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("1234");

        when(passwordEncoder.encode("1234")).thenReturn("encodedPassword");
        when(currentUserService.getCurrentUserId()).thenReturn(1);

        userService.createUser(user);

        assertEquals("encodedPassword", user.getPassword());
        assertNotNull(user.getCreatedAt());

        verify(userDAO).createUser(user);
        verify(logService).logEvent(
                1,
                Log.AuditEventType.CREATE_USER,
                "Created user: testUser"
        );
    }

    @Test
    void createUser_ShouldThrowDataAccessException_WhenDaoThrowsException() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("1234");

        when(passwordEncoder.encode("1234")).thenReturn("encodedPassword");
        doThrow(new RuntimeException("DB error")).when(userDAO).createUser(user);

        assertThrows(DataAccessException.class, () -> userService.createUser(user));

        verify(userDAO).createUser(user);
    }

    @Test
    void registerClient_ShouldSetClientRoleAndCreateUser() {
        User user = new User();
        user.setUsername("client");
        user.setPassword("1234");

        Role clientRole = new Role();
        clientRole.setRoleName("CLIENT");

        when(userDAO.getRoleByName("CLIENT")).thenReturn(clientRole);
        when(passwordEncoder.encode("1234")).thenReturn("encodedPassword");
        when(currentUserService.getCurrentUserId()).thenReturn(1);

        userService.registerClient(user);

        assertEquals(clientRole, user.getRole());
        assertEquals("encodedPassword", user.getPassword());

        verify(userDAO).getRoleByName("CLIENT");
        verify(userDAO).createUser(user);
    }

    @Test
    void updateUser_ShouldUpdateUserAndWriteLog() {
        User oldUser = new User();
        oldUser.setUsername("oldUser");

        User newUser = new User();
        newUser.setUsername("newUser");

        when(currentUserService.getCurrentUserId()).thenReturn(1);

        userService.updateUser(oldUser, newUser);

        verify(userDAO).updateUser(oldUser, newUser);
        verify(logService).logEvent(
                1,
                Log.AuditEventType.UPDATE_USER,
                "Updated user: oldUser"
        );
    }

    @Test
    void updateUser_ShouldThrowDataAccessException_WhenDaoThrowsException() {
        User oldUser = new User();
        oldUser.setUsername("oldUser");

        User newUser = new User();
        newUser.setUsername("newUser");

        doThrow(new RuntimeException("DB error"))
                .when(userDAO)
                .updateUser(oldUser, newUser);

        assertThrows(DataAccessException.class, () -> userService.updateUser(oldUser, newUser));
    }

    @Test
    void updateUserPassword_ShouldUpdatePassword_WhenOldPasswordIsCorrect() {
        int userId = 5;

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setPassword("oldEncodedPassword");

        UserPasswordDTO dto = new UserPasswordDTO();
        dto.setOldPassword("oldPassword");
        dto.setNewPassword("newPassword");

        when(userDAO.getUserById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(currentUserService.getCurrentUserId()).thenReturn(1);

        userService.updateUserPassword(userId, dto);

        verify(userDAO).updateUserPassword(userId, "newEncodedPassword");
        verify(logService).logEvent(
                1,
                Log.AuditEventType.UPDATE_USER,
                "Updated password for user: testUser"
        );
    }

    @Test
    void updateUserPassword_ShouldThrowDataAccessException_WhenOldPasswordIsIncorrect() {
        int userId = 5;

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setPassword("oldEncodedPassword");

        UserPasswordDTO dto = new UserPasswordDTO();
        dto.setOldPassword("wrongPassword");
        dto.setNewPassword("newPassword");

        when(userDAO.getUserById(userId)).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "oldEncodedPassword")).thenReturn(false);

        assertThrows(DataAccessException.class, () -> userService.updateUserPassword(userId, dto));

        verify(userDAO, never()).updateUserPassword(anyInt(), anyString());
    }

    @Test
    void deleteUser_ShouldDeleteUserAndWriteLog() {
        int userId = 10;

        when(currentUserService.getCurrentUserId()).thenReturn(1);

        userService.deleteUser(userId);

        verify(userDAO).deleteUser(userId);
        verify(logService).logEvent(
                1,
                Log.AuditEventType.DELETE_USER,
                "Deleted user with ID: 10"
        );
    }

    @Test
    void getUserByUsername_ShouldReturnUserFromDao() {
        User user = new User();
        user.setUsername("testUser");

        when(userDAO.getUserByUsername("testUser")).thenReturn(user);

        User result = userService.getUserByUsername("testUser");

        assertEquals(user, result);
        verify(userDAO).getUserByUsername("testUser");
    }

    @Test
    void getUsersPage_ShouldReturnPageResultFromDao() {
        PageResult<User> expectedPage = new PageResult<>(List.of(), 0, 15, 0);

        when(userDAO.getUsersPage(null, null, "test", null, 0, 15, "username"))
                .thenReturn(expectedPage);

        PageResult<User> result =
                userService.getUsersPage(null, null, "test", null, 0, 15, "username");

        assertEquals(expectedPage, result);
        verify(userDAO).getUsersPage(null, null, "test", null, 0, 15, "username");
    }
    //
}