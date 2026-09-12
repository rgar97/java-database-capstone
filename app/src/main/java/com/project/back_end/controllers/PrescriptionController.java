package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private Service service;

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@PathVariable String token,
                                                               @Valid @RequestBody Prescription prescription) {
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "doctor");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Acceso no autorizado: Se requiere rol de médico.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Long doctorId = service.getUserIdFromToken(token);
        return prescriptionService.savePrescription(prescription, doctorId);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId,
                                                               @PathVariable String token) {
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "doctor");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Acceso no autorizado: Se requiere rol de médico.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Long doctorId = service.getUserIdFromToken(token);
        return prescriptionService.getPrescription(appointmentId, doctorId);
    }
}