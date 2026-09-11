package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class TokenService {

    @Value("${jwt.secret:defaultSecretKeyForClinicManagementSystem1234567890}")
    private String jwtSecret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Genera la clave de firma HMAC basada en el secreto configurado.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT válido por 7 días conteniendo el identificador como sujeto.
     */
    public String generateToken(Object idOrIdentifier, String role) {
        String identifier = String.valueOf(idOrIdentifier);
        long expirationTime = 7L * 24 * 60 * 60 * 1000; // 7 días en milisegundos

        return Jwts.builder()
                .setSubject(identifier)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el identificador (sujeto) del token JWT.
     */
    public String extractIdentifier(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Obtiene el ID numérico del usuario a partir del token (utilizado en servicios).
     */
    public Long getUserIdFromToken(String token) {
        try {
            String identifier = extractIdentifier(token);
            return Long.parseLong(identifier);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Valida el token JWT y verifica que el usuario exista según su rol.
     */
    public boolean validateToken(String token, String userRole) {
        try {
            String identifier = extractIdentifier(token);
            if (identifier == null || identifier.trim().isEmpty()) {
                return false;
            }

            switch (userRole.toLowerCase()) {
                case "admin":
                    Admin admin = adminRepository.findByUsername(identifier);
                    if (admin != null) return true;
                    try {
                        Long adminId = Long.parseLong(identifier);
                        return adminRepository.existsById(adminId);
                    } catch (NumberFormatException e) {
                        return false;
                    }

                case "doctor":
                    Doctor doctor = doctorRepository.findByEmail(identifier);
                    if (doctor != null) return true;
                    try {
                        Long doctorId = Long.parseLong(identifier);
                        return doctorRepository.existsById(doctorId);
                    } catch (NumberFormatException e) {
                        return false;
                    }

                case "patient":
                    Patient patient = patientRepository.findByEmail(identifier);
                    if (patient != null) return true;
                    try {
                        Long patientId = Long.parseLong(identifier);
                        return patientRepository.existsById(patientId);
                    } catch (NumberFormatException e) {
                        return false;
                    }

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}