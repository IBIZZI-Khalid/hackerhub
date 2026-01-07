package com.hackhub.dto;

import com.hackhub.model.InteractionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InteractionResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long eventId;
    private String eventTitle;
    private InteractionType type;
    private Double rating;
    private LocalDateTime timestamp;
    private Integer viewDurationSeconds;
}





