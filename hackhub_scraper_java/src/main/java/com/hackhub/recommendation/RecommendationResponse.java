package com.hackhub.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResponse {
    private Long userId;
    private List<RecommendedItem> recommendations;
    private RecommendationStrategy strategy;
    private LocalDateTime generatedAt;
}


