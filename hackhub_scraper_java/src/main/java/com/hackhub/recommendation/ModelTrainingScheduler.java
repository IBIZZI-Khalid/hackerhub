package com.hackhub.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Automatic model training scheduler
 * Trains the collaborative filtering model automatically at startup and
 * periodically
 */
@Service
@Slf4j
public class ModelTrainingScheduler {

    @Autowired
    private CollaborativeFilteringService collaborativeFilteringService;

    @Autowired
    private LibRecDataExporter dataExporter;

    @Value("${librec.auto.train.enabled:true}")
    private boolean autoTrainEnabled;

    @Value("${librec.auto.train.min.interactions:20}")
    private int minInteractionsForTraining;

    /**
     * Train model automatically when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void trainModelOnStartup() {
        if (!autoTrainEnabled) {
            log.info("Automatic model training is disabled");
            return;
        }

        log.info("Application ready - checking if model training is needed...");

        try {
            // Check if we have enough data
            LibRecDataExporter.DatasetStats stats = dataExporter.getDatasetStats();

            if (stats.getTotalInteractions() < minInteractionsForTraining) {
                log.info("Not enough interactions for training ({} < {}). Skipping automatic training.",
                        stats.getTotalInteractions(), minInteractionsForTraining);
                return;
            }

            log.info("Starting automatic model training at startup...");
            log.info("Dataset: {} interactions, {} users, {} events",
                    stats.getTotalInteractions(), stats.getUniqueUsers(), stats.getUniqueEvents());

            CollaborativeFilteringService.ModelTrainingResult result = collaborativeFilteringService.trainModel();

            if (result.isSuccess()) {
                log.info("✅ Model training completed successfully!");
                log.info("   Algorithm: {}", result.getAlgorithm());
                log.info("   RMSE: {:.4f}", result.getRmse());
                log.info("   MAE: {:.4f}", result.getMae());
                log.info("   Training time: {}ms", result.getTrainingTimeMs());
                log.info("   Users: {}", result.getNumUsers());
                log.info("🎯 Collaborative filtering is now ACTIVE for all users!");
            } else {
                log.warn("❌ Model training failed at startup");
            }

        } catch (Exception e) {
            log.error("Error during automatic model training at startup", e);
        }
    }

    /**
     * Re-train model automatically every 24 hours
     * Runs at 2 AM every day
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledModelTraining() {
        if (!autoTrainEnabled) {
            return;
        }

        log.info("🔄 Starting scheduled model re-training...");

        try {
            LibRecDataExporter.DatasetStats stats = dataExporter.getDatasetStats();

            if (stats.getTotalInteractions() < minInteractionsForTraining) {
                log.info("Not enough interactions for training. Skipping scheduled training.");
                return;
            }

            CollaborativeFilteringService.ModelTrainingResult result = collaborativeFilteringService.trainModel();

            if (result.isSuccess()) {
                log.info("✅ Scheduled model re-training completed!");
                log.info("   RMSE: {:.4f}, MAE: {:.4f}, Time: {}ms",
                        result.getRmse(), result.getMae(), result.getTrainingTimeMs());
            } else {
                log.warn("❌ Scheduled model re-training failed");
            }

        } catch (Exception e) {
            log.error("Error during scheduled model training", e);
        }
    }

    /**
     * Manual trigger for model training (can be called by admin endpoint)
     */
    public CollaborativeFilteringService.ModelTrainingResult triggerManualTraining() {
        log.info("Manual model training triggered");
        return collaborativeFilteringService.trainModel();
    }
}


