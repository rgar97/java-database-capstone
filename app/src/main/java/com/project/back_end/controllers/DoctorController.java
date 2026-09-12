package com.project.back_end.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private Service service;

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user,
                                                                    @PathVariable Long doctorId,
                                                                    @PathVariable String date,
                                                                    @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, user);

        if (authResult.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Acceso no autorizado o token expirado.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        LocalDate parseDate = LocalDate.parse(date);
        List<String> availability = doctorService.getDoctorAvailability(doctorId, parseDate);
        response.put("availability", availability);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        response.put("doctors", doctors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@Valid @RequestBody Doctor doctor,
                                                          @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "admin");

        if (authResult.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Acceso denegado: Se requiere rol de administrador.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            response.put("message", "Doctor agregado a la base de datos");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else if (result == -1) {
            response.put("message", "El doctor ya existe");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } else {
            response.put("message", "Ocurrió un error interno");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/appointments/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable String date,
                                                               @PathVariable String patientName,
                                                               @PathVariable String token) {
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "doctor");
        if (authResult.getStatusCode() != HttpStatus.OK) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Acceso no autorizado: se requiere rol de médico.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            LocalDate appointmentDate = LocalDate.parse(date);
            return new ResponseEntity<>(appointmentService.getAppointment(patientName, appointmentDate, token), HttpStatus.OK);
        } catch (DateTimeParseException exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "La fecha de las citas no es válida.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@Valid @RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@Valid @RequestBody Doctor doctor,
                                                            @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "admin");

        if (authResult.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Acceso denegado: Se requiere rol de administrador.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            response.put("message", "Doctor actualizado");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Doctor no encontrado");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Ocurrió un error interno");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable long id,
                                                            @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String, String>> authResult = service.validateToken(token, "admin");

        if (authResult.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Acceso denegado: Se requiere rol de administrador.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            response.put("message", "Doctor eliminado exitosamente");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Doctor no encontrado con el id");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Ocurrió un error interno");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(@PathVariable String name,
                                                             @PathVariable String time,
                                                             @PathVariable String speciality) {
        Map<String, Object> filteredDoctors = service.filterDoctor(name, speciality, time);
        return new ResponseEntity<>(filteredDoctors, HttpStatus.OK);
    }
}