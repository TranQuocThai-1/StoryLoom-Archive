package com.storyloom.archive.model; // This line is what fixes the error!

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email address.")
    private String email;

    @NotBlank(message = "Screen name is required.")
    @Size(min = 3, max = 30, message = "Screen name must be between 3 and 30 characters.")
    private String screenName;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    private String password;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}