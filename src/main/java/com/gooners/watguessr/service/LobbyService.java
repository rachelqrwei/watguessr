package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.LobbyDto;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Service
public class LobbyService {

	private final Map<UUID, List<LobbyPlayerDto>> lobbies = new ConcurrentHashMap<>();
	private final GameRepository gameRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final MultiplayerGameStateService multiplayerGameStateService;
	private final GameService gameService;
	private final RankedGameStateService rankedGameStateService;

	@Autowired
	public LobbyService(GameRepository gameRepository, SimpMessagingTemplate messagingTemplate, MultiplayerGameStateService multiplayerGameStateService, @Lazy GameService gameService, RankedGameStateService rankedGameStateService) {
		this.gameRepository = gameRepository;
		this.messagingTemplate = messagingTemplate;
		this.multiplayerGameStateService = multiplayerGameStateService;
		this.gameService = gameService;
		this.rankedGameStateService = rankedGameStateService;
	}

	public void joinLobby(UUID lobbyId, User user) {
		lobbies.computeIfAbsent(lobbyId, k -> new ArrayList<>());
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);

		// Get max players from the game
		Integer maxPlayers = gameRepository.findById(lobbyId)
				.map(game -> game.getMaxPlayers())
				.orElse(8);

		// Prevent duplicates and check max players
		if (players.stream().noneMatch(p -> p.getUserId().equals(user.getId().toString())) && players.size() < maxPlayers) {
			LobbyPlayerDto player = new LobbyPlayerDto(user.getId().toString(), user.getUsername(), false);
			players.add(player);

			// Update the game entity with current player count
			updateGamePlayerCount(lobbyId, players.size());
			
			// Broadcast to all clients in this lobby
			broadcastLobbyUpdate(lobbyId);
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	public void leaveLobby(UUID lobbyId, User user) {
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);
		if (players != null) {
			players.removeIf(p -> p.getUserId().equals(user.getId().toString()));
			
			// Update the game entity with current player count
			updateGamePlayerCount(lobbyId, players.size());
			
			// Broadcast to all clients in this lobby
			broadcastLobbyUpdate(lobbyId);
			
			// If no users left, remove the lobby from memory and database
			if (players.isEmpty()) {
				lobbies.remove(lobbyId);
				
				// Remove corresponding Game entity from database
				try {
					gameRepository.deleteById(lobbyId);
				} catch (Exception e) {
					System.err.println("Failed to delete empty lobby from database: " + lobbyId + " - " + e.getMessage());
				}
			}
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	public void setPlayerReady(UUID lobbyId, String userId, boolean ready) {
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);
		if (players != null) {
			Optional<LobbyPlayerDto> playerOpt = players.stream()
					.filter(p -> p.getUserId().equals(userId))
					.findFirst();
			
			if (playerOpt.isPresent()) {
				LobbyPlayerDto player = playerOpt.get();
				player.setReady(ready);

				// Broadcast updated lobby state
				broadcastLobbyUpdate(lobbyId);
			}
		}
	}

	public List<User> getUsers(UUID lobbyId) {
		List<LobbyPlayerDto> players = lobbies.getOrDefault(lobbyId, Collections.emptyList());
		return players.stream()
				.map(p -> {
					User user = new User();
					user.setId(UUID.fromString(p.getUserId()));
					user.setUsername(p.getUsername());
					return user;
				})
				.toList();
	}

	public int getActiveLobbyCount() {
		return lobbies.size();
	}

	private void updateGamePlayerCount(UUID lobbyId, int playerCount) {
		try {
			Game game = gameRepository.findById(lobbyId).orElse(null);
			if (game != null) {
				// We could add a currentPlayers field to the Game entity if needed
				// For now, we'll just ensure the lobby is properly tracked
			}
		} catch (Exception e) {
			// Log error but don't fail the operation
			System.err.println("Failed to update game player count: " + e.getMessage());
		}
	}

	private void broadcastLobbyUpdate(UUID lobbyId) {
		List<LobbyPlayerDto> players = lobbies.getOrDefault(lobbyId, Collections.emptyList());

		messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, new LobbyUpdate(players));
	}

	private void broadcastPublicLobbyUpdate() {
		// Broadcast to all clients subscribed to public lobby updates
		messagingTemplate.convertAndSend("/topic/lobbies/public", "update");
	}

	public UUID tryStartGame(UUID lobbyId, String gameMode, Integer roundCount, Integer timer) {
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);
		if (players.size() >= 2 && areAllPlayersReady(lobbyId)) { // min 2 players and all ready
			// Get game details
			UUID gameId = gameService.createMultiplayerGame(roundCount, timer);
			if (gameId != null) {
				// Convert LobbyPlayerDto to User for game initialization
				List<User> users = players.stream()
						.map(p -> {
							User user = new User();
							user.setId(UUID.fromString(p.getUserId()));
							user.setUsername(p.getUsername());
							return user;
						})
						.toList();

				if (gameMode.equals("multiplayer")) {
					// Initialize multiplayer game state with full user objects
					multiplayerGameStateService.initializeGame(
							gameId,
							users,
							roundCount,
							timer
					);
				}
				else if (gameMode.equals("ranked")) {
					rankedGameStateService.initializeGame(
							gameId,
							users,
							roundCount,
							timer
					);
				}
			} else {
				System.err.println("❌ Failed to create game - gameId is null");
			}
			
			// Convert players to users for GameStart message
			List<User> usersForMessage = players.stream()
					.map(p -> {
						User user = new User();
						user.setId(UUID.fromString(p.getUserId()));
						user.setUsername(p.getUsername());
						return user;
					})
					.toList();
			
			messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/start", new GameStart(gameId.toString(), usersForMessage));
			
			// Remove lobby from memory and database after game starts
			lobbies.remove(lobbyId);
			
			// Remove corresponding Game entity from database
			try {
				gameRepository.deleteById(lobbyId);
			} catch (Exception e) {
				System.err.println("Failed to delete lobby from database: " + lobbyId + " - " + e.getMessage());
			}
			
			return gameId;
		}
		return null;
	}

	private boolean areAllPlayersReady(UUID lobbyId) {
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);
		return players != null && players.size() >= 2 && 
			   players.stream().allMatch(LobbyPlayerDto::isReady);
	}

	/**
	 * Deletes all empty lobbies
	 * @return number of lobbies deleted
	 */
	public int cleanupEmptyLobbies() {
		int before = lobbies.size();
		List<UUID> emptyLobbyIds = new ArrayList<>();

		// Collect IDs of empty lobbies
		lobbies.entrySet().removeIf(entry -> {
			if (entry.getValue().isEmpty()) {
				emptyLobbyIds.add(entry.getKey());
				return true;
			}
			return false;
		});
		
		// Remove corresponding Game entities from database
		for (UUID lobbyId : emptyLobbyIds) {
			try {
				gameRepository.deleteById(lobbyId);
			} catch (Exception e) {
				System.err.println("Failed to delete empty lobby from database: " + lobbyId + " - " + e.getMessage());
			}
		}
		
		int deleted = before - lobbies.size();
		return deleted;
	}

	public boolean cleanupStaleLobby(String lobbyId) {
		try {
			lobbies.remove(lobbyId);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to cleanup lobby " + lobbyId + ": " + e.getMessage());
			return false;
		}
	}

	// Inner classes for WebSocket messaging
	public static class LobbyUpdate {
		private List<LobbyPlayerDto> players;

		public LobbyUpdate(List<LobbyPlayerDto> players) {
			this.players = players;
		}

		public List<LobbyPlayerDto> getPlayers() {
			return players;
		}

		public void setPlayers(List<LobbyPlayerDto> players) {
			this.players = players;
		}
	}

	public static class GameStart {
		private String gameId;
		private List<User> users;

		public GameStart(String gameId, List<User> users) {
			this.gameId = gameId;
			this.users = users;
		}

		public String getGameId() {
			return gameId;
		}

		public void setGameId(String gameId) {
			this.gameId = gameId;
		}

		public List<User> getUsers() {
			return users;
		}

		public void setUsers(List<User> users) {
			this.users = users;
		}
	}

	public static class LobbyPlayerDto {
		private String userId;
		private String username;
		private boolean ready;

		public LobbyPlayerDto(String userId, String username, boolean ready) {
			this.userId = userId;
			this.username = username;
			this.ready = ready;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public boolean isReady() {
			return ready;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
		}
	}
}
