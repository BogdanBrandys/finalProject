package com.kodilla.finalProject.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

public class UserActionEvent extends ApplicationEvent {
    private final Long userId;      // ID użytkownika
    private final String userEmail; // Email użytkownika
    private final ActionType action; // Typ akcji
    private final LocalDateTime timestamp; // Czas zdarzenia

    public UserActionEvent(Object source, Long userId, String userEmail, ActionType action) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }
}

