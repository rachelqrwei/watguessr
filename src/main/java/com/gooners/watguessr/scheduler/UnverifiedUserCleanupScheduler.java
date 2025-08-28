package com.gooners.watguessr.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gooners.watguessr.service.UserService;

@Component
public class UnverifiedUserCleanupScheduler {

    private final UserService userService;

    public UnverifiedUserCleanupScheduler(UserService userService) {
        this.userService = userService;
    }

    // Run daily at 2 AM
    @Scheduled(cron = "0 * * * * *")
    public void cleanupUnverifiedUsers() {
        int deletedCount = userService.cleanupUnverifiedUsers();
        if (deletedCount > 0) {
            System.out.println("Unverified user cleanup: deleted " + deletedCount + " unverified users older than 1 day");
        }
    }
}
