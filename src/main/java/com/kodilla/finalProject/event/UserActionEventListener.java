package com.kodilla.finalProject.event;

import com.kodilla.finalProject.repository.UserActionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class UserActionEventListener {

    private final UserActionRepository userActionRepository;

    @EventListener
    public void onUserActionEvent(UserActionEvent event) {
        UserAction userAction = new UserAction(
                event.getUserId(),
                event.getUserEmail(),
                event.getAction(),
                LocalDateTime.now());

        userActionRepository.save(userAction);
    }
}
