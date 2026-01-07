package com.hackhub.service;

import com.hackhub.dto.AuthResponse;
import com.hackhub.dto.LoginRequest;
import com.hackhub.dto.RegisterRequest;
import com.hackhub.model.Role;
import com.hackhub.model.User;
import com.hackhub.model.UserProfile;
import com.hackhub.repository.UserRepository;
import com.hackhub.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        // Create user profile if data provided
        if (hasProfileData(request)) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setJobTitle(request.getJobTitle());
            profile.setCompany(request.getCompany());
            profile.setYearsOfExperience(request.getYearsOfExperience());
            profile.setSkillLevel(request.getSkillLevel());
            profile.setInterests(request.getInterests());
            profile.setPreferredProviders(request.getPreferredProviders());
            profile.setLearningGoals(request.getLearningGoals());
            user.setProfile(profile);
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities("ROLE_" + savedUser.getRole().name())
                .build();

        String token = tokenProvider.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userDetails);

        // Get user details
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }

    private boolean hasProfileData(RegisterRequest request) {
        return request.getFirstName() != null ||
                request.getLastName() != null ||
                request.getJobTitle() != null ||
                request.getSkillLevel() != null ||
                (request.getInterests() != null && !request.getInterests().isEmpty());
    }
}





