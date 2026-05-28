package com.car_rental.controller;

import static com.car_rental.constants.ControllerConstants.*;

import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    private static final String LOGS_VIEW = "log/logsView";

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    public String viewLogs(@RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "15") int size,
                           Model model) {
        try {
            PageResult<Log> logsPage = logService.getLogsPage(q, page, size);
            model.addAttribute(PAGE, logsPage);
            model.addAttribute("searchQuery", q);
            return LOGS_VIEW;
        } catch (Exception e) {
            log.error("Error retrieving logs: {}", e.getMessage(), e);
            model.addAttribute(ERROR_MSG, "Error retrieving logs: " + e.getMessage());
            return REDIRECT_TO_MAIN_PAGE;
        }
    }
}
