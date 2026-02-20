package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import com.car_rental.form.user.UserUpdateDTO;
import com.car_rental.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(SuperAdminUserController.class);

    private static final String ALL_USERS_VIEW = "superAdminUser/usersPage";
    private static final String CREATE_USER_VIEW = "superAdminUser/createUserPage";
    private static final String UPDATE_USER_VIEW = "superAdminUser/updateUserPage";
    private static final String REDIRECT_TO_ALL_USERS = "redirect:/users";
    private static final String USER_UPDATE_DTO = "userUpdateDTO";

    private final UserService userService;

    @Autowired
    public SuperAdminUserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("roles")
    public List<Role> populateRoles() {
        return userService.getAllRoles();
    }

    @RequestMapping(value = "/users", method = {RequestMethod.GET, RequestMethod.POST})
    public String getAllUsers(@RequestParam(required = false) Integer roleId,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) Integer userId,
                              @RequestParam(required = false) String lastName,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int pageSize,
                              @RequestParam(defaultValue = "userIdAsc") String sortBy,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            PageResult<User> userPage =
                    userService.getUsersPage(roleId, userId, username, lastName, page, pageSize, sortBy);

            model.addAttribute("roleId", roleId);
            model.addAttribute("usernameSearch", username);
            model.addAttribute("userIdSearch", userId);
            model.addAttribute("lastNameSearch", lastName);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute(PAGE, userPage);

            return ALL_USERS_VIEW;
        } catch (Exception e) {
            logger.error("Error retrieving users: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving users: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @GetMapping("/createUser")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return CREATE_USER_VIEW;
    }

    @PostMapping("/createUser")
    public String createUser(@Valid User user,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return CREATE_USER_VIEW;
        }

        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "User created successfully.");
            return REDIRECT_TO_ALL_USERS;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, "Error creating user: " + e.getMessage());
            user.setPassword(null);
            model.addAttribute("user", user);
            return CREATE_USER_VIEW;
        }
    }

    @GetMapping("/updateUser")
    public String showUpdateUserForm(@RequestParam int id,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute(ERROR_MSG, "User not found.");
                return REDIRECT_TO_ALL_USERS;
            }

            UserUpdateDTO userUpdateDto = new UserUpdateDTO(user);
            model.addAttribute(USER_UPDATE_DTO, userUpdateDto);
            model.addAttribute("password", new UserPasswordDTO());

            return UPDATE_USER_VIEW;
        } catch (Exception e) {
            logger.error("Error retrieving user data: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error retrieving user data: " + e.getMessage());
            return REDIRECT_TO_ALL_USERS;
        }
    }

    @PostMapping("/updateUser")
    public String updateUser(@Valid UserUpdateDTO userUpdateDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        int id = userUpdateDto.getId();
        User oldUser = userService.getUserById(id);
        User newUser = new User(userUpdateDto);
        newUser.setId(id);

        if (oldUser.equals(newUser)) {
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "No changes were made.");
            return REDIRECT_TO_ALL_USERS;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute(USER_UPDATE_DTO, userUpdateDto);
            return UPDATE_USER_VIEW;
        }

        try {
            userService.updateUser(oldUser, newUser);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "User updated successfully.");
            return REDIRECT_TO_ALL_USERS;
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, "Error updating user: " + e.getMessage());
            model.addAttribute(USER_UPDATE_DTO, userUpdateDto);
            return UPDATE_USER_VIEW;
        }
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam int id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "User deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, "Error deleting user: " + e.getMessage());
        }
        return REDIRECT_TO_ALL_USERS;
    }
}