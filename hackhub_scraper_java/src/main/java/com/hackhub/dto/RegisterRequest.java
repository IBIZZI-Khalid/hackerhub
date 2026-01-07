package com.hackhub.dto;

import com.hackhub.model.SkillLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Optional profile data
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String company;
    private Integer yearsOfExperience;
    private SkillLevel skillLevel;
    private Set<String> interests = new HashSet<>();
    private Set<String> preferredProviders = new HashSet<>();
    private Set<String> learningGoals = new HashSet<>();
}





