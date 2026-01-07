package com.hackhub.dto;

import com.hackhub.model.SkillLevel;
import lombok.Data;

import java.util.Set;

@Data
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String company;
    private Integer yearsOfExperience;
    private SkillLevel skillLevel;
    private Set<String> interests;
    private Set<String> preferredProviders;
    private Set<String> learningGoals;
}





