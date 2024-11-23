package com.kodilla.finalProject.service;

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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActionServiceTest {

    @InjectMocks
    private UserActionEventListener userActionEventListener;

    @Mock
    private UserActionRepository userActionRepository;

    @Test
    void testOnUserActionEvent() {
        // Given
        UserActionEvent event = mock(UserActionEvent.class);
        when(event.getUserId()).thenReturn(1L);
        when(event.getUserEmail()).thenReturn("testuser@example.com");
        when(event.getAction()).thenReturn(ActionType.LOGIN);
        // When
        userActionEventListener.onUserActionEvent(event);

        // Then
        verify(userActionRepository).save(any(UserAction.class));

        ArgumentCaptor<UserAction> captor = ArgumentCaptor.forClass(UserAction.class);
        verify(userActionRepository).save(captor.capture());
        UserAction savedAction = captor.getValue();

        assertEquals(1L, savedAction.getUserId());
        assertEquals("testuser@example.com", savedAction.getUserEmail());
        assertEquals(ActionType.LOGIN, savedAction.getAction());
    }
}