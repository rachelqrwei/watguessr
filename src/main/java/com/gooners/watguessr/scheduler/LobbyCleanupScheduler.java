package com.gooners.watguessr.scheduler;

import com.gooners.watguessr.service.LobbyService;

// Soft-deleted: empty lobbies are already removed by leaveLobby and
// cleanupInactiveUsers (90s idle). This hourly sweep was redundant.
// @Component
public class LobbyCleanupScheduler {

	private final LobbyService lobbyService;

	public LobbyCleanupScheduler(LobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	// @Scheduled(fixedRate = 3600000)
	public void scheduledCleanup() {
		int deleted = lobbyService.cleanupEmptyLobbies();
	}
}