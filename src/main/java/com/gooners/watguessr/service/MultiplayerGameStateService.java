package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.MultiplayerGameStateDto;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MultiplayerGameStateService {
	
	private final Map<UUID, MultiplayerGameStateDto> gameStates = new ConcurrentHashMap<>();
	private final SimpMessagingTemplate messagingTemplate;
	private final RoundService roundService;

	public MultiplayerGameStateService(SimpMessagingTemplate messagingTemplate, RoundService roundService) {
		this.messagingTemplate = messagingTemplate;
		this.roundService = roundService;
	}

	public void initializeGame(UUID gameId, List<User> users, Integer roundCount, Integer timer) {
		MultiplayerGameStateDto gameState = new MultiplayerGameStateDto();
		gameState.setGameId(gameId.toString());
		gameState.setCurrentRound(1);
		gameState.setMaxRounds(roundCount);
		gameState.setTimer(timer);
		gameState.setGameStatus("loading");
		
		// Initialize all players with loading status, 0 score, and usernames
		Map<String, PlayerStateDto> players = new HashMap<>();
		for (User user : users) {
			PlayerStateDto playerState = new PlayerStateDto();
			playerState.setScore(0);
			playerState.setStatus("loading");
			playerState.setUsername(user.getUsername());
			players.put(user.getId().toString(), playerState);
		}
		gameState.setPlayers(players);
		
		gameStates.put(gameId, gameState);
		broadcastGameState(gameId);
	}

	public void updatePlayerProgress(UUID gameId, String userId, Integer score, String status) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null && gameState.getPlayers().containsKey(userId)) {
			PlayerStateDto player = gameState.getPlayers().get(userId);
			player.setScore(score);
			player.setStatus(status);
			broadcastGameState(gameId);
			
			// Check if all players completed the round
			if (checkAllPlayersCompleted(gameId)) {
				gameState.setGameStatus("round-complete");
				broadcastGameState(gameId);
			}
		} else {
			System.err.println("❌ Failed to update player progress: gameState=" + (gameState != null) + ", playerExists=" + (gameState != null && gameState.getPlayers().containsKey(userId)));
		}
	}
	
	public void setPlayerStatus(UUID gameId, String userId, String status) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null && gameState.getPlayers().containsKey(userId)) {
			gameState.getPlayers().get(userId).setStatus(status);
			broadcastGameState(gameId);
		}
	}

	public void setPlayerReady(UUID gameId, String userId, boolean ready) {
		System.out.println("🎮 Setting player ready: " + userId + " -> " + ready);
		setPlayerStatus(gameId, userId, ready ? "ready" : "ended");
		
		// Check if all players are ready to advance
		if (checkAllPlayersReady(gameId)) {
			System.out.println("✅ All players ready! Advancing to next round...");
			advanceToNextRound(gameId);
		} else {
			System.out.println("⏳ Waiting for more players to be ready...");
		}
	}

	public void startRound(UUID gameId, UUID sceneId) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null) {
			gameState.setGameStatus("playing");
			gameState.setCurrentSceneId(sceneId.toString());
			
			// Set all players to playing status
			for (PlayerStateDto player : gameState.getPlayers().values()) {
				player.setStatus("playing");
			}
			
			broadcastGameState(gameId);
		}
	}

	public boolean checkAllPlayersCompleted(UUID gameId) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState == null) return false;
		
		return gameState.getPlayers().values().stream()
				.allMatch(player -> "ended".equals(player.getStatus()));
	}

	private boolean checkAllPlayersReady(UUID gameId) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState == null) return false;
		
		return gameState.getPlayers().values().stream()
				.allMatch(player -> "ready".equals(player.getStatus()));
	}

	public void advanceToNextRound(UUID gameId) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null) {
			int currentRound = gameState.getCurrentRound();
			int maxRounds = gameState.getMaxRounds();
			
			if (currentRound < maxRounds) {
				// Move to next round
				gameState.setCurrentRound(currentRound + 1);
				gameState.setGameStatus("loading");
				
				// Reset all players to loading status
				for (PlayerStateDto player : gameState.getPlayers().values()) {
					player.setStatus("loading");
				}
				
				// Create a new round in the database
				try {
					Round newRound = roundService.create(gameId);
					gameState.setCurrentSceneId(newRound.getId().toString());
					
					// Set all players to playing status for the new round
					for (PlayerStateDto player : gameState.getPlayers().values()) {
						player.setStatus("playing");
					}
					gameState.setGameStatus("playing");
					
					// Broadcast round start event with round details
					Map<String, Object> roundStartData = Map.of(
						"gameId", gameId.toString(),
						"roundId", newRound.getId().toString(),
						"roundNumber", currentRound + 1,
						"sceneId", newRound.getScene().getId().toString()
					);
					System.out.println("🚀 Broadcasting round start: " + roundStartData);
					messagingTemplate.convertAndSend("/topic/game/" + gameId + "/round-start", roundStartData);
				} catch (Exception e) {
					System.err.println("Failed to create new round: " + e.getMessage());
				}
				
				broadcastGameState(gameId);
			} else {
				// Game completed
				gameState.setGameStatus("game-complete");
				gameState.setShouldEnd(true);
				
				// Determine the winner
				String winnerId = findWinner(gameState);
				if (winnerId != null) {
					gameState.setFinalWinner(winnerId);
				}
				
				broadcastGameState(gameId);
				
				// Broadcast game completion event
				messagingTemplate.convertAndSend("/topic/game/" + gameId + "/complete", gameState);
			}
		}
	}

	public MultiplayerGameStateDto getGameState(UUID gameId) {
		return gameStates.get(gameId);
	}

	public void removeGame(UUID gameId) {
		gameStates.remove(gameId);
	}

	private void broadcastGameState(UUID gameId) {
		MultiplayerGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null) {
			String topic = "/topic/game/" + gameId + "/state";
			messagingTemplate.convertAndSend(topic, gameState);
		} else {
			System.err.println("❌ Cannot broadcast game state: gameState is null for gameId: " + gameId);
		}
	}

	private String findWinner(MultiplayerGameStateDto gameState) {
		if (gameState.getPlayers().isEmpty()) {
			return null;
		}

		String winnerId = null;
		Integer maxScore = Integer.MIN_VALUE;

		for (Map.Entry<String, PlayerStateDto> entry : gameState.getPlayers().entrySet()) {
			Integer playerScore = entry.getValue().getScore();
			if (playerScore > maxScore) {
				maxScore = playerScore;
				winnerId = entry.getKey();
			}
		}

		return winnerId;
	}

	// Inner class for player state
	public static class PlayerStateDto {
		private Integer score;
		private String status; // "idle", "loading", "playing", "ended", "ready"
		private String username; // Player's username

		public Integer getScore() { return score; }
		public void setScore(Integer score) { this.score = score; }

		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }

		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
	}
}
