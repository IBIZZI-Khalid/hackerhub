package com.hackhub.dto;

import com.hackhub.model.SkillLevel;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserProfileRequest {
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





