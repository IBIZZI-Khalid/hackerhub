package com.hackhub.recommendation;

public enum RecommendationStrategy {
    COLLABORATIVE, // Pure collaborative filtering (LibRec)
    CONTENT_BASED, // Pure content-based filtering
    HYBRID, // Mix of collaborative + content-based
    COLD_START, // For new users (content + popular)
    POPULARITY // Trending/popular items only
}


