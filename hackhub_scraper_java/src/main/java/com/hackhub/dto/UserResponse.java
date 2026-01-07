package com.hackhub.dto;

import com.hackhub.model.Role;
import com.hackhub.model.SkillLevel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private UserProfileResponse profile;
    private LocalDateTime createdAt;
}





