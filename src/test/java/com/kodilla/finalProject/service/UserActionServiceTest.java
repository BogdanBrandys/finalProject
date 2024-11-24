package com.kodilla.finalProject.service;

import com.kodilla.finalProject.domain.User;
import com.kodilla.finalProject.event.ActionType;
import com.kodilla.finalProject.event.UserAction;
import com.kodilla.finalProject.event.UserActionEvent;
import com.kodilla.finalProject.event.UserActionEventListener;
import com.kodilla.finalProject.repository.UserActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionServiceTest {

    @InjectMocks
    private UserActionService userActionService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void shouldPublishUserActionEvent() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("testuser@example.com");
        ActionType action = ActionType.LOGIN;

        // When
        userActionService.publishUserActionEvent(user, action);

        // Then
        ArgumentCaptor<UserActionEvent> captor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        UserActionEvent publishedEvent = captor.getValue();

        assertEquals(1L, publishedEvent.getUserId());
        assertEquals("testuser@example.com", publishedEvent.getUserEmail());
        assertEquals(ActionType.LOGIN, publishedEvent.getAction());
    }
}