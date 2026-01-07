package com.hackhub.recommendation;

import com.hackhub.model.Event;
import com.hackhub.model.EventType;
import com.hackhub.model.Interaction;
import com.hackhub.repository.EventRepository;
import com.hackhub.service.InteractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class RecommendationService {

    @Autowired
    private ContentBasedRecommender contentBasedService;

    @Autowired
    private CollaborativeFilteringService collaborativeFilteringService;

    @Autowired
    private UserBasedCFService userBasedCFService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private EventRepository eventRepository;

    @Value("${librec.min.interactions.for.cf:5}")
    private int minInteractionsForCF;

    /**
     * Get personalized recommendations for user
     */
    public RecommendationResponse getRecommendations(Long userId, Integer limit, EventType type) {
        int topN = limit != null ? limit : 10;

        // Check if user has enough interactions for collaborative filtering
        List<Interaction> userInteractions = interactionService.getUserInteractions(userId);

        RecommendationStrategy strategy;
        List<RecommendedItem> recommendations;

        if (userInteractions.size() >= minInteractionsForCF && collaborativeFilteringService.isModelTrained()) {
            // Use collaborative filtering (LibRec-inspired algorithms)
            strategy = RecommendationStrategy.COLLABORATIVE;
            recommendations = getCollaborativeRecommendations(userId, topN, type);
        } else if (userInteractions.size() >= minInteractionsForCF) {
            // Use hybrid with content-based
            strategy = RecommendationStrategy.HYBRID;
            recommendations = getHybridRecommendations(userId, topN, type);
        } else {
            // Use cold start strategy
            strategy = RecommendationStrategy.COLD_START;
            recommendations = getColdStartRecommendations(userId, topN, type);
        }

        return new RecommendationResponse(
                userId,
                recommendations,
                strategy,
                LocalDateTime.now());
    }

    /**
     * Collaborative filtering using SVD++ matrix factorization
     */
    private List<RecommendedItem> getCollaborativeRecommendations(Long userId, int limit, EventType type) {
        List<Long> eventIds = collaborativeFilteringService.getRecommendations(userId, limit * 2);

        return eventIds.stream()
                .map(eventRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .filter(e -> type == null || e.getType() == type)
                .limit(limit)
                .map(e -> new RecommendedItem(e, 0.0, "Recommended by SVD++ algorithm"))
                .collect(Collectors.toList());
    }

    /**
     * Hybrid: Combine content-based + popular
     */
    private List<RecommendedItem> getHybridRecommendations(Long userId, int limit, EventType type) {
        List<RecommendedItem> recommendations = new ArrayList<>();

        // 70% Content-based
        int cbLimit = (int) (limit * 0.7);
        List<Event> cbEvents = contentBasedService.getRecommendations(userId, cbLimit);
        cbEvents.forEach(e -> recommendations.add(
                new RecommendedItem(e, 0.0, "Based on your profile and interests")));

        // 30% Popular
        int popLimit = limit - recommendations.size();
        List<Event> popularEvents = interactionService.getTrendingEvents(popLimit);
        popularEvents.forEach(e -> {
            if (recommendations.stream().noneMatch(r -> r.getEvent().getId().equals(e.getId()))) {
                recommendations.add(new RecommendedItem(e, 0.0, "Popular among users"));
            }
        });

        // Filter by type if specified
        Stream<RecommendedItem> stream = recommendations.stream();
        if (type != null) {
            stream = stream.filter(r -> r.getEvent().getType() == type);
        }

        return stream.limit(limit).collect(Collectors.toList());
    }

    /**
     * Cold start: Content-based + popular
     */
    private List<RecommendedItem> getColdStartRecommendations(Long userId, int limit, EventType type) {
        List<RecommendedItem> recommendations = new ArrayList<>();

        // 60% Content-based (profile matching)
        int cbLimit = (int) (limit * 0.6);
        List<Event> cbEvents = contentBasedService.getRecommendations(userId, cbLimit);
        cbEvents.forEach(e -> recommendations.add(
                new RecommendedItem(e, 0.0, "Matches your profile")));

        // 40% Popular events
        final int popLimit = limit - recommendations.size();
        final List<RecommendedItem> finalRecommendations = recommendations;
        List<Event> popularEvents = interactionService.getTrendingEvents(popLimit * 2);
        popularEvents.stream()
                .filter(e -> finalRecommendations.stream()
                        .noneMatch(r -> r.getEvent().getId().equals(e.getId())))
                .limit(popLimit)
                .forEach(e -> finalRecommendations.add(
                        new RecommendedItem(e, 0.0, "Trending event")));

        // Filter by type if specified
        if (type != null) {
            return finalRecommendations.stream()
                    .filter(r -> r.getEvent().getType() == type)
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return finalRecommendations.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get similar events to a given event
     */
    public List<RecommendedItem> getSimilarEvents(Long eventId, int limit) {
        // TODO: Implement with LibRec item-item similarity
        // For now, return trending events

        List<Event> similarEvents = interactionService.getTrendingEvents(limit);

        return similarEvents.stream()
                .limit(limit)
                .map(e -> new RecommendedItem(e, 0.0, "Similar event"))
                .collect(Collectors.toList());
    }
}


