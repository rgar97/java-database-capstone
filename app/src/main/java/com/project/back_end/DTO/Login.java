package com.project.back_end.DTO;

import jakarta.validation.constraints.NotBlank;

public class Login {

    @NotBlank(message = "El identificador es requerido")
    private String identifier;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    public Login() {
    }

    public Login(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    // Getters y Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}