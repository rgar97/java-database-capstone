package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private TokenService tokenService;

    /**
     * Reserva una nueva cita en la base de datos.
     */
    public int bookAppointment(Appointment appointment) {
        try {
            if (appointment == null || appointment.getDoctor() == null || appointment.getPatient() == null
                    || appointment.getDoctor().getId() == null || appointment.getPatient().getId() == null
                    || appointment.getAppointmentTime() == null || !appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {
                return 0;
            }

            Optional<Doctor> doctor = doctorRepository.findById(appointment.getDoctor().getId());
            Optional<Patient> patient = patientRepository.findById(appointment.getPatient().getId());
            if (doctor.isEmpty() || patient.isEmpty()) {
                return 0;
            }

            String requestedSlot = appointment.getAppointmentTime()
                    .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            if (doctor.get().getAvailableTimes() == null
                    || !doctor.get().getAvailableTimes().contains(requestedSlot)
                    || appointmentRepository.existsByDoctorIdAndAppointmentTime(
                    doctor.get().getId(), appointment.getAppointmentTime())) {
                return 0;
            }

            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Actualiza los datos de una cita existente validando su presencia.
     */
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();

        if (appointment == null || appointment.getId() == null) {
            response.put("message", "Datos de cita inválidos.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
        if (existingAppointment.isEmpty()) {
            response.put("message", "La cita no existe.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        try {
            appointmentRepository.save(appointment);
            response.put("message", "Cita actualizada exitosamente.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Error al actualizar la cita.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancela y elimina una cita previa verificación de permisos.
     */
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) {
            response.put("message", "Cita no encontrada.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        try {
            Appointment appointment = appointmentOpt.get();
            if (!isAuthorizedForAppointment(appointment, token)) {
                response.put("message", "No está autorizado para cancelar esta cita.");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            appointmentRepository.delete(appointment);
            response.put("message", "Cita cancelada exitosamente.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Error al cancelar la cita.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recupera la lista de citas filtradas por médico, fecha y opcionalmente por nombre de paciente.
     */
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!tokenService.validateToken(token, "doctor") || date == null) {
                response.put("message", "Token o fecha inválidos.");
                return response;
            }

            Long doctorId = tokenService.getUserIdFromToken(token);
            if (doctorId == null) {
                response.put("message", "Token de autorización inválido.");
                return response;
            }

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            List<Appointment> appointments;
            if (pname != null && !pname.trim().isEmpty() && !"null".equalsIgnoreCase(pname)) {
                appointments = appointmentRepository
                        .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, pname, start, end);
            } else {
                appointments = appointmentRepository
                        .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
            }

            response.put("appointments", appointments);
        } catch (Exception e) {
            response.put("message", "Error al obtener las citas.");
        }

        return response;
    }

    private boolean isAuthorizedForAppointment(Appointment appointment, String token) {
        Long userId = tokenService.getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }

        boolean isDoctor = tokenService.validateToken(token, "doctor")
                && appointment.getDoctor() != null
                && userId.equals(appointment.getDoctor().getId());
        boolean isPatient = tokenService.validateToken(token, "patient")
                && appointment.getPatient() != null
                && userId.equals(appointment.getPatient().getId());
        return isDoctor || isPatient;
    }
}