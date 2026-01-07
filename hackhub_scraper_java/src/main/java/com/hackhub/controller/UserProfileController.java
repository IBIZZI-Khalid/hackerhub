package com.hackhub.controller;

import com.hackhub.model.User;
import com.hackhub.model.UserProfile;
import com.hackhub.model.dto.UpdateInterestsRequest;
import com.hackhub.repository.UserProfileRepository;
import com.hackhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @PostMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePreferences(@RequestBody UpdateInterestsRequest request) {
        try {
            User user = userService.getCurrentUser();
            UserProfile profile = user.getProfile();

            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(user);
                user.setProfile(profile);
            }

            if (request.getInterests() != null) {
                profile.setInterests(request.getInterests());
            }

            if (request.getEventTypes() != null) {
                profile.setPreferredEventTypes(request.getEventTypes());
            }

            if (request.getProviders() != null) {
                profile.setPreferredProviders(request.getProviders());
            }

            userProfileRepository.save(profile);
            return ResponseEntity.ok("Preferences updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating preferences: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfile> getProfile() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(user.getProfile());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
