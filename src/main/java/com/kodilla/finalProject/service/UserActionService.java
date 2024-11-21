package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.event.UserActionEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserActionService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishUserActionEvent(User user, ActionType action) {
        UserActionEvent event = new UserActionEvent(this, user.getId(), user.getEmail(), action);
        applicationEventPublisher.publishEvent(event);
    }
}