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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LibRecDataExporter {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private InteractionService interactionService;

    @Value("${librec.data.path:src/main/resources/librec/data}")
    private String dataPath;

    /**
     * Export all interactions to LibRec format
     * Format: userId itemId rating timestamp
     */
    public void exportToLibRecFormat() throws IOException {
        List<Interaction> interactions = interactionRepository.findAll();

        File dataDir = new File(dataPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File ratingsFile = new File(dataDir, "ratings.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(ratingsFile))) {
            for (Interaction interaction : interactions) {
                // Convert interaction to rating
                double rating = interactionService.calculateImplicitRating(interaction);

                // Format: userId itemId rating timestamp
                writer.printf("%d %d %.1f %d%n",
                        interaction.getUser().getId(),
                        interaction.getEvent().getId(),
                        rating,
                        interaction.getTimestamp().toEpochSecond(ZoneOffset.UTC));
            }
        }

        log.info("Exported {} interactions to {}", interactions.size(), ratingsFile.getAbsolutePath());
    }

    /**
     * Split data into train/test sets (80/20)
     */
    public void splitTrainTest() throws IOException {
        File ratingsFile = new File(dataPath, "ratings.txt");

        if (!ratingsFile.exists()) {
            log.warn("Ratings file not found. Exporting data first...");
            exportToLibRecFormat();
        }

        List<String> lines = Files.readAllLines(ratingsFile.toPath());

        Collections.shuffle(lines);

        int trainSize = (int) (lines.size() * 0.8);
        List<String> trainLines = lines.subList(0, trainSize);
        List<String> testLines = lines.subList(trainSize, lines.size());

        // Write train set
        Files.write(new File(dataPath, "train.txt").toPath(), trainLines);

        // Write test set
        Files.write(new File(dataPath, "test.txt").toPath(), testLines);

        log.info("Split data: {} train, {} test", trainLines.size(), testLines.size());
    }

    /**
     * Export user-item matrix for collaborative filtering
     */
    public Map<Long, Map<Long, Double>> exportUserItemMatrix() {
        List<Interaction> interactions = interactionRepository.findAll();

        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        for (Interaction interaction : interactions) {
            Long userId = interaction.getUser().getId();
            Long eventId = interaction.getEvent().getId();

            // Convert interaction to rating
            double rating = interactionService.calculateImplicitRating(interaction);

            matrix.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(eventId, rating);
        }

        return matrix;
    }

    /**
     * Get statistics about the dataset
     */
    public DatasetStats getDatasetStats() {
        List<Interaction> interactions = interactionRepository.findAll();

        Set<Long> uniqueUsers = interactions.stream()
                .map(i -> i.getUser().getId())
                .collect(Collectors.toSet());

        Set<Long> uniqueEvents = interactions.stream()
                .map(i -> i.getEvent().getId())
                .collect(Collectors.toSet());

        double avgInteractionsPerUser = uniqueUsers.isEmpty() ? 0 : (double) interactions.size() / uniqueUsers.size();
        double avgInteractionsPerEvent = uniqueEvents.isEmpty() ? 0
                : (double) interactions.size() / uniqueEvents.size();

        // Calculate sparsity
        long possibleInteractions = (long) uniqueUsers.size() * uniqueEvents.size();
        double sparsity = possibleInteractions == 0 ? 0 : 1.0 - ((double) interactions.size() / possibleInteractions);

        return new DatasetStats(
                interactions.size(),
                uniqueUsers.size(),
                uniqueEvents.size(),
                avgInteractionsPerUser,
                avgInteractionsPerEvent,
                sparsity);
    }

    @Data
    @AllArgsConstructor
    public static class DatasetStats {
        private int totalInteractions;
        private int uniqueUsers;
        private int uniqueEvents;
        private double avgInteractionsPerUser;
        private double avgInteractionsPerEvent;
        private double sparsity;
    }
}


