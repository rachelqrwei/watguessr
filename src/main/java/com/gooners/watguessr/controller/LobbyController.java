package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Controller
public class LobbyController {

	private final LobbyService lobbyService;

	@Autowired
	public LobbyController(LobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	// User joins a lobby
	@MessageMapping("/lobby/join")
	public void joinLobby(@Payload JoinLobbyRequest request) {
        // TODO: cant join lobby already started.
		lobbyService.joinLobby(UUID.fromString(request.getLobbyId()), request.getUser());
	}

	// User leaves a lobby
	@MessageMapping("/lobby/leave")
	public void leaveLobby(@Payload JoinLobbyRequest request) {
		lobbyService.leaveLobby(UUID.fromString(request.getLobbyId()), request.getUser());
	}

	// User sets ready status
	@MessageMapping("/lobby/ready")
	public void setPlayerReady(@Payload SetReadyRequest request) {
		lobbyService.setPlayerReady(UUID.fromString(request.getLobbyId()), request.getUserId(), request.isReady());
	}

	// Attempt to start the game manually (optional)
	@MessageMapping("/lobby/start")
	public UUID startGame(@Payload StartGameRequest request) {
        // TODO: cant start game already started.
		return lobbyService.tryStartGame(UUID.fromString(request.getLobbyId()), request.getGameMode(), request.getRoundCount(), request.getTimer());
	}

	@MessageMapping("/lobby/cleanup")
	public String cleanupLobby(String lobbyId) {
		boolean success = lobbyService.cleanupStaleLobby(lobbyId);
		return success ? "success" : "failure";
	}

	// Handle heartbeat from lobby users
	@MessageMapping("/lobby/heartbeat")
	public void handleHeartbeat(@Payload HeartbeatRequest request) {
		lobbyService.updateLastSeen(UUID.fromString(request.getLobbyId()), request.getUserId());
	}

	// REST API endpoint for lobby cleanup (fallback when WebSocket not available)
	@PostMapping("/api/lobby/cleanup")
	public ResponseEntity<String> cleanupLobbyRest(@RequestBody CleanupRequest request) {
		boolean success = false;
		
		if (request.getUserId() != null) {
			// Remove specific user from lobby
			success = lobbyService.removeUserFromLobby(request.getLobbyId(), request.getUserId());
		} else {
			// Clean up entire lobby
			success = lobbyService.cleanupStaleLobby(request.getLobbyId());
		}
		
		return ResponseEntity.ok(success ? "success" : "failure");
	}


	public static class JoinLobbyRequest {
		private String lobbyId;
		private User user;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

		public User getUser() { return user; }
		public void setUser(User user) { this.user = user; }
	}

	public static class SetReadyRequest {
		private String lobbyId;
		private String userId;
		private boolean ready;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

		public String getUserId() { return userId; }
		public void setUserId(String userId) { this.userId = userId; }

		public boolean isReady() { return ready; }
		public void setReady(boolean ready) { this.ready = ready; }
	}

	public static class StartGameRequest {
		private String lobbyId;
		private String gameMode;
		private Integer roundCount;
		private Integer timer;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

		public String getGameMode() { return gameMode; }
		public void setGameMode(String gameMode) { this.gameMode = gameMode; }

		public Integer getRoundCount() { return roundCount; }
		public void setRoundCount(Integer roundCount) { this.roundCount = roundCount; }

		public Integer getTimer() { return timer; }
		public void setTimer(Integer timer) { this.timer = timer; }
	}

	public static class CleanupRequest {
		private String lobbyId;
		private String userId;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

		public String getUserId() { return userId; }
		public void setUserId(String userId) { this.userId = userId; }
	}

	public static class HeartbeatRequest {
		private String lobbyId;
		private String userId;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

		public String getUserId() { return userId; }
		public void setUserId(String userId) { this.userId = userId; }
	}
}
