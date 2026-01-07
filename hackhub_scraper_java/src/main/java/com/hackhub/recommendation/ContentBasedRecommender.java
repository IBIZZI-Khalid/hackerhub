package com.hackhub.recommendation;

import com.hackhub.model.Event;
import com.hackhub.model.SkillLevel;
import com.hackhub.model.UserProfile;
import com.hackhub.repository.EventRepository;
import com.hackhub.repository.UserProfileRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContentBasedRecommender {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Get recommendations based on user profile
     */
    public List<Event> getRecommendations(Long userId, int limit) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);

        if (profile == null) {
            log.warn("No profile found for user {}", userId);
            return Collections.emptyList();
        }

        List<Event> allEvents = eventRepository.findAll();

        // Calculate similarity score for each event
        return allEvents.stream()
                .map(event -> new ScoredEvent(event, calculateSimilarity(profile, event)))
                .filter(se -> se.getScore() > 0) // Only events with some match
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .map(ScoredEvent::getEvent)
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between user profile and event
     */
    private double calculateSimilarity(UserProfile profile, Event event) {
        double score = 0.0;

        // 1. Skill level matching (weight: 3.0)
        if (profile.getSkillLevel() != null && event.getLevel() != null) {
            if (matchesSkillLevel(profile.getSkillLevel(), event.getLevel())) {
                score += 3.0;
            }
        }

        // 2. Provider preference (weight: 2.0)
        if (profile.getPreferredProviders() != null &&
                profile.getPreferredProviders().contains(event.getProvider())) {
            score += 2.0;
        }

        // 3. Interest matching (weight: 1.0 per match)
        if (profile.getInterests() != null && event.getTitle() != null) {
            for (String interest : profile.getInterests()) {
                String lowerInterest = interest.toLowerCase();
                String lowerTitle = event.getTitle().toLowerCase();
                String lowerDesc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";

                if (lowerTitle.contains(lowerInterest) || lowerDesc.contains(lowerInterest)) {
                    score += 1.0;
                }
            }
        }

        // 4. Preferred Event Type (weight: 2.5)
        if (profile.getPreferredEventTypes() != null && event.getType() != null) {
            String typeName = event.getType().name();
            // Check implicit "S" removal just in case (e.g. HACKATHONS -> HACKATHON)
            boolean match = profile.getPreferredEventTypes().stream()
                    .anyMatch(pref -> pref.equalsIgnoreCase(typeName) ||
                            pref.equalsIgnoreCase(typeName + "S") ||
                            (pref.toUpperCase().endsWith("S")
                                    && pref.substring(0, pref.length() - 1).equalsIgnoreCase(typeName)));

            if (match) {
                score += 2.5;
            }
        }

        // 5. Event type boost for certifications (weight: 0.5)
        if (event.getType() != null && event.getType().toString().equals("CERTIFICATION")) {
            score += 0.5;
        }

        return score;
    }

    /**
     * Check if event level matches user skill level
     */
    private boolean matchesSkillLevel(SkillLevel userLevel, String eventLevel) {
        if (eventLevel == null)
            return false;

        String normalizedLevel = eventLevel.toLowerCase();

        switch (userLevel) {
            case BEGINNER:
                return normalizedLevel.contains("beginner") ||
                        normalizedLevel.contains("fundamental") ||
                        normalizedLevel.contains("intro") ||
                        normalizedLevel.contains("basic");
            case INTERMEDIATE:
                return normalizedLevel.contains("intermediate") ||
                        normalizedLevel.contains("associate") ||
                        normalizedLevel.contains("standard");
            case ADVANCED:
                return normalizedLevel.contains("advanced") ||
                        normalizedLevel.contains("expert") ||
                        normalizedLevel.contains("professional") ||
                        normalizedLevel.contains("architect");
            default:
                return false;
        }
    }

    /**
     * Helper class to store event with score
     */
    @Data
    @AllArgsConstructor
    private static class ScoredEvent {
        private Event event;
        private double score;
    }
}
