package com.gooners.watguessr.scheduler;

import com.gooners.watguessr.service.GameService;
import com.gooners.watguessr.service.RoundService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GameCleanupScheduler {

	private final GameService gameService;
	private final RoundService roundService;

	public GameCleanupScheduler(GameService gameService, RoundService roundService) {
		this.gameService = gameService;
        this.roundService = roundService;
    }

	// Run daily at 2 AM. Games are only eligible after 2 hours unfinished,
	// so a frequent poll never helped and just kept the scheduler busy.
	@Scheduled(cron = "0 0 2 * * *")
	public void scheduledCleanup() {
		int deleted = gameService.cleanupExpiredGames() + roundService.cleanupEmptyRoundsFromFinishedGames();
		if (deleted > 0) {
			System.out.println("Game cleanup scheduler: deleted " + deleted + " expired games and empty rounds");
		}
	}
}