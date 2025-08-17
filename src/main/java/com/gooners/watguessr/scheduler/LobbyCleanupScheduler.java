package com.gooners.watguessr.scheduler;

import com.gooners.watguessr.service.LobbyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LobbyCleanupScheduler {

	private final LobbyService lobbyService;

	public LobbyCleanupScheduler(LobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	@Scheduled(fixedRate = 60000) // every 1 minute
	public void scheduledCleanup() {
		int deleted = lobbyService.cleanupEmptyLobbies();
		if (deleted > 0) {
			System.out.println("Scheduled cleanup removed " + deleted + " empty lobbies");
		}
	}
}