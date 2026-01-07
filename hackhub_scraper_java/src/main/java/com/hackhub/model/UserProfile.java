package com.hackhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // Prevent circular reference with User - fixes JSON nesting depth error
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Demographics
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String company;
    private Integer yearsOfExperience;

    // Preferences
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_providers", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "provider")
    private Set<String> preferredProviders = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_types", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "event_type")
    private Set<String> preferredEventTypes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_goals", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "goal")
    private Set<String> learningGoals = new HashSet<>();
}
