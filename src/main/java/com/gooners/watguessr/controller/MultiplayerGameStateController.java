package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.MultiplayerGameStateDto;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.HeartbeatMessage;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.MultiplayerGameStateService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MultiplayerGameStateController {
	private final Map<UUID, List<MultiplayerGameStateDto>> multiplayerGameStates = new ConcurrentHashMap<>();
	private final MultiplayerGameStateService multiplayerGameStateService;

	public MultiplayerGameStateController(MultiplayerGameStateService multiplayerGameStateService) {
		this.multiplayerGameStateService = multiplayerGameStateService;
	}

	@MessageMapping("/multiplayer-game/update-progress")
	public void updatePlayerProgress(@Payload UpdatePlayerProgressRequest updatePlayerProgressRequest) {
        // TODO: should not be able to update your own.
        UUID gameId = UUID.fromString(updatePlayerProgressRequest.getGameId());
		String userId = updatePlayerProgressRequest.getUserId();
		Integer score = updatePlayerProgressRequest.getScore();
		String status = updatePlayerProgressRequest.getStatus();

		multiplayerGameStateService.updatePlayerProgress(gameId, userId, score, status);
	}

	@MessageMapping("/multiplayer-game/ready")
	public void setPlayerReady(@Payload PlayerStatusUpdateRequest request) {
		UUID gameId = UUID.fromString(request.getGameId());
		String userId = request.getUserId();
		multiplayerGameStateService.setPlayerReady(gameId, userId, true);
	}

	@MessageMapping("/multiplayer-game/completed")
	public void setPlayerCompleted(@Payload PlayerStatusUpdateRequest request) {
        // TODO: should not be able to update your own.
		UUID gameId = UUID.fromString(request.getGameId());
		String userId = request.getUserId();
		multiplayerGameStateService.setPlayerCompleted(gameId, userId, true);
	}

	@MessageMapping("/multiplayer-game/start-round")
	public void startRound(@Payload StartRoundRequest request) {
        // TODO: should not be able to start round yourself.
		UUID gameId = UUID.fromString(request.getGameId());
		UUID sceneId = UUID.fromString(request.getSceneId());
		multiplayerGameStateService.startRound(gameId, sceneId);
	}

	@MessageMapping("/multiplayer-game/heartbeat")
	public void handleHeartbeatForMultiplayerGame(HeartbeatMessage heartbeat, Principal principal) {
		multiplayerGameStateService.updateLastSeen(UUID.fromString(heartbeat.getGameId()), principal.getName());
	}

	/**
	 * Request DTOs
	 */

	public static class UpdatePlayerProgressRequest {
		private String gameId;
		private String userId;
		private Integer score;
		private String status;

		public String getGameId() { return gameId; }
		public void setGameId(String gameId) { this.gameId = gameId; }

		public String getUserId() { return userId; }
		public void setUserId(String userId) { this.userId = userId; }

		public Integer getScore() { return score; }
		public void setScore(Integer score) { this.score = score; }

		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
	}

	public static class PlayerStatusUpdateRequest {
		private String gameId;
		private String userId;

		public String getGameId() { return gameId; }
		public void setGameId(String gameId) { this.gameId = gameId; }

		public String getUserId() { return userId; }
		public void setUserId(String userId) { this.userId = userId; }
	}

	public static class StartRoundRequest {
		private String gameId;
		private String sceneId;

		public String getGameId() { return gameId; }
		public void setGameId(String gameId) { this.gameId = gameId; }

		public String getSceneId() { return sceneId; }
		public void setSceneId(String sceneId) { this.sceneId = sceneId; }
	}
}
