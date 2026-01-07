package com.hackhub.repository;

import com.hackhub.model.Event;
import com.hackhub.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByProvider(String provider);

    List<Event> findByType(EventType type);

    Optional<Event> findByTitleAndProvider(String title, String provider);
}




