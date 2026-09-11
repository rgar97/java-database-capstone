package com.project.back_end.controller;

import com.project.back_end.DTO.Login;
import com.project.back_end.model.Patient;
import com.project.back_end.service.PatientService;
import com.project.back_end.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private Service service;

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "patient");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Acceso no autorizado o token expirado.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();

        boolean isUnique = service.validatePatient(patient);
        if (!isUnique) {
            response.put("message", "El paciente con el correo electrónico o número de teléfono ya existe");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        int result = patientService.createPatient(patient);
        if (result == 1) {
            response.put("message", "Registro exitoso");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            response.put("message", "Error interno del servidor");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(@PathVariable Long id,
                                                                      @PathVariable String token) {
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "patient");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Acceso no autorizado o token expirado.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointments(@PathVariable String condition,
                                                                          @PathVariable String name,
                                                                          @PathVariable String token) {
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "patient");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Acceso no autorizado o token expirado.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return service.filterPatient(condition, name, token);
    }
}