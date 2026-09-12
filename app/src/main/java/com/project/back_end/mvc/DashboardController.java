package com.project.back_end.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.back_end.services.Service;


@Controller
public class DashboardController {

    @Autowired
    private Service service;

    @GetMapping("/adminDashboard")
    public String adminDashboard(@RequestParam String token) {
        var validationResult = service.validateToken(token, "admin");
        if (validationResult.getStatusCode().is2xxSuccessful()) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard")
    public String doctorDashboard(@RequestParam String token) {
        var validationResult = service.validateToken(token, "doctor");
        if (validationResult.getStatusCode().is2xxSuccessful()) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }
}