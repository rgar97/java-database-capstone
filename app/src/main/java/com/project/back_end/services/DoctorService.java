package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Doctor doctor = doctorOpt.get();
        List<String> allSlots = doctor.getAvailableTimes() != null ? doctor.getAvailableTimes() : Collections.emptyList();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Set<String> bookedSlots = bookedAppointments.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().format(formatter))
                .collect(Collectors.toSet());

        return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());

        if (doctor != null && doctor.getPassword().equals(login.getPassword())) {
            String token = tokenService.generateToken(doctor.getId(), "doctor");
            response.put("token", token);
            response.put("role", "doctor");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Credenciales de doctor inválidas.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        result.put("doctors", doctors);
        return result;
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        result.put("doctors", filtered);
        return result;
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        result.put("doctors", filtered);
        return result;
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty);
        result.put("doctors", doctors);
        return result;
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        result.put("doctors", filtered);
        return result;
    }

    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        result.put("doctors", doctors);
        return result;
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> allDoctors = doctorRepository.findAll();
        List<Doctor> filtered = filterDoctorByTime(allDoctors, amOrPm);
        result.put("doctors", filtered);
        return result;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isEmpty() || "null".equalsIgnoreCase(amOrPm)) {
            return doctors;
        }

        return doctors.stream().filter(doc -> {
            if (doc.getAvailableTimes() == null) return false;
            return doc.getAvailableTimes().stream().anyMatch(slot -> {
                try {
                    int hour = Integer.parseInt(slot.split(":")[0]);
                    if ("AM".equalsIgnoreCase(amOrPm)) {
                        return hour < 12;
                    } else if ("PM".equalsIgnoreCase(amOrPm)) {
                        return hour >= 12;
                    }
                } catch (Exception e) {
                    return false;
                }
                return false;
            });
        }).collect(Collectors.toList());
    }
}