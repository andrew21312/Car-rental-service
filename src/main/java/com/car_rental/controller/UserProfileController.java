package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import com.car_rental.form.user.UserUpdateDTO;
import com.car_rental.security.UserDetailsImpl;
import com.car_rental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private static final String USER_PROFILE_PAGE = "profile/userProfile";
    private static final String REDIRECT_TO_USER_PROFILE = "redirect:/profile";
    private static final String LOGIN_PAGE = "auth/signIn";
    private static final String SIGNUP_PAGE = "auth/signUp";
    private static final String REDIRECT_TO_SIGN_IN = "redirect:/signIn";

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signIn")
    public String showLoginPage() {
        return LOGIN_PAGE;
    }

    @GetMapping("/signUp")
    public String showSignUpPage(Model model) {
        model.addAttribute("user", new User());
        return SIGNUP_PAGE;
    }

    @PostMapping("/signUp")
    public String signUp(@ModelAttribute("user") @Valid User user,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return SIGNUP_PAGE;
        }

        try {
            userService.registerClient(user);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Account created successfully. Please sign in.");
            return REDIRECT_TO_SIGN_IN;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            user.setPassword(null);
            model.addAttribute(ERROR_MSG, "Error creating user: " + e.getMessage());
            return SIGNUP_PAGE;
        }
    }

    @GetMapping("/profile")
    public String showUserProfile(Model model,
                                  @AuthenticationPrincipal UserDetailsImpl authenticatedUser,
                                  RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByUsername(authenticatedUser.getUsername());
            populateUserProfile(model, currentUser);
            return USER_PROFILE_PAGE;
        } catch (Exception e) {
            logger.error("Error retrieving user profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @PostMapping("/updateUserPassword")
    public String updatePassword(@Valid @ModelAttribute("password") UserPasswordDTO passwordDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 @AuthenticationPrincipal UserDetailsImpl authenticatedUser,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserById(authenticatedUser.getId());

        if (bindingResult.hasErrors()) {
            populateUserProfile(model, currentUser);
            model.addAttribute(ERROR_MSG, bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return USER_PROFILE_PAGE;
        }

        try {
            userService.updateUserPassword(currentUser.getId(), passwordDto);
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Password updated successfully.");
            return REDIRECT_TO_USER_PROFILE;
        } catch (Exception e) {
            logger.error("Failed to update password: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
            return REDIRECT_TO_USER_PROFILE;
        }
    }

    @PostMapping("/updateProfile")
    public String updateUserProfile(@Valid @ModelAttribute("user") UserUpdateDTO userUpdateDto,
                                    BindingResult bindingResult,
                                    Model model,
                                    @AuthenticationPrincipal UserDetailsImpl authenticatedUser,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = userService.getUserById(authenticatedUser.getId());

        if (bindingResult.hasErrors()) {
            populateUserProfile(model, currentUser);
            model.addAttribute(ERROR_MSG, bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return USER_PROFILE_PAGE;
        }

        try {
            User updatedUser = new User(userUpdateDto);
            updatedUser.setId(userUpdateDto.getId());
            userService.updateUser(currentUser, updatedUser);

            if (!updatedUser.getUsername().equals(authenticatedUser.getUsername())) {
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                redirectAttributes.addFlashAttribute(SUCCESS_MSG,
                                                     "Profile updated. Please sign in with your new credentials.");
                return REDIRECT_TO_SIGN_IN;
            }

            redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Profile updated successfully.");
            return REDIRECT_TO_USER_PROFILE;
        } catch (Exception e) {
            logger.error("Failed to update user profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
            return REDIRECT_TO_USER_PROFILE;
        }
    }

    private void populateUserProfile(Model model, User currentUser) {
        model.addAttribute("user", currentUser);
        model.addAttribute("password", new UserPasswordDTO());
    }
}