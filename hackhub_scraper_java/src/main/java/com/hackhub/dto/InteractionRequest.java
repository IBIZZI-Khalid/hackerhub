package com.hackhub.dto;

import com.hackhub.model.InteractionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InteractionRequest {

    @NotNull(message = "Interaction type is required")
    private InteractionType type;

    // For RATE type
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Double rating;

    // For VIEW type
    private Integer viewDurationSeconds;

    // Metadata
    private String deviceType;
    private String source;
}





