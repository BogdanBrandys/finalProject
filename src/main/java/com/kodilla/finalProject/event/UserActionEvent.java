package com.kodilla.finalProject.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserActionEvent extends ApplicationEvent {
    private final Long userId;
    private final String userEmail;
    private final ActionType action;

    public UserActionEvent(Object source, Long userId, String userEmail, ActionType action) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
    }
}

