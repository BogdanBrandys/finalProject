package com.kodilla.finalProject.scheduler;


import com.kodilla.finalProject.config.AdminConfig;
import com.kodilla.finalProject.mailing.Mail;
import com.kodilla.finalProject.mailing.SimpleEmailService;
import com.kodilla.finalProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private static final String SUBJECT = "MoviesCollection: Once a month - Number of users";
    private final SimpleEmailService simpleEmailService;
    private final AdminConfig adminConfig;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 10 1 * *") //first day of the month
    public void sendInformationEmail() {
        long size = userRepository.count();
        simpleEmailService.send(
                new Mail(
                        adminConfig.getAdminMail(),
                        SUBJECT,
                        "Currently in database you've got: " + size + " users"
                )
        );
    }
}