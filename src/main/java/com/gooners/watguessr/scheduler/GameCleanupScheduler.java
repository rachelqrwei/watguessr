package com.gooners.watguessr.scheduler;

import com.gooners.watguessr.service.GameService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GameCleanupScheduler {

	private final GameService gameService;

	public GameCleanupScheduler(GameService gameService) {
		this.gameService = gameService;
	}

	@Scheduled(fixedRate = 60000) // every 1 minute
	public void scheduledCleanup() {
		int deleted = gameService.cleanupExpiredGames();
		if (deleted > 0) {
			System.out.println("Game cleanup scheduler: deleted " + deleted + " expired games");
		}
	}
}