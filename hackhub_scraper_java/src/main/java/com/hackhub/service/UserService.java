package com.hackhub.service;

import com.hackhub.dto.UserProfileRequest;
import com.hackhub.dto.UserProfileResponse;
import com.hackhub.dto.UserResponse;
import com.hackhub.model.User;
import com.hackhub.model.UserProfile;
import com.hackhub.repository.UserProfileRepository;
import com.hackhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToUserResponse(user);
    }

    public UserResponse getCurrentUserResponse() {
        User user = getCurrentUser();
        return convertToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UserProfileRequest request) {
        User user = getCurrentUser();

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        // Update profile fields
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setJobTitle(request.getJobTitle());
        profile.setCompany(request.getCompany());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setSkillLevel(request.getSkillLevel());
        profile.setInterests(request.getInterests());
        profile.setPreferredProviders(request.getPreferredProviders());
        profile.setLearningGoals(request.getLearningGoals());

        userProfileRepository.save(profile);
        User savedUser = userRepository.save(user);

        return convertToUserResponse(savedUser);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getProfile() != null) {
            response.setProfile(convertToProfileResponse(user.getProfile()));
        }

        return response;
    }

    private UserProfileResponse convertToProfileResponse(UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setJobTitle(profile.getJobTitle());
        response.setCompany(profile.getCompany());
        response.setYearsOfExperience(profile.getYearsOfExperience());
        response.setSkillLevel(profile.getSkillLevel());
        response.setInterests(profile.getInterests());
        response.setPreferredProviders(profile.getPreferredProviders());
        response.setLearningGoals(profile.getLearningGoals());
        return response;
    }
}





