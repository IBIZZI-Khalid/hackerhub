package com.hackhub.controller;

import com.hackhub.model.EventType;
import com.hackhub.model.User;
import com.hackhub.recommendation.*;
import com.hackhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @Autowired
    private LibRecDataExporter dataExporter;

    @Autowired
    private CollaborativeFilteringService collaborativeFilteringService;

    /**
     * Get personalized recommendations
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @RequestParam(required = false) EventType type,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            User user = userService.getCurrentUser();
            RecommendationResponse response = recommendationService
                    .getRecommendations(user.getId(), limit, type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get similar events
     */
    @GetMapping("/similar/{eventId}")
    public ResponseEntity<List<RecommendedItem>> getSimilarEvents(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<RecommendedItem> similar = recommendationService
                    .getSimilarEvents(eventId, limit);
            return ResponseEntity.ok(similar);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Train collaborative filtering model (Admin only)
     */
    @PostMapping("/train")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaborativeFilteringService.ModelTrainingResult> trainModel() {
        try {
            CollaborativeFilteringService.ModelTrainingResult result = collaborativeFilteringService.trainModel();

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Export data to LibRec format (Admin only)
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportData() {
        try {
            dataExporter.exportToLibRecFormat();
            dataExporter.splitTrainTest();
            return ResponseEntity.ok("Data exported successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Export failed: " + e.getMessage());
        }
    }

    /**
     * Get dataset statistics (Admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibRecDataExporter.DatasetStats> getDatasetStats() {
        try {
            return ResponseEntity.ok(dataExporter.getDatasetStats());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


