package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private Service service;

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token,
                                                                @Valid @RequestBody Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "patient");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Acceso no autorizado: se requiere rol de paciente.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        Long patientId = service.getUserIdFromToken(token);
        if (patientId == null || appointment.getPatient() == null
                || !patientId.equals(appointment.getPatient().getId())) {
            response.put("message", "El paciente de la cita no coincide con la sesión.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        appointment.setPatient(patientRepository.findById(patientId).orElse(null));
        if (appointment.getPatient() == null || appointmentService.bookAppointment(appointment) != 1) {
            response.put("message", "No se pudo reservar la cita. Comprueba la fecha y disponibilidad.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        response.put("message", "Cita reservada correctamente.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
