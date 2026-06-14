package com.car_rental.controller;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import com.car_rental.form.user.UserUpdateDTO;
import com.car_rental.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.car_rental.constants.ControllerConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SuperAdminUserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final Model model = mock(Model.class);
    private final BindingResult bindingResult = mock(BindingResult.class);
    private final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

    private final SuperAdminUserController controller =
            new SuperAdminUserController(userService);

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("john_doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        Role role = new Role();
        role.setRoleName("CLIENT");
        user.setRole(role);
        return user;
    }


    @Test
    void populateRoles_ShouldReturnAllRoles() {
        List<Role> roles = List.of(new Role(), new Role());
        when(userService.getAllRoles()).thenReturn(roles);

        List<Role> result = controller.populateRoles();

        assertEquals(roles, result);
        verify(userService).getAllRoles();
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_ShouldReturnUsersPage_WhenNoFilters() {
        PageResult<User> page = new PageResult<>(List.of(), 0, 10, 0);
        when(userService.getUsersPage(null, null, null, null, 0, 10, "userIdAsc"))
                .thenReturn(page);

        String result = controller.getAllUsers(
                null, null, null, null, 0, 10, "userIdAsc", model, redirectAttributes);

        assertEquals("superAdminUser/usersPage", result);
        verify(model).addAttribute(PAGE, page);
        verify(model).addAttribute("sortBy", "userIdAsc");
    }

    @Test
    void getAllUsers_ShouldReturnUsersPage_WithFilters() {
        PageResult<User> page = new PageResult<>(List.of(createUser()), 0, 10, 1);
        when(userService.getUsersPage(1, 1, "john", "Doe", 0, 10, "userIdAsc"))
                .thenReturn(page);

        String result = controller.getAllUsers(
                1, "john", 1, "Doe", 0, 10, "userIdAsc", model, redirectAttributes);

        assertEquals("superAdminUser/usersPage", result);
        verify(model).addAttribute(PAGE, page);
        verify(model).addAttribute("usernameSearch", "john");
        verify(model).addAttribute("lastNameSearch", "Doe");
    }

    @Test
    void getAllUsers_ShouldRedirectToMain_WhenServiceThrows() {
        when(userService.getUsersPage(any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenThrow(new RuntimeException("DB error"));

        String result = controller.getAllUsers(
                null, null, null, null, 0, 10, "userIdAsc", model, redirectAttributes);

        assertEquals(REDIRECT_TO_MAIN_PAGE, result);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR_MSG), contains("DB error"));
    }

    // --- showCreateUserForm ---

    @Test
    void showCreateUserForm_ShouldReturnCreatePage() {
        String result = controller.showCreateUserForm(model);

        assertEquals("superAdminUser/createUserPage", result);
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    // --- createUser ---

    @Test
    void createUser_ShouldCreateAndRedirect_WhenValidData() {
        User user = createUser();
        user.setPassword("secret123");
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.createUser(user, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(userService).createUser(user);
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MSG, "User created successfully.");
    }

    @Test
    void createUser_ShouldReturnForm_WhenBindingErrors() {
        User user = createUser();
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.createUser(user, bindingResult, model, redirectAttributes);

        assertEquals("superAdminUser/createUserPage", result);
        verify(userService, never()).createUser(any());
        verify(model).addAttribute("user", user);
    }

    @Test
    void createUser_ShouldReturnForm_WhenServiceThrows() {
        User user = createUser();
        user.setPassword("secret123");
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("duplicate username")).when(userService).createUser(any());

        String result = controller.createUser(user, bindingResult, model, redirectAttributes);

        assertEquals("superAdminUser/createUserPage", result);
        verify(model).addAttribute(eq(ERROR_MSG), contains("duplicate username"));
    }

    // --- showUpdateUserForm ---

    @Test
    void showUpdateUserForm_ShouldReturnUpdatePage_WhenUserFound() {
        User user = createUser();
        when(userService.getUserById(1)).thenReturn(user);

        String result = controller.showUpdateUserForm(1, model, redirectAttributes);

        assertEquals("superAdminUser/updateUserPage", result);
        verify(model).addAttribute(eq("userUpdateDTO"), any(UserUpdateDTO.class));
        verify(model).addAttribute(eq("password"), any(UserPasswordDTO.class));
    }

    @Test
    void showUpdateUserForm_ShouldRedirect_WhenUserNotFound() {
        when(userService.getUserById(99)).thenReturn(null);

        String result = controller.showUpdateUserForm(99, model, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MSG, "User not found.");
    }

    @Test
    void showUpdateUserForm_ShouldRedirect_WhenServiceThrows() {
        when(userService.getUserById(1)).thenThrow(new RuntimeException("DB error"));

        String result = controller.showUpdateUserForm(1, model, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR_MSG), contains("DB error"));
    }

    // --- updateUser ---

    @Test
    void updateUser_ShouldUpdateAndRedirect_WhenDataChanged() {
        User oldUser = createUser();
        UserUpdateDTO dto = mock(UserUpdateDTO.class);
        when(dto.getId()).thenReturn(1);
        when(userService.getUserById(1)).thenReturn(oldUser);
        when(bindingResult.hasErrors()).thenReturn(false);

        // new user differs from old
        User newUser = new User(dto);
        newUser.setId(1);
        newUser.setUsername("new_username");

        String result = controller.updateUser(dto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(userService).updateUser(eq(oldUser), any(User.class));
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MSG, "User updated successfully.");
    }

    @Test
    void updateUser_ShouldRedirectWithNoChanges_WhenUsersAreEqual() {
        User user = createUser();
        UserUpdateDTO dto = new UserUpdateDTO(user);

        when(userService.getUserById(1)).thenReturn(user);

        String result = controller.updateUser(dto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(userService, never()).updateUser(any(), any());
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MSG, "No changes were made.");
    }

    @Test
    void updateUser_ShouldReturnForm_WhenBindingErrors() {
        User user = createUser();
        user.setUsername("changed");
        UserUpdateDTO dto = mock(UserUpdateDTO.class);
        when(dto.getId()).thenReturn(1);
        when(userService.getUserById(1)).thenReturn(createUser());
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.updateUser(dto, bindingResult, model, redirectAttributes);

        assertEquals("superAdminUser/updateUserPage", result);
        verify(userService, never()).updateUser(any(), any());
    }

    // --- deleteUser ---

    @Test
    void deleteUser_ShouldDeleteAndRedirect_WhenSuccess() {
        String result = controller.deleteUser(1, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(userService).deleteUser(1);
        verify(redirectAttributes).addFlashAttribute(SUCCESS_MSG, "User deleted successfully.");
    }

    @Test
    void deleteUser_ShouldRedirectWithError_WhenServiceThrows() {
        doThrow(new RuntimeException("Cannot delete")).when(userService).deleteUser(1);

        String result = controller.deleteUser(1, redirectAttributes);

        assertEquals("redirect:/users", result);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR_MSG), contains("Cannot delete"));
    }
}