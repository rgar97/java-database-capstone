package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Prescription;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * 1. Guarda una nueva receta en MongoDB.
     */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription, Long doctorId) {
        Map<String, String> response = new HashMap<>();

        try {
            if (prescription == null) {
                response.put("message", "La información de la receta es inválida.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (doctorId == null || !doctorOwnsAppointment(prescription.getAppointmentId(), doctorId)) {
                response.put("message", "El doctor no está autorizado para esta cita.");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            prescriptionRepository.save(prescription);
            response.put("message", "Receta guardada");
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            response.put("message", "Ocurrió un error al guardar la receta.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 2. Recupera la receta asociada con un ID de cita específico.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId, Long doctorId) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (appointmentId == null) {
                response.put("message", "El ID de la cita no puede ser nulo.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (doctorId == null || !doctorOwnsAppointment(appointmentId, doctorId)) {
                response.put("message", "El doctor no está autorizado para esta cita.");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            
            response.put("prescriptions", prescriptions);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al recuperar la receta.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean doctorOwnsAppointment(Long appointmentId, Long doctorId) {
        if (appointmentId == null) {
            return false;
        }

        return appointmentRepository.findById(appointmentId)
                .map(Appointment::getDoctor)
                .map(doctor -> doctorId.equals(doctor.getId()))
                .orElse(false);
    }
}