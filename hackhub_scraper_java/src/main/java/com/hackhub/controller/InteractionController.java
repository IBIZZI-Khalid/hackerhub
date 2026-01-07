package com.hackhub.controller;

import com.hackhub.dto.EventStatsResponse;
import com.hackhub.dto.InteractionRequest;
import com.hackhub.dto.InteractionResponse;
import com.hackhub.model.Event;
import com.hackhub.model.Interaction;
import com.hackhub.model.InteractionType;
import com.hackhub.model.User;
import com.hackhub.service.InteractionService;
import com.hackhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private UserService userService;

    // Track interaction
    @PostMapping("/track/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InteractionResponse> trackInteraction(
            @PathVariable Long eventId,
            @Valid @RequestBody InteractionRequest request) {
        try {
            Interaction interaction = interactionService.trackInteraction(
                    eventId,
                    request.getType(),
                    request.getRating(),
                    request.getViewDurationSeconds(),
                    request.getDeviceType(),
                    request.getSource());

            return ResponseEntity.ok(convertToResponse(interaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get user's interaction history
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InteractionResponse>> getUserHistory() {
        try {
            User user = userService.getCurrentUser();
            List<Interaction> interactions = interactionService
                    .getUserInteractions(user.getId());

            List<InteractionResponse> responses = interactions.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get user's recent interactions
    @GetMapping("/history/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InteractionResponse>> getRecentHistory(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            User user = userService.getCurrentUser();
            List<Interaction> interactions = interactionService
                    .getUserRecentInteractions(user.getId(), limit);

            List<InteractionResponse> responses = interactions.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get user's bookmarks
    @GetMapping("/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Event>> getUserBookmarks() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(interactionService.getUserBookmarks(user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get user's enrollments
    @GetMapping("/enrollments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Event>> getUserEnrollments() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(interactionService.getUserEnrollments(user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Check if user has bookmarked event
    @GetMapping("/bookmarks/check/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkBookmark(@PathVariable Long eventId) {
        try {
            User user = userService.getCurrentUser();
            boolean hasBookmarked = interactionService.hasUserBookmarked(
                    user.getId(), eventId);
            return ResponseEntity.ok(hasBookmarked);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    // Remove bookmark
    @DeleteMapping("/bookmarks/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long eventId) {
        try {
            User user = userService.getCurrentUser();
            interactionService.deleteInteraction(
                    user.getId(), eventId, InteractionType.BOOKMARK);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get event statistics
    @GetMapping("/stats/{eventId}")
    public ResponseEntity<EventStatsResponse> getEventStats(@PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(interactionService.getEventStats(eventId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get trending events
    @GetMapping("/trending")
    public ResponseEntity<List<Event>> getTrendingEvents(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(interactionService.getTrendingEvents(limit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get popular events by interaction type
    @GetMapping("/popular/{type}")
    public ResponseEntity<List<Event>> getPopularByType(
            @PathVariable InteractionType type,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(
                    interactionService.getPopularEventsByType(type, days, limit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Helper method to convert Interaction to Response
    private InteractionResponse convertToResponse(Interaction interaction) {
        InteractionResponse response = new InteractionResponse();
        response.setId(interaction.getId());
        response.setUserId(interaction.getUser().getId());
        response.setUsername(interaction.getUser().getUsername());
        response.setEventId(interaction.getEvent().getId());
        response.setEventTitle(interaction.getEvent().getTitle());
        response.setType(interaction.getType());
        response.setRating(interaction.getRating());
        response.setTimestamp(interaction.getTimestamp());
        response.setViewDurationSeconds(interaction.getViewDurationSeconds());
        return response;
    }
}


