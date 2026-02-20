package com.car_rental.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    private static final String PAGE_NOT_FOUND_VIEW = "error/404";
    private static final String DEFAULT_ERROR_VIEW = "error/default";

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 0;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            if (statusCode == 404) {
                return PAGE_NOT_FOUND_VIEW;
            }
        }
        String errorMessage = switch (statusCode) {
            case 500 -> "Internal Server Error";
            case 403 -> "Access Denied";
            default -> "Something went wrong";
        };
        model.addAttribute("errorCode", statusCode);
        model.addAttribute("errorMSG", errorMessage);
        return DEFAULT_ERROR_VIEW; // Назва вашого шаблону для інших помилок
    }
}
