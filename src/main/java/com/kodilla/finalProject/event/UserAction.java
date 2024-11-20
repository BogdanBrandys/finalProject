package com.kodilla.finalProject.event;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userEmail;
    private ActionType action;
    private LocalDateTime timestamp;

    public UserAction(Long userId, String userEmail, ActionType action, LocalDateTime timestamp) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.timestamp = timestamp;
    }
}