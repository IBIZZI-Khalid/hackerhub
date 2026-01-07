package com.hackhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "events", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "title", "provider" })
})
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private Double price;
    private LocalDate eventDate;
    private String location;
    private String sourceUrl;
    private String provider;

    // Certificate-specific fields
    private String category; // Technology area (e.g., "Oracle Cloud Infrastructure", "Database")
    private String examCode; // Exam code (e.g., "1Z0-1085-25")
    private String level; // Certification level (e.g., "Associate", "Professional", "Foundations")

    // Scraper-specific fields (from original Event entity)
    private LocalDateTime scrappedAt;
    private String blurb;
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String judges;

    private String judgingCriteria;
    private LocalDate parsedDate;

    @PrePersist
    protected void onCreate() {
        if (scrappedAt == null) {
            scrappedAt = LocalDateTime.now();
        }
    }
}
