package com.hackhub.service;

import com.hackhub.model.Event;
import com.hackhub.model.EventType;
import com.hackhub.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByType(EventType type) {
        return eventRepository.findByType(type);
    }

    public List<Event> getEventsByProvider(String provider) {
        return eventRepository.findByProvider(provider);
    }

    public Event saveEvent(Event event) {
        // Simple deduplication logic based on Title and Provider
        return eventRepository.findByTitleAndProvider(event.getTitle(), event.getProvider())
                .orElseGet(() -> {
                    // Ensure scrappedAt is set if not already handled by @PrePersist
                    if (event.getScrappedAt() == null) {
                        event.setScrappedAt(LocalDateTime.now());
                    }
                    return eventRepository.save(event); // Returns event with generated ID
                });
    }
}




