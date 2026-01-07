package com.hackhub.repository;

import com.hackhub.model.Interaction;
import com.hackhub.model.InteractionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    // Find user's interactions
    List<Interaction> findByUserIdOrderByTimestampDesc(Long userId);

    List<Interaction> findByUserIdAndType(Long userId, InteractionType type);

    // Find event's interactions
    List<Interaction> findByEventId(Long eventId);

    List<Interaction> findByEventIdAndType(Long eventId, InteractionType type);

    // Check if interaction exists
    Optional<Interaction> findByUserIdAndEventIdAndType(
            Long userId, Long eventId, InteractionType type);

    boolean existsByUserIdAndEventIdAndType(
            Long userId, Long eventId, InteractionType type);

    // Analytics queries
    @Query("SELECT i.event, COUNT(i) as count FROM Interaction i " +
            "WHERE i.type = :type AND i.timestamp > :since " +
            "GROUP BY i.event ORDER BY count DESC")
    List<Object[]> findMostPopularEventsByType(
            @Param("type") InteractionType type,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    @Query("SELECT AVG(i.rating) FROM Interaction i " +
            "WHERE i.event.id = :eventId AND i.type = 'RATE' AND i.rating IS NOT NULL")
    Double getAverageRating(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(i) FROM Interaction i " +
            "WHERE i.event.id = :eventId AND i.type = :type")
    Long countByEventIdAndType(
            @Param("eventId") Long eventId,
            @Param("type") InteractionType type);

    // Get user's recent interactions
    @Query("SELECT i FROM Interaction i WHERE i.user.id = :userId " +
            "ORDER BY i.timestamp DESC")
    List<Interaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}



