package com.kodilla.finalProject.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;


public class UserActionEvent extends ApplicationEvent {
    private final Long userId;
    private final String userEmail;
    private final ActionType action;
    private final LocalDateTime timestamp;

    public UserActionEvent(Object source, Long userId, String userEmail, ActionType action) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }
}

