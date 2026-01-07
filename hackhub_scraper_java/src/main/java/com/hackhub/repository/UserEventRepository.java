package com.hackhub.repository;

import com.hackhub.model.User;
import com.hackhub.model.UserEvent;
import com.hackhub.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    List<UserEvent> findByUser(User user);

    List<UserEvent> findByUserAndIsFavorite(User user, Boolean isFavorite);

    boolean existsByUserAndEvent(User user, Event event);
}




