package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
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
            if (doctor.getPassword() != null && !doctor.getPassword().startsWith("$2")) {
                doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
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

        if (doctor != null && matchesPassword(login.getPassword(), doctor.getPassword())) {
            if (!doctor.getPassword().startsWith("$2")) {
                doctor.setPassword(passwordEncoder.encode(login.getPassword()));
                doctorRepository.save(doctor);
            }
            String token = tokenService.generateToken(doctor.getId(), "doctor");
            response.put("token", token);
            response.put("role", "doctor");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("message", "Credenciales de doctor inválidas.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        return storedPassword.startsWith("$2")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
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