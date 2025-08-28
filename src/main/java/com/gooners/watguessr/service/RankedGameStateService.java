package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.RankedGameStateDto;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RankedGameStateService {
	
	private final Map<UUID, RankedGameStateDto> gameStates = new ConcurrentHashMap<>();
	private final Map<UUID, Map<String, Instant>> gameUserLastSeen = new ConcurrentHashMap<>();
	private final SimpMessagingTemplate messagingTemplate;
	private final RoundService roundService;
	private final UserService userService;

	public RankedGameStateService(SimpMessagingTemplate messagingTemplate, RoundService roundService, UserService userService) {
		this.messagingTemplate = messagingTemplate;
		this.roundService = roundService;
		this.userService = userService;
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
		Map<String, Instant> userLastSeen = new ConcurrentHashMap<>();

		for (User user : users) {
			PlayerStateDto playerState = new PlayerStateDto();
			playerState.setScore(0);
			playerState.setStatus("loading");
			playerState.setUsername(user.getUsername());
			players.put(user.getId().toString(), playerState);

			userLastSeen.put(user.getId().toString(), Instant.now());
		}
		gameState.setPlayers(players);
		
		// Create the first round for all players to share
		try {
			Round firstRound = roundService.create(gameId);
			gameState.setCurrentSceneId(firstRound.getId().toString());
		} catch (Exception e) {
			System.err.println("Failed to create first round: " + e.getMessage());
		}
		
		gameStates.put(gameId, gameState);
		gameUserLastSeen.put(gameId, userLastSeen);
		broadcastGameState(gameId);
		
		// Start the first round immediately so all players get the round ID
		startFirstRound(gameId);
	}

	public void updatePlayerProgress(UUID gameId, String userId, Integer score, String status) {
		updateLastSeen(gameId, userId);

		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null && gameState.getPlayers().containsKey(userId)) {
			PlayerStateDto player = gameState.getPlayers().get(userId);
			player.setScore(score);
			player.setStatus(status);
			broadcastGameState(gameId);
		} else {
			System.err.println("❌ Failed to update player progress: gameState=" + (gameState != null) + ", playerExists=" + (gameState != null && gameState.getPlayers().containsKey(userId)));
		}
	}
	
	public void setPlayerStatus(UUID gameId, String userId, String status) {
		updateLastSeen(gameId, userId);

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
		updateLastSeen(gameId, userId);

		setPlayerStatus(gameId, userId, ready ? "ready" : "ended");
		
		if (ready) {
			if (checkAllPlayersReady(gameId)) {
				advanceToNextRound(gameId);
			}
		} else {
			if (checkAllPlayersEnded(gameId)) {
				RankedGameStateDto gameState = gameStates.get(gameId);
				if (gameState != null) {
					gameState.setGameStatus("round-complete");
					broadcastGameState(gameId);
				}
			}
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
				
				// Broadcast round start event with the first round details
				Map<String, Object> roundStartData = Map.of(
					"gameId", gameId.toString(),
					"roundId", firstRound.getId().toString(),
					"roundNumber", 1,
					"sceneId", firstRound.getScene().getId().toString()
				);
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
					messagingTemplate.convertAndSend("/topic/ranked-game/" + gameId + "/round-start", roundStartData);
				} catch (Exception e) {
					System.err.println("Failed to create new round: " + e.getMessage());
				}
				
				broadcastGameState(gameId);
			} else {
				// This is the final round - no more rounds to advance to
				// Don't remove the game state yet - wait for all players to complete
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
			messagingTemplate.convertAndSend(topic, gameState);
		} else {
			System.err.println("❌ Cannot broadcast game state: gameState is null for gameId: " + gameId);
		}
	}

	private String findWinner(RankedGameStateDto gameState) {
		if (gameState.getPlayers().isEmpty()) {
			return null;
		}

		String winnerId = null;
		Integer maxScore = Integer.MIN_VALUE;

		for (Map.Entry<String, PlayerStateDto> entry : gameState.getPlayers().entrySet()) {
			String playerId = entry.getKey();
			PlayerStateDto player = entry.getValue();
			Integer playerScore = player.getScore();

			if (playerScore > maxScore) {
				maxScore = playerScore;
				winnerId = playerId;
			}
		}

		return winnerId;
	}

	public void setPlayerCompleted(UUID gameId, String userId, boolean completed) {
		if (completed) {
			setPlayerStatus(gameId, userId, "completed");
			
			// Check if all players completed
			if (checkAllPlayersCompleted(gameId)) {
				// Game completed
				RankedGameStateDto gameState = gameStates.get(gameId);
				if (gameState != null) {
					// Log final player states
					gameState.getPlayers().forEach((playerId, player) -> {
					});
					
					gameState.setGameStatus("game-complete");
					gameState.setShouldEnd(true);

					// Determine the winner for WebSocket state
					String winnerId = findWinner(gameState);
					if (winnerId != null) {
						gameState.setFinalWinner(winnerId);
					} else {
						System.err.println("❌ Failed to determine winner for WebSocket state: " + gameId);
					}

					broadcastGameState(gameId);

					// Broadcast game completion event
					messagingTemplate.convertAndSend("/topic/ranked-game/" + gameId + "/complete", gameState);
					
					// Now that all players have completed, remove the game state
					removeGame(gameId);
				} else {
					System.err.println("❌ Game state is null for game: " + gameId);
				}
			}
		} else {
			setPlayerStatus(gameId, userId, "ended");
		}
	}

	public void updateLastSeen(UUID gameId, String userId) {
		gameUserLastSeen
				.computeIfAbsent(gameId, id -> new ConcurrentHashMap<>())
				.put(userId, Instant.now());
	}

	@Scheduled(fixedRate = 10000) // every 30 seconds
	public void cleanupInactiveUsers() {
		if (gameStates.isEmpty()) {
			return;
		}

		Instant cutoff = Instant.now().minusSeconds(30); // 20 seconds

		for (var gameEntry : gameUserLastSeen.entrySet()) {
			UUID gameId = gameEntry.getKey();
			Map<String, Instant> userMap = gameEntry.getValue();

			for (var userEntry : new HashMap<>(userMap).entrySet()) {
				String userId = userEntry.getKey();
				Instant lastSeen = userEntry.getValue();

				if (lastSeen.isBefore(cutoff)) {
					forceLeaveUser(gameId, userId);
					userMap.remove(userId);
				}
			}
		}
	}

	public void forceLeaveUser(UUID gameId, String userId) {
		// Your existing logic to remove user from game
		gameUserLastSeen.remove(gameId, userId);

		// Remove user from game state players
		RankedGameStateDto gameState = gameStates.get(gameId);
		if (gameState != null) {
			Map<String, PlayerStateDto> players = gameState.getPlayers();
			if (players != null) {
				players.remove(userId);
				
				// If no players left in the game, remove the game state
				if (players.isEmpty()) {
					removeGame(gameId);
					return; // Don't broadcast since game is removed
				}
				else {
					// Give the remaining player +2 ELO bonus for being abandoned
					String remainingUserId = players.keySet().iterator().next(); // get the only key
					User leftAloneUser = userService.findById(UUID.fromString(remainingUserId));
					if (leftAloneUser != null) {
						Integer updatedElo = leftAloneUser.getElo() + 2;
						leftAloneUser.setElo(updatedElo);
						userService.update(leftAloneUser);
					}
				}
			}
		}

		// Reduce ELO by 10 points as a penalty for disconnecting
		User disconnectingUser = userService.findById(UUID.fromString(userId));
		if (disconnectingUser != null) {
			Integer updatedElo = disconnectingUser.getElo() - 10;
			disconnectingUser.setElo(Math.max(0, updatedElo)); // Ensure ELO doesn't go below 0
			userService.update(disconnectingUser);
		}

		broadcastGameState(gameId); // notify all clients
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
