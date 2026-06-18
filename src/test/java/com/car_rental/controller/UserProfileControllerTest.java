package com.car_rental.controller;

import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import com.car_rental.form.user.UserUpdateDTO;
import com.car_rental.security.UserDetailsImpl;
import com.car_rental.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.car_rental.constants.ControllerConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class UserProfileControllerTest {

    private final UserService userService = mock(UserService.class);
    private final Model model = mock(Model.class);
    private final BindingResult bindingResult = mock(BindingResult.class);
    private final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
    private final UserDetailsImpl authenticatedUser = mock(UserDetailsImpl.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpSession session = mock(HttpSession.class);

    private final UserProfileController controller = new UserProfileController(userService);

    @Test
    void showLoginPage_ShouldReturnLoginPage() {
        String result = controller.showLoginPage();

        assertEquals("auth/signIn", result);
    }

    @Test
    void showSignUpPage_ShouldReturnSignUpPageAndAddUserToModel() {
        String result = controller.showSignUpPage(model);

        assertEquals("auth/signUp", result);
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    @Test
    void signUp_ShouldRegisterClientAndRedirect_WhenDataIsValid() {
        User user = new User();
        user.setUsername("client");
        user.setPassword("1234");

        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.signUp(user, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/signIn", result);

        verify(userService).registerClient(user);
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Account created successfully. Please sign in."
        );
    }

    @Test
    void signUp_ShouldReturnSignUpPage_WhenValidationHasErrors() {
        User user = new User();

        when(bindingResult.hasErrors()).thenReturn(true);

        String result = controller.signUp(user, bindingResult, model, redirectAttributes);

        assertEquals("auth/signUp", result);
        verify(userService, never()).registerClient(any(User.class));
    }

    @Test
    void signUp_ShouldReturnSignUpPageAndClearPassword_WhenServiceThrowsException() {
        User user = new User();
        user.setUsername("client");
        user.setPassword("1234");

        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("DB error")).when(userService).registerClient(user);

        String result = controller.signUp(user, bindingResult, model, redirectAttributes);

        assertEquals("auth/signUp", result);
        assertNull(user.getPassword());

        verify(model).addAttribute(ERROR_MSG, "Error creating user: DB error");
    }

    @Test
    void showUserProfile_ShouldReturnProfilePage_WhenUserExists() {
        User user = new User();
        user.setUsername("client");

        when(authenticatedUser.getUsername()).thenReturn("client");
        when(userService.getUserByUsername("client")).thenReturn(user);

        String result = controller.showUserProfile(model, authenticatedUser, redirectAttributes);

        assertEquals("profile/userProfile", result);

        verify(model).addAttribute("user", user);
        verify(model).addAttribute(eq("password"), any(UserPasswordDTO.class));
    }

    @Test
    void showUserProfile_ShouldRedirectToMainPage_WhenServiceThrowsException() {
        when(authenticatedUser.getUsername()).thenReturn("client");
        when(userService.getUserByUsername("client"))
                .thenThrow(new RuntimeException("User not found"));

        String result = controller.showUserProfile(model, authenticatedUser, redirectAttributes);

        assertEquals(REDIRECT_TO_MAIN_PAGE, result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MSG, "User not found");
    }

    @Test
    void updatePassword_ShouldUpdatePasswordAndRedirect_WhenDataIsValid() {
        User user = new User();
        user.setId(1);
        user.setUsername("client");

        UserPasswordDTO passwordDTO = new UserPasswordDTO();

        when(authenticatedUser.getId()).thenReturn(1);
        when(userService.getUserById(1)).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.updatePassword(
                passwordDTO,
                bindingResult,
                model,
                authenticatedUser,
                redirectAttributes
        );

        assertEquals("redirect:/profile", result);

        verify(userService).updateUserPassword(1, passwordDTO);
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Password updated successfully."
        );
    }

    @Test
    void updatePassword_ShouldRedirectToProfile_WhenServiceThrowsException() {
        User user = new User();
        user.setId(1);

        UserPasswordDTO passwordDTO = new UserPasswordDTO();

        when(authenticatedUser.getId()).thenReturn(1);
        when(userService.getUserById(1)).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        doThrow(new RuntimeException("Wrong password"))
                .when(userService)
                .updateUserPassword(1, passwordDTO);

        String result = controller.updatePassword(
                passwordDTO,
                bindingResult,
                model,
                authenticatedUser,
                redirectAttributes
        );

        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MSG, "Wrong password");
    }

    @Test
    void updateUserProfile_ShouldUpdateAndRedirectToProfile_WhenUsernameNotChanged() {
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername("client");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setUsername("client");

        when(authenticatedUser.getId()).thenReturn(1);
        when(authenticatedUser.getUsername()).thenReturn("client");
        when(userService.getUserById(1)).thenReturn(currentUser);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.updateUserProfile(
                dto,
                bindingResult,
                model,
                authenticatedUser,
                request,
                redirectAttributes
        );

        assertEquals("redirect:/profile", result);

        verify(userService).updateUser(eq(currentUser), any(User.class));
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Profile updated successfully."
        );
    }

    @Test
    void updateUserProfile_ShouldRedirectToSignIn_WhenUsernameChanged() {
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername("oldUsername");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setUsername("newUsername");

        when(authenticatedUser.getId()).thenReturn(1);
        when(authenticatedUser.getUsername()).thenReturn("oldUsername");
        when(userService.getUserById(1)).thenReturn(currentUser);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(request.getSession()).thenReturn(session);

        String result = controller.updateUserProfile(
                dto,
                bindingResult,
                model,
                authenticatedUser,
                request,
                redirectAttributes
        );

        assertEquals("redirect:/signIn", result);

        verify(userService).updateUser(eq(currentUser), any(User.class));
        verify(session).invalidate();
        verify(redirectAttributes).addFlashAttribute(
                SUCCESS_MSG,
                "Profile updated. Please sign in with your new credentials."
        );
    }

    @Test
    void updateUserProfile_ShouldRedirectToProfile_WhenServiceThrowsException() {
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername("client");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(1);
        dto.setUsername("client");

        when(authenticatedUser.getId()).thenReturn(1);
        when(userService.getUserById(1)).thenReturn(currentUser);
        when(bindingResult.hasErrors()).thenReturn(false);

        doThrow(new RuntimeException("Update error"))
                .when(userService)
                .updateUser(eq(currentUser), any(User.class));

        String result = controller.updateUserProfile(
                dto,
                bindingResult,
                model,
                authenticatedUser,
                request,
                redirectAttributes
        );

        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute(ERROR_MSG, "Update error");
    }
}