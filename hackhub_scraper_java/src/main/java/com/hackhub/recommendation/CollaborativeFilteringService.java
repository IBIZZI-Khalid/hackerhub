package com.hackhub.recommendation;

import com.hackhub.model.Interaction;
import com.hackhub.repository.InteractionRepository;
import com.hackhub.service.InteractionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Collaborative Filtering Service using Matrix Factorization (SVD-like
 * algorithm)
 * Inspired by LibRec's SVD++ algorithm
 */
@Service
@Slf4j
public class CollaborativeFilteringService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private LibRecDataExporter dataExporter;

    @Autowired
    private InteractionService interactionService;

    @Value("${librec.svdpp.factors:20}")
    private int numFactors;

    @Value("${librec.svdpp.iterations:100}")
    private int numIterations;

    @Value("${librec.svdpp.learnRate:0.01}")
    private double learnRate;

    // Model parameters (trained)
    private Map<Long, double[]> userFactors = new HashMap<>();
    private Map<Long, double[]> itemFactors = new HashMap<>();
    private Map<Long, Double> userBiases = new HashMap<>();
    private Map<Long, Double> itemBiases = new HashMap<>();
    private double globalMean = 0.0;
    private boolean modelTrained = false;

    /**
     * Train the collaborative filtering model using SVD++ approach
     */
    public ModelTrainingResult trainModel() {
        log.info("Starting collaborative filtering model training...");
        long startTime = System.currentTimeMillis();

        try {
            // 1. Export data
            dataExporter.exportToLibRecFormat();

            // 2. Load training data
            Map<Long, Map<Long, Double>> userItemMatrix = dataExporter.exportUserItemMatrix();

            if (userItemMatrix.isEmpty()) {
                log.warn("No training data available");
                return new ModelTrainingResult("SVD++", 0, 0, 0, 0, false);
            }

            // 3. Initialize model
            initializeModel(userItemMatrix);

            // 4. Train using gradient descent
            double rmse = trainWithGradientDescent(userItemMatrix);

            long trainingTime = System.currentTimeMillis() - startTime;
            modelTrained = true;

            log.info("Model training completed in {}ms - RMSE: {}", trainingTime, rmse);

            return new ModelTrainingResult(
                    "SVD++",
                    rmse,
                    rmse * 0.8, // MAE approximation
                    trainingTime,
                    userItemMatrix.size(),
                    true);

        } catch (Exception e) {
            log.error("Error training model", e);
            return new ModelTrainingResult("SVD++", 0, 0, 0, 0, false);
        }
    }

    /**
     * Initialize user and item factors randomly
     */
    private void initializeModel(Map<Long, Map<Long, Double>> userItemMatrix) {
        Random random = new Random(42); // Fixed seed for reproducibility

        // Calculate global mean
        double sum = 0;
        int count = 0;
        for (Map<Long, Double> items : userItemMatrix.values()) {
            for (Double rating : items.values()) {
                sum += rating;
                count++;
            }
        }
        globalMean = count > 0 ? sum / count : 3.0;

        // Initialize user factors and biases
        for (Long userId : userItemMatrix.keySet()) {
            userFactors.put(userId, randomVector(numFactors, random));
            userBiases.put(userId, 0.0);
        }

        // Initialize item factors and biases
        Set<Long> allItems = new HashSet<>();
        for (Map<Long, Double> items : userItemMatrix.values()) {
            allItems.addAll(items.keySet());
        }
        for (Long itemId : allItems) {
            itemFactors.put(itemId, randomVector(numFactors, random));
            itemBiases.put(itemId, 0.0);
        }

        log.info("Model initialized: {} users, {} items, {} factors",
                userFactors.size(), itemFactors.size(), numFactors);
    }

    /**
     * Train model using stochastic gradient descent
     */
    private double trainWithGradientDescent(Map<Long, Map<Long, Double>> userItemMatrix) {
        double regularization = 0.01;

        for (int iter = 0; iter < numIterations; iter++) {
            double squaredError = 0;
            int count = 0;

            // Iterate over all ratings
            for (Map.Entry<Long, Map<Long, Double>> userEntry : userItemMatrix.entrySet()) {
                Long userId = userEntry.getKey();
                Map<Long, Double> items = userEntry.getValue();

                for (Map.Entry<Long, Double> itemEntry : items.entrySet()) {
                    Long itemId = itemEntry.getKey();
                    double actualRating = itemEntry.getValue();

                    // Predict rating
                    double predictedRating = predictRating(userId, itemId);

                    // Calculate error
                    double error = actualRating - predictedRating;
                    squaredError += error * error;
                    count++;

                    // Update biases
                    double userBias = userBiases.get(userId);
                    double itemBias = itemBiases.get(itemId);

                    userBiases.put(userId, userBias + learnRate * (error - regularization * userBias));
                    itemBiases.put(itemId, itemBias + learnRate * (error - regularization * itemBias));

                    // Update factors
                    double[] userFactor = userFactors.get(userId);
                    double[] itemFactor = itemFactors.get(itemId);

                    for (int f = 0; f < numFactors; f++) {
                        double uf = userFactor[f];
                        double if_ = itemFactor[f];

                        userFactor[f] += learnRate * (error * if_ - regularization * uf);
                        itemFactor[f] += learnRate * (error * uf - regularization * if_);
                    }
                }
            }

            // Calculate RMSE for this iteration
            double rmse = Math.sqrt(squaredError / count);

            if (iter % 10 == 0) {
                log.debug("Iteration {}/{} - RMSE: {}", iter, numIterations, rmse);
            }
        }

        // Final RMSE
        double finalSquaredError = 0;
        int finalCount = 0;
        for (Map.Entry<Long, Map<Long, Double>> userEntry : userItemMatrix.entrySet()) {
            for (Map.Entry<Long, Double> itemEntry : userEntry.getValue().entrySet()) {
                double error = itemEntry.getValue() - predictRating(userEntry.getKey(), itemEntry.getKey());
                finalSquaredError += error * error;
                finalCount++;
            }
        }

        return Math.sqrt(finalSquaredError / finalCount);
    }

    /**
     * Predict rating for user-item pair
     */
    public double predictRating(Long userId, Long itemId) {
        if (!modelTrained) {
            return globalMean;
        }

        double prediction = globalMean;

        // Add biases
        if (userBiases.containsKey(userId)) {
            prediction += userBiases.get(userId);
        }
        if (itemBiases.containsKey(itemId)) {
            prediction += itemBiases.get(itemId);
        }

        // Add dot product of factors
        if (userFactors.containsKey(userId) && itemFactors.containsKey(itemId)) {
            double[] uf = userFactors.get(userId);
            double[] if_ = itemFactors.get(itemId);

            for (int f = 0; f < numFactors; f++) {
                prediction += uf[f] * if_[f];
            }
        }

        // Clamp to valid rating range
        return Math.max(1.0, Math.min(5.0, prediction));
    }

    /**
     * Get top-N recommendations for user
     */
    public List<Long> getRecommendations(Long userId, int topN) {
        if (!modelTrained) {
            log.warn("Model not trained yet");
            return Collections.emptyList();
        }

        // Get items user hasn't interacted with
        Map<Long, Map<Long, Double>> userItemMatrix = dataExporter.exportUserItemMatrix();
        Set<Long> userItems = userItemMatrix.getOrDefault(userId, Collections.emptyMap()).keySet();

        // Predict ratings for all items
        Map<Long, Double> predictions = new HashMap<>();
        for (Long itemId : itemFactors.keySet()) {
            if (!userItems.contains(itemId)) {
                predictions.put(itemId, predictRating(userId, itemId));
            }
        }

        // Sort by predicted rating and return top-N
        return predictions.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Check if model is trained
     */
    public boolean isModelTrained() {
        return modelTrained;
    }

    /**
     * Generate random vector
     */
    private double[] randomVector(int size, Random random) {
        double[] vector = new double[size];
        for (int i = 0; i < size; i++) {
            vector[i] = random.nextGaussian() * 0.01; // Small random values
        }
        return vector;
    }

    @Data
    @AllArgsConstructor
    public static class ModelTrainingResult {
        private String algorithm;
        private double rmse;
        private double mae;
        private long trainingTimeMs;
        private int numUsers;
        private boolean success;
    }
}


