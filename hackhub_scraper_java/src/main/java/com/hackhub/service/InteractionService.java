package com.hackhub.service;

import com.hackhub.dto.EventStatsResponse;
import com.hackhub.model.Event;
import com.hackhub.model.Interaction;
import com.hackhub.model.InteractionType;
import com.hackhub.model.User;
import com.hackhub.repository.EventRepository;
import com.hackhub.repository.InteractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EventRepository eventRepository;

    // Track interaction
    @Transactional
    public Interaction trackInteraction(Long eventId, InteractionType type,
            Double rating, Integer viewDuration,
            String deviceType, String source) {
        User user = userService.getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check if interaction already exists (for non-repeatable types)
        if (type == InteractionType.BOOKMARK || type == InteractionType.ENROLL) {
            Optional<Interaction> existing = interactionRepository
                    .findByUserIdAndEventIdAndType(user.getId(), eventId, type);
            if (existing.isPresent()) {
                return existing.get(); // Already tracked
            }
        }

        // For RATE type, update existing rating if present
        if (type == InteractionType.RATE) {
            Optional<Interaction> existing = interactionRepository
                    .findByUserIdAndEventIdAndType(user.getId(), eventId, type);
            if (existing.isPresent()) {
                Interaction existingInteraction = existing.get();
                existingInteraction.setRating(rating);
                existingInteraction.setTimestamp(LocalDateTime.now());
                return interactionRepository.save(existingInteraction);
            }
        }

        Interaction interaction = new Interaction();
        interaction.setUser(user);
        interaction.setEvent(event);
        interaction.setType(type);
        interaction.setRating(rating);
        interaction.setViewDurationSeconds(viewDuration);
        interaction.setDeviceType(deviceType);
        interaction.setSource(source);
        interaction.setTimestamp(LocalDateTime.now());

        return interactionRepository.save(interaction);
    }

    // Get user's interaction history
    public List<Interaction> getUserInteractions(Long userId) {
        return interactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    // Get user's recent interactions
    public List<Interaction> getUserRecentInteractions(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return interactionRepository.findRecentByUserId(userId, pageable);
    }

    // Get user's bookmarked events
    public List<Event> getUserBookmarks(Long userId) {
        return interactionRepository.findByUserIdAndType(userId, InteractionType.BOOKMARK)
                .stream()
                .map(Interaction::getEvent)
                .collect(Collectors.toList());
    }

    // Get user's enrolled events
    public List<Event> getUserEnrollments(Long userId) {
        return interactionRepository.findByUserIdAndType(userId, InteractionType.ENROLL)
                .stream()
                .map(Interaction::getEvent)
                .collect(Collectors.toList());
    }

    // Check if user has bookmarked event
    public boolean hasUserBookmarked(Long userId, Long eventId) {
        return interactionRepository.existsByUserIdAndEventIdAndType(
                userId, eventId, InteractionType.BOOKMARK);
    }

    // Check if user has enrolled in event
    public boolean hasUserEnrolled(Long userId, Long eventId) {
        return interactionRepository.existsByUserIdAndEventIdAndType(
                userId, eventId, InteractionType.ENROLL);
    }

    // Get event statistics
    public EventStatsResponse getEventStats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventStatsResponse stats = new EventStatsResponse();
        stats.setEventId(eventId);
        stats.setEventTitle(event.getTitle());

        Double avgRating = interactionRepository.getAverageRating(eventId);
        stats.setAverageRating(avgRating != null ? avgRating : 0.0);

        stats.setTotalViews(interactionRepository.countByEventIdAndType(
                eventId, InteractionType.VIEW));
        stats.setTotalLikes(interactionRepository.countByEventIdAndType(
                eventId, InteractionType.LIKE));
        stats.setTotalEnrollments(interactionRepository.countByEventIdAndType(
                eventId, InteractionType.ENROLL));
        stats.setTotalBookmarks(interactionRepository.countByEventIdAndType(
                eventId, InteractionType.BOOKMARK));

        Long ratingCount = interactionRepository.countByEventIdAndType(
                eventId, InteractionType.RATE);
        stats.setTotalRatings(ratingCount);

        // Calculate popularity score
        double popularityScore = calculatePopularityScore(
                stats.getTotalViews(),
                stats.getTotalLikes(),
                stats.getTotalBookmarks(),
                stats.getTotalEnrollments(),
                avgRating != null ? avgRating : 0.0);
        stats.setPopularityScore(popularityScore);

        return stats;
    }

    // Get trending events
    public List<Event> getTrendingEvents(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> results = interactionRepository
                .findMostPopularEventsByType(InteractionType.VIEW, since, pageable);

        return results.stream()
                .map(row -> (Event) row[0])
                .collect(Collectors.toList());
    }

    // Get popular events by type
    public List<Event> getPopularEventsByType(InteractionType type, int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> results = interactionRepository
                .findMostPopularEventsByType(type, since, pageable);

        return results.stream()
                .map(row -> (Event) row[0])
                .collect(Collectors.toList());
    }

    // Calculate popularity score
    private double calculatePopularityScore(Long views, Long likes, Long bookmarks,
            Long enrollments, Double avgRating) {
        double score = 0.0;

        // Weights for different interactions
        score += (views != null ? views : 0) * 0.1;
        score += (likes != null ? likes : 0) * 0.2;
        score += (bookmarks != null ? bookmarks : 0) * 0.3;
        score += (enrollments != null ? enrollments : 0) * 0.5;
        score += (avgRating != null ? avgRating : 0) * 2.0;

        return Math.round(score * 100.0) / 100.0;
    }

    // Convert implicit feedback to rating (for LibRec)
    public double calculateImplicitRating(Interaction interaction) {
        double rating = 0.0;

        switch (interaction.getType()) {
            case VIEW:
                rating = 1.0;
                if (interaction.getViewDurationSeconds() != null &&
                        interaction.getViewDurationSeconds() > 60) {
                    rating = 2.0;
                }
                break;
            case LIKE:
                rating = 3.0;
                break;
            case BOOKMARK:
                rating = 3.5;
                break;
            case ENROLL:
                rating = 4.5;
                break;
            case COMPLETE:
                rating = 5.0;
                break;
            case RATE:
                rating = interaction.getRating() != null ? interaction.getRating() : 0.0;
                break;
        }

        return rating;
    }

    // Delete interaction (e.g., remove bookmark)
    @Transactional
    public void deleteInteraction(Long userId, Long eventId, InteractionType type) {
        Optional<Interaction> interaction = interactionRepository
                .findByUserIdAndEventIdAndType(userId, eventId, type);
        interaction.ifPresent(interactionRepository::delete);
    }
}



