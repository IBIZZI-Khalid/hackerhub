package com.hackhub.recommendation;

import com.hackhub.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedItem {
    private Event event;
    private Double score;
    private String reason;
}


