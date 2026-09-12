package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService,
                   PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Valida la validez de un token para un usuario específico.
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        boolean isValid = tokenService.validateToken(token, user);

        if (!isValid) {
            response.put("message", "Token inválido o expirado.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Long getUserIdFromToken(String token) {
        return tokenService.getUserIdFromToken(token);
    }

    /**
     * Valida las credenciales de un administrador y devuelve un token.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());

        if (admin != null && matchesPassword(receivedAdmin.getPassword(), admin.getPassword())) {
            migratePassword(admin.getPassword(), receivedAdmin.getPassword(), admin);
            String token = tokenService.generateToken(admin.getId(), "admin");
            response.put("token", token);
            response.put("role", "admin");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Credenciales de administrador inválidas.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Filtra doctores coordinando las búsquedas según los parámetros recibidos.
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = name != null && !name.trim().isEmpty() && !"null".equalsIgnoreCase(name);
        boolean hasSpec = specialty != null && !specialty.trim().isEmpty() && !"null".equalsIgnoreCase(specialty);
        boolean hasTime = time != null && !time.trim().isEmpty() && !"null".equalsIgnoreCase(time);

        if (hasName && hasSpec && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (hasName && hasSpec) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (hasSpec && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpec) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctorService.getDoctors());
        return result;
    }

    /**
     * Valida si un horario de cita está libre para un doctor.
     */
    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            return -1;
        }

        Long doctorId = appointment.getDoctor().getId();
        if (doctorId == null) {
            return -1;
        }
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return -1;
        }

        LocalDate appDate = appointment.getAppointmentTime().toLocalDate();
        LocalTime appTime = appointment.getAppointmentTime().toLocalTime();
        String requestedSlot = appTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, appDate);

        return availableSlots.contains(requestedSlot) ? 1 : 0;
    }

    /**
     * Verifica si un paciente NO existe (devuelve true si está disponible para registrar).
     */
    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    /**
     * Valida las credenciales de inicio de sesión de un paciente.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        Patient patient = patientRepository.findByEmail(login.getIdentifier());

        if (patient != null && matchesPassword(login.getPassword(), patient.getPassword())) {
            migratePassword(patient.getPassword(), login.getPassword(), patient);
            String token = tokenService.generateToken(patient.getId(), "patient");
            response.put("token", token);
            response.put("role", "patient");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Credenciales de paciente inválidas.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Filtra las citas de un paciente según la condición (past/future) y el nombre del doctor.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Long patientId = tokenService.getUserIdFromToken(token);
        if (patientId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Token de paciente inválido.");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        boolean hasCondition = condition != null && !condition.trim().isEmpty() && !"null".equalsIgnoreCase(condition);
        boolean hasDoctorName = name != null && !name.trim().isEmpty() && !"null".equalsIgnoreCase(name);

        if (hasCondition && hasDoctorName) {
            return patientService.filterByDoctorAndCondition(condition, name, patientId);
        } else if (hasCondition) {
            return patientService.filterByCondition(condition, patientId);
        } else if (hasDoctorName) {
            return patientService.filterByDoctor(name, patientId);
        }

        return patientService.getPatientAppointment(patientId, token);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        return storedPassword.startsWith("$2")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }

    private void migratePassword(String storedPassword, String rawPassword, Object user) {
        if (storedPassword == null || !storedPassword.startsWith("$2")) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            if (user instanceof Admin admin) {
                admin.setPassword(encodedPassword);
                adminRepository.save(admin);
            } else if (user instanceof Patient patient) {
                patient.setPassword(encodedPassword);
                patientRepository.save(patient);
            }
        }
    }
}