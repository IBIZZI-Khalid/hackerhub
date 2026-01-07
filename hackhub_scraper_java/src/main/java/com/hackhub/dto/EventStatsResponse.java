package com.hackhub.dto;

import lombok.Data;

@Data
public class EventStatsResponse {
    private Long eventId;
    private String eventTitle;
    private Double averageRating;
    private Long totalViews;
    private Long totalLikes;
    private Long totalEnrollments;
    private Long totalBookmarks;
    private Long totalRatings;
    private Double popularityScore;
}





