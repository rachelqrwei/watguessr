package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.LobbyDto;
import com.gooners.watguessr.dto.MultiplayerGameStateDto;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Service
public class LobbyService {

	private final Map<UUID, List<LobbyPlayerDto>> lobbies = new ConcurrentHashMap<>();
	private final Map<UUID, Map<String, Instant>> lobbyUserLastSeen = new ConcurrentHashMap<>();

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
			
			// Update last seen tracking
			updateLastSeen(lobbyId, user.getId().toString());

			// Update the game entity with current player count
			updateGamePlayerCount(lobbyId, players.size());
			
			// Broadcast to all clients in this lobby
			broadcastLobbyUpdate(lobbyId);
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	public void leaveLobby(UUID lobbyId, User user) {
		System.out.println("User " + user.getUsername() + " (ID: " + user.getId() + ") leaving lobby " + lobbyId);
		
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);
		if (players != null) {
			boolean removed = players.removeIf(p -> p.getUserId().equals(user.getId().toString()));
			System.out.println("Player removal result: " + (removed ? "removed" : "not found"));
			
			// Remove from last seen tracking
			Map<String, Instant> userMap = lobbyUserLastSeen.get(lobbyId);
			if (userMap != null) {
				userMap.remove(user.getId().toString());
			}
			
			// Update the game entity with current player count
			updateGamePlayerCount(lobbyId, players.size());
			
			// Broadcast to all clients in this lobby
			broadcastLobbyUpdate(lobbyId);
			
			// If no users left, remove the lobby from memory and database
			if (players.isEmpty()) {
				System.out.println("Lobby " + lobbyId + " is now empty, removing from memory");
				lobbies.remove(lobbyId);
				lobbyUserLastSeen.remove(lobbyId);
				
				// Remove corresponding Game entity from database
				try {
					gameRepository.deleteById(lobbyId);
				} catch (Exception e) {
					System.err.println("Failed to delete empty lobby from database: " + lobbyId + " - " + e.getMessage());
				}
			}
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		} else {
			System.out.println("Lobby " + lobbyId + " not found in memory");
		}
	}

	public void setPlayerReady(UUID lobbyId, String userId, boolean ready) {
		updateLastSeen(lobbyId, userId);

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
		
		System.out.println("Broadcasting lobby update for lobby " + lobbyId + " with " + players.size() + " players:");
		players.forEach(p -> System.out.println("  - " + p.getUsername() + " (ID: " + p.getUserId() + ", Ready: " + p.isReady() + ")"));

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

	public void updateLastSeen(UUID lobbyId, String userId) {
		lobbyUserLastSeen
				.computeIfAbsent(lobbyId, id -> new ConcurrentHashMap<>())
				.put(userId, Instant.now());
	}

	@Scheduled(fixedRate = 10000) // every 10 seconds
	public void cleanupInactiveUsers() {
		Instant cutoff = Instant.now().minusSeconds(90); // inactive for 90s

		for (var lobbyEntry : lobbyUserLastSeen.entrySet()) {
			UUID lobbyId = lobbyEntry.getKey();
			Map<String, Instant> userMap = lobbyEntry.getValue();

			for (var userEntry : new HashMap<>(userMap).entrySet()) {
				String userId = userEntry.getKey();
				Instant lastSeen = userEntry.getValue();

				if (lastSeen.isBefore(cutoff)) {
					forceLeaveUser(lobbyId, userId);
				}
			}
		}
	}

	private void forceLeaveUser(UUID lobbyId, String userId) {
		// Remove from last seen map
		Map<String, Instant> userMap = lobbyUserLastSeen.get(lobbyId);
		if (userMap != null) userMap.remove(userId);

		// Remove from lobby players
		List<LobbyPlayerDto> players = lobbies.get(lobbyId);
		if (players != null) {
			players.removeIf(p -> p.getUserId().equals(userId));

			// Broadcast updated lobby state
			broadcastLobbyUpdate(lobbyId);

			// If lobby is empty, clean up
			if (players.isEmpty()) {
				lobbies.remove(lobbyId);
				lobbyUserLastSeen.remove(lobbyId);

				try {
					gameRepository.deleteById(lobbyId);
				} catch (Exception e) {
					System.err.println("Failed to delete empty lobby: " + lobbyId + " - " + e.getMessage());
				}
			}
		}

		// Broadcast public lobby update
		broadcastPublicLobbyUpdate();
	}

	public boolean cleanupStaleLobby(String lobbyId) {
		try {
			System.out.println("Cleaning up stale lobby: " + lobbyId);
			UUID lobbyUUID = UUID.fromString(lobbyId);
			
			// Remove from lobbies map
			List<LobbyPlayerDto> removedPlayers = lobbies.remove(lobbyUUID);
			System.out.println("Removed " + (removedPlayers != null ? removedPlayers.size() : 0) + " players from lobby");
			
			// Remove from last seen tracking
			lobbyUserLastSeen.remove(lobbyUUID);
			
			// Remove from database
			try {
				gameRepository.deleteById(lobbyUUID);
				System.out.println("Deleted lobby from database: " + lobbyId);
			} catch (Exception e) {
				System.err.println("Failed to delete lobby from database: " + lobbyId + " - " + e.getMessage());
			}
			
			// Broadcast public lobby update if there were players
			if (removedPlayers != null && !removedPlayers.isEmpty()) {
				broadcastPublicLobbyUpdate();
				System.out.println("Broadcasted public lobby update");
			}
			
			return true;
		} catch (Exception e) {
			System.err.println("Failed to cleanup lobby " + lobbyId + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * Remove a specific user from a lobby (for cleanup purposes)
	 */
	public boolean removeUserFromLobby(String lobbyId, String userId) {
		try {
			System.out.println("Removing user " + userId + " from lobby " + lobbyId);
			UUID lobbyUUID = UUID.fromString(lobbyId);
			List<LobbyPlayerDto> players = lobbies.get(lobbyUUID);
			
			if (players != null) {
				// Remove user from players list
				boolean removed = players.removeIf(p -> p.getUserId().equals(userId));
				System.out.println("User removal result: " + (removed ? "removed" : "not found"));
				
				if (removed) {
					// Remove from last seen tracking
					Map<String, Instant> userMap = lobbyUserLastSeen.get(lobbyUUID);
					if (userMap != null) {
						userMap.remove(userId);
					}
					
					// Broadcast updated lobby state
					broadcastLobbyUpdate(lobbyUUID);
					System.out.println("Broadcasted lobby update");
					
					// If lobby is empty, clean it up completely
					if (players.isEmpty()) {
						System.out.println("Lobby is empty, cleaning up completely");
						cleanupStaleLobby(lobbyId);
					}
					
					// Broadcast public lobby update
					broadcastPublicLobbyUpdate();
					System.out.println("Broadcasted public lobby update");
					
					return true;
				}
			} else {
				System.out.println("Lobby not found: " + lobbyId);
			}
			
			return false;
		} catch (Exception e) {
			System.err.println("Failed to remove user " + userId + " from lobby " + lobbyId + ": " + e.getMessage());
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
