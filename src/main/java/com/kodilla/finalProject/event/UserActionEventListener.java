package com.kodilla.finalProject.event;

import com.kodilla.finalProject.repository.UserActionRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserActionEventListener {

    private final UserActionRepository userActionRepository;

    @EventListener
    public void onUserActionEvent(UserAction event) {
        UserAction userAction = new UserAction(
                event.getUserId(),
                event.getUserEmail(),
                event.getAction(),
                event.getTimestamp());

        userActionRepository.save(userAction);
    }
}
