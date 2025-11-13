package com.example.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Model class for user registration form
public class RegisterForm {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    @NotBlank
    private String confirmPassword;

    // --- getters/setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}