package com.hackhub.recommendation;

import com.hackhub.model.Interaction;
import com.hackhub.repository.InteractionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User-Based Collaborative Filtering using Cosine Similarity
 * Inspired by LibRec's ItemKNN algorithm
 */
@Service
@Slf4j
public class UserBasedCFService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private LibRecDataExporter dataExporter;

    /**
     * Get recommendations using user-based collaborative filtering
     */
    public List<Long> getRecommendations(Long userId, int topN) {
        // 1. Get user-item matrix
        Map<Long, Map<Long, Double>> matrix = dataExporter.exportUserItemMatrix();

        if (!matrix.containsKey(userId)) {
            return Collections.emptyList();
        }

        // 2. Find similar users
        List<SimilarUser> similarUsers = findSimilarUsers(userId, matrix, 20);

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Get items rated highly by similar users
        Map<Long, Double> predictions = predictRatings(userId, similarUsers, matrix);

        // 4. Get items user hasn't interacted with
        Set<Long> userInteractedItems = matrix.get(userId).keySet();

        // 5. Sort by predicted rating and filter
        return predictions.entrySet().stream()
                .filter(entry -> !userInteractedItems.contains(entry.getKey()))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Find users similar to target user using cosine similarity
     */
    private List<SimilarUser> findSimilarUsers(Long targetUserId,
            Map<Long, Map<Long, Double>> matrix,
            int topN) {
        Map<Long, Double> targetUserRatings = matrix.get(targetUserId);
        if (targetUserRatings == null || targetUserRatings.isEmpty()) {
            return Collections.emptyList();
        }

        List<SimilarUser> similarities = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : matrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(targetUserId))
                continue;

            Map<Long, Double> otherUserRatings = entry.getValue();
            double similarity = cosineSimilarity(targetUserRatings, otherUserRatings);

            if (similarity > 0) {
                similarities.add(new SimilarUser(otherUserId, similarity));
            }
        }

        return similarities.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Calculate cosine similarity between two users
     */
    private double cosineSimilarity(Map<Long, Double> user1, Map<Long, Double> user2) {
        Set<Long> commonItems = new HashSet<>(user1.keySet());
        commonItems.retainAll(user2.keySet());

        if (commonItems.isEmpty())
            return 0.0;

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long itemId : commonItems) {
            double rating1 = user1.get(itemId);
            double rating2 = user2.get(itemId);

            dotProduct += rating1 * rating2;
            norm1 += rating1 * rating1;
            norm2 += rating2 * rating2;
        }

        if (norm1 == 0 || norm2 == 0)
            return 0.0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Predict ratings for items based on similar users
     */
    private Map<Long, Double> predictRatings(Long userId,
            List<SimilarUser> similarUsers,
            Map<Long, Map<Long, Double>> matrix) {
        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Double> similaritySum = new HashMap<>();

        for (SimilarUser similarUser : similarUsers) {
            Map<Long, Double> ratings = matrix.get(similarUser.getUserId());
            if (ratings == null)
                continue;

            for (Map.Entry<Long, Double> entry : ratings.entrySet()) {
                Long eventId = entry.getKey();
                Double rating = entry.getValue();

                predictions.merge(eventId,
                        rating * similarUser.getSimilarity(),
                        Double::sum);
                similaritySum.merge(eventId,
                        similarUser.getSimilarity(),
                        Double::sum);
            }
        }

        // Normalize by similarity sum
        for (Map.Entry<Long, Double> entry : predictions.entrySet()) {
            Long eventId = entry.getKey();
            double sum = similaritySum.get(eventId);
            if (sum > 0) {
                predictions.put(eventId, entry.getValue() / sum);
            }
        }

        return predictions;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SimilarUser {
        private Long userId;
        private double similarity;
    }
}


