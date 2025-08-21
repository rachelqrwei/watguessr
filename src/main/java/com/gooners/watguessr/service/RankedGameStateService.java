package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.RankedGameStateDto;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RankedGameStateService {
	
	private final Map<UUID, RankedGameStateDto> gameStates = new ConcurrentHashMap<>();
	private final SimpMessagingTemplate messagingTemplate;
	private final RoundService roundService;

	public RankedGameStateService(SimpMessagingTemplate messagingTemplate, RoundService roundService) {
		this.messagingTemplate = messagingTemplate;
		this.roundService = roundService;
	}

	public void initializeGame(UUID gameId, List<User> users, Integer roundCount, Integer timer) {
		RankedGameStateDto gameState = new RankedGameStateDto();
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
		
		// Create the first round for all players to share
		try {
			Round firstRound = roundService.create(gameId);
			gameState.setCurrentSceneId(firstRound.getId().toString());
			System.out.println("🎯 Created first round for ranked game: " + firstRound.getId());
			System.out.println("🎯 First round scene ID: " + firstRound.getScene().getId());
			System.out.println("🎯 Game state currentSceneId set to: " + gameState.getCurrentSceneId());
		} catch (Exception e) {
			System.err.println("Failed to create first round: " + e.getMessage());
		}
		
		gameStates.put(gameId, gameState);
		broadcastGameState(gameId);
		
		// Start the first round immediately so all players get the round ID
		startFirstRound(gameId);
	}

	public void updatePlayerProgress(UUID gameId, String userId, Integer score, String status) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null && gameState.getPlayers().containsKey(userId)) {
			PlayerStateDto player = gameState.getPlayers().get(userId);
			player.setScore(score);
			player.setStatus(status);
			broadcastGameState(gameId);
			
			// Check if all players completed the round
			if (checkAllPlayersEnded(gameId)) {
				gameState.setGameStatus("round-complete");
				broadcastGameState(gameId);
			}
		} else {
			System.err.println("❌ Failed to update player progress: gameState=" + (gameState != null) + ", playerExists=" + (gameState != null && gameState.getPlayers().containsKey(userId)));
		}
	}
	
	public void setPlayerStatus(UUID gameId, String userId, String status) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null && gameState.getPlayers().containsKey(userId)) {
			PlayerStateDto player = gameState.getPlayers().get(userId);
			player.setStatus(status);
			broadcastGameState(gameId);
		} else {
			System.err.println("❌ Failed to set player status: gameState=" + (gameState != null) + ", playerExists=" + (gameState != null && gameState.getPlayers().containsKey(userId)));
		}
	}

	public void setPlayerReady(UUID gameId, String userId, boolean ready) {
		setPlayerStatus(gameId, userId, ready ? "ready" : "ended");
		
		// Check if all players are ready to advance
		if (checkAllPlayersReady(gameId)) {
			advanceToNextRound(gameId);
		}
	}

	public void startRound(UUID gameId, UUID sceneId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null) {
			gameState.setGameStatus("playing");
			// Note: sceneId parameter is actually the round ID, not the scene ID
			// This is for consistency with the advanceToNextRound method
			// TODO: Consider renaming currentSceneId to currentRoundId for clarity
			gameState.setCurrentSceneId(sceneId.toString());
			
			// Set all players to playing status
			for (PlayerStateDto player : gameState.getPlayers().values()) {
				player.setStatus("playing");
			}
			
			broadcastGameState(gameId);
		}
	}

	public void startFirstRound(UUID gameId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null && gameState.getCurrentSceneId() != null) {
			// Start the first round that was created in initializeGame
			gameState.setGameStatus("playing");
			
			// Set all players to playing status
			for (PlayerStateDto player : gameState.getPlayers().values()) {
				player.setStatus("playing");
			}
			
			// Get the round to access its scene
			try {
				Round firstRound = roundService.findById(UUID.fromString(gameState.getCurrentSceneId()));
				System.out.println("🎯 Starting first round: " + firstRound.getId());
				System.out.println("🎯 First round scene: " + firstRound.getScene().getId());
				
				// Broadcast round start event with the first round details
				Map<String, Object> roundStartData = Map.of(
					"gameId", gameId.toString(),
					"roundId", firstRound.getId().toString(),
					"roundNumber", 1,
					"sceneId", firstRound.getScene().getId().toString()
				);
				System.out.println("🚀 Broadcasting first round start: " + roundStartData);
				messagingTemplate.convertAndSend("/topic/ranked-game/" + gameId + "/round-start", roundStartData);
			} catch (Exception e) {
				System.err.println("Failed to get first round details: " + e.getMessage());
			}
			
			broadcastGameState(gameId);
		}
	}

	public boolean checkAllPlayersEnded(UUID gameId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState == null) return false;
		
		return gameState.getPlayers().values().stream()
				.allMatch(player -> "ended".equals(player.getStatus()));
	}

	private boolean checkAllPlayersReady(UUID gameId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState == null) return false;
		
		return gameState.getPlayers().values().stream()
				.allMatch(player -> "ready".equals(player.getStatus()));
	}

	private boolean checkAllPlayersCompleted(UUID gameId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState == null) return false;
		
		return gameState.getPlayers().values().stream()
				.allMatch(player -> "completed".equals(player.getStatus()));
	}

	public void advanceToNextRound(UUID gameId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
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
					// Note: currentSceneId actually stores the round ID, not the scene ID
					Map<String, Object> roundStartData = Map.of(
						"gameId", gameId.toString(),
						"roundId", newRound.getId().toString(),
						"roundNumber", currentRound + 1,
						"sceneId", newRound.getScene().getId().toString()
					);
					System.out.println("🚀 Broadcasting round start: " + roundStartData);
					messagingTemplate.convertAndSend("/topic/ranked-game/" + gameId + "/round-start", roundStartData);
				} catch (Exception e) {
					System.err.println("Failed to create new round: " + e.getMessage());
				}
				
				broadcastGameState(gameId);
			} else {
				// This is the final round - no more rounds to advance to
				System.out.println("🎯 Final round reached. Waiting for all players to complete...");
				gameState.setGameStatus("final-round");
				broadcastGameState(gameId);
			}
		}
	}

	public RankedGameStateDto getGameState(UUID gameId) {
		return gameStates.get(gameId);
	}

	public void removeGame(UUID gameId) {
		gameStates.remove(gameId);
	}

	private void broadcastGameState(UUID gameId) {
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null) {
			String topic = "/topic/ranked-game/" + gameId + "/state";
			System.out.println("<UNK> Broadcasting state: " + gameState.getGameStatus());
			messagingTemplate.convertAndSend(topic, gameState);
		} else {
			System.err.println("❌ Cannot broadcast game state: gameState is null for gameId: " + gameId);
		}
	}

	private String findWinner(RankedGameStateDto gameState) {
		if (gameState.getPlayers().isEmpty()) {
			System.out.println("❌ Cannot find winner: no players in game state");
			return null;
		}

		String winnerId = null;
		Integer maxScore = Integer.MIN_VALUE;

		System.out.println("🔍 Finding winner among players:");
		for (Map.Entry<String, PlayerStateDto> entry : gameState.getPlayers().entrySet()) {
			String playerId = entry.getKey();
			PlayerStateDto player = entry.getValue();
			Integer playerScore = player.getScore();
			System.out.println("  Player " + player.getUsername() + " (ID: " + playerId + "): score=" + playerScore + ", status=" + player.getStatus());
			
			if (playerScore > maxScore) {
				maxScore = playerScore;
				winnerId = playerId;
				System.out.println("  🎯 New leader: " + player.getUsername() + " with score " + playerScore);
			}
		}

		System.out.println("🏆 Final winner determined: " + (winnerId != null ? "Player " + winnerId + " with score " + maxScore : "None"));
		return winnerId;
	}

	public void setPlayerCompleted(UUID gameId, String userId, boolean completed) {
		System.out.println("🎮 Setting player completed: " + userId + " -> " + completed);
		
		if (completed) {
			setPlayerStatus(gameId, userId, "completed");
			
			// Check if all players completed
			if (checkAllPlayersCompleted(gameId)) {
				System.out.println("🏆 All players completed! Ending game...");
				
				// Game completed
				RankedGameStateDto gameState = gameStates.get(gameId);
				if (gameState != null) {
					// Log final player states
					System.out.println("📊 Final player states:");
					gameState.getPlayers().forEach((playerId, player) -> {
						System.out.println("  Player " + player.getUsername() + " (ID: " + playerId + "): score=" + player.getScore() + ", status=" + player.getStatus());
					});
					
					gameState.setGameStatus("game-complete");
					gameState.setShouldEnd(true);

					// Determine the winner for WebSocket state
					System.out.println("🔍 Determining winner for WebSocket state: " + gameId);
					String winnerId = findWinner(gameState);
					if (winnerId != null) {
						gameState.setFinalWinner(winnerId);
						System.out.println("🏆 Winner set in WebSocket state: " + winnerId);
					} else {
						System.err.println("❌ Failed to determine winner for WebSocket state: " + gameId);
					}

					// IMPORTANT: Frontend will call GameService endpoint to resolve game and set database winner
					System.out.println("🎯 Game completion event sent to frontend - frontend will call backend endpoint to resolve game");
					System.out.println("🎯 Winner set in WebSocket state: " + gameState.getFinalWinner());

					System.out.println("📢 Broadcasting final game state with winner: " + gameState.getFinalWinner());
					broadcastGameState(gameId);

					// Broadcast game completion event
					System.out.println("📢 Broadcasting game completion event");
					messagingTemplate.convertAndSend("/topic/ranked-game/" + gameId + "/complete", gameState);
				} else {
					System.err.println("❌ Game state is null for game: " + gameId);
				}
			} else {
				System.out.println("⏳ Waiting for more players to complete...");
				// Log which players still need to complete
				RankedGameStateDto gameState = gameStates.get(gameId);
				if (gameState != null) {
					gameState.getPlayers().entrySet().stream()
						.filter(entry -> !"completed".equals(entry.getValue().getStatus()))
						.forEach(entry -> System.out.println("Player " + entry.getValue().getUsername() + " still needs to complete (status: " + entry.getValue().getStatus() + ")"));
				}
			}
		} else {
			setPlayerStatus(gameId, userId, "ended");
		}
	}

	// Inner class for player state
	public static class PlayerStateDto {
		private Integer score;
		private String status; // "idle", "loading", "playing", "ended", "ready", "completed"
		private String username; // Player's username

		public Integer getScore() { return score; }
		public void setScore(Integer score) { this.score = score; }

		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }

		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
	}
}
