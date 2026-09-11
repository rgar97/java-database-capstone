package com.project.back_end.service;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.re.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    /**
     * 1. Guarda un nuevo paciente en la base de datos.
     */
    public int createPatient(Patient patient) {
        try {
            if (patient == null) {
                return 0;
            }
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 2. Recupera las citas de un paciente verificando su autorización mediante el token JWT.
     */
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long tokenUserId = tokenService.getUserIdFromToken(token);
            if (tokenUserId == null || !tokenUserId.equals(id)) {
                response.put("message", "Acceso no autorizado.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al recuperar las citas del paciente.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 3. Filtra citas por condición ('past' -> status 1, 'future' -> status 0).
     */
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);

            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al filtrar citas por condición.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 4. Filtra las citas de un paciente por el nombre del médico.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientId(name, patientId);

            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al filtrar citas por doctor.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 5. Filtra las citas por nombre del médico y condición ('past' / 'future').
     */
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();

        try {
            int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);

            List<AppointmentDTO> dtos = appointments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al filtrar citas por doctor y condición.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 6. Recupera los detalles del paciente autenticado a partir del token JWT.
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long patientId = tokenService.getUserIdFromToken(token);
            if (patientId == null) {
                response.put("message", "Token inválido o expirado.");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient == null) {
                response.put("message", "Paciente no encontrado.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            response.put("patient", patient);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al recuperar detalles del paciente.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Método auxiliar para transformar una entidad Appointment a AppointmentDTO.
     */
    private AppointmentDTO convertToDTO(Appointment app) {
        return new AppointmentDTO(
                app.getId(),
                app.getDoctor() != null ? app.getDoctor().getId() : null,
                app.getDoctor() != null ? app.getDoctor().getName() : null,
                app.getPatient() != null ? app.getPatient().getId() : null,
                app.getPatient() != null ? app.getPatient().getName() : null,
                app.getPatient() != null ? app.getPatient().getEmail() : null,
                app.getPatient() != null ? app.getPatient().getPhone() : null,
                app.getPatient() != null ? app.getPatient().getAddress() : null,
                app.getAppointmentTime(),
                app.getStatus()
        );
    }
}