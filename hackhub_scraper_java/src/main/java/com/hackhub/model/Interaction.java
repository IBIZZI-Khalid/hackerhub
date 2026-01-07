package com.hackhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // Prevent circular reference - fixes JSON nesting depth error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore // Prevent circular reference - fixes JSON nesting depth error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;

    // For RATE type
    @Column(name = "rating")
    private Double rating; // 1.0 to 5.0

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Implicit feedback signals
    @Column(name = "view_duration_seconds")
    private Integer viewDurationSeconds;

    @Column(name = "completed")
    private Boolean completed = false;

    // Metadata
    @Column(name = "device_type")
    private String deviceType; // web, mobile, tablet

    @Column(name = "source")
    private String source; // search, recommendation, browse

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (completed == null) {
            completed = false;
        }
    }
}
