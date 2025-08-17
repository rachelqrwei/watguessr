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

@Service
public class LobbyService {

	private final Map<UUID, List<User>> lobbies = new ConcurrentHashMap<>();
	private final GameRepository gameRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final MultiplayerGameStateService multiplayerGameStateService;
	private final GameService gameService;

	@Autowired
	public LobbyService(GameRepository gameRepository, SimpMessagingTemplate messagingTemplate, MultiplayerGameStateService multiplayerGameStateService, @Lazy GameService gameService) {
		this.gameRepository = gameRepository;
		this.messagingTemplate = messagingTemplate;
		this.multiplayerGameStateService = multiplayerGameStateService;
		this.gameService = gameService;
	}

	public void joinLobby(UUID lobbyId, User user) {
		lobbies.computeIfAbsent(lobbyId, k -> new ArrayList<>());
		List<User> users = lobbies.get(lobbyId);

		// Get max players from the game
		Integer maxPlayers = gameRepository.findById(lobbyId)
				.map(game -> game.getMaxPlayers())
				.orElse(8);

		// Prevent duplicates and check max players
		if (users.stream().noneMatch(u -> u.getId().equals(user.getId())) && users.size() < maxPlayers) {
			users.add(user);
			
			// Update the game entity with current player count
			updateGamePlayerCount(lobbyId, users.size());
			
			// Broadcast to all clients in this lobby
			broadcastLobbyUpdate(lobbyId);
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	public void leaveLobby(UUID lobbyId, User user) {
		List<User> users = lobbies.get(lobbyId);
		if (users != null) {
			users.removeIf(u -> u.getId().equals(user.getId()));
			
			// Update the game entity with current player count
			updateGamePlayerCount(lobbyId, users.size());
			
			// Broadcast to all clients in this lobby
			broadcastLobbyUpdate(lobbyId);
			
			// If no users left, remove the lobby from memory and database
			if (users.isEmpty()) {
				lobbies.remove(lobbyId);
				
				// Remove corresponding Game entity from database
				try {
					gameRepository.deleteById(lobbyId);
					System.out.println("Deleted empty lobby from database after user left: " + lobbyId);
				} catch (Exception e) {
					System.err.println("Failed to delete empty lobby from database: " + lobbyId + " - " + e.getMessage());
				}
			}
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	public List<User> getUsers(UUID lobbyId) {
		return lobbies.getOrDefault(lobbyId, Collections.emptyList());
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
		List<User> users = getUsers(lobbyId);
		messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, new LobbyUpdate(users));
	}

	private void broadcastPublicLobbyUpdate() {
		// Broadcast to all clients subscribed to public lobby updates
		messagingTemplate.convertAndSend("/topic/lobbies/public", "update");
	}

	public UUID tryStartGame(UUID lobbyId, Integer roundCount, Integer timer) {
		List<User> users = getUsers(lobbyId);
		if (users.size() >= 2) { // min 2 players
			// Get game details
			UUID gameId = gameService.createMultiplayerGame(roundCount, timer);
			if (gameId != null) {
				// Initialize multiplayer game state with full user objects
				multiplayerGameStateService.initializeGame(
					gameId,
					users,
					roundCount,
					timer
				);
			} else {
				System.err.println("❌ Failed to create game - gameId is null");
			}
			
			messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/start", new GameStart(gameId.toString(), users));
			
			// Remove lobby from memory and database after game starts
			lobbies.remove(lobbyId);
			
			// Remove corresponding Game entity from database
			try {
				gameRepository.deleteById(lobbyId);
				System.out.println("Deleted lobby from database after game started: " + lobbyId);
			} catch (Exception e) {
				System.err.println("Failed to delete lobby from database: " + lobbyId + " - " + e.getMessage());
			}
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();

			return gameId;
		} else {
			return null;
		}
	}

	/**
	 * Deletes all empty lobbies
	 * @return number of lobbies deleted
	 */
	public void removeLobby(UUID lobbyId) {
		lobbies.remove(lobbyId);
	}

	/**
	 * Delete all empty lobbies
	 */
	public int cleanupEmptyLobbies() {
		int before = lobbies.size();
		List<UUID> emptyLobbyIds = new ArrayList<>();
		
		System.out.println("Starting scheduled cleanup. Current lobbies in memory: " + before);
		
		// Collect IDs of empty lobbies
		lobbies.entrySet().removeIf(entry -> {
			if (entry.getValue().isEmpty()) {
				emptyLobbyIds.add(entry.getKey());
				System.out.println("Found empty lobby in memory: " + entry.getKey());
				return true;
			}
			return false;
		});
		
		// Remove corresponding Game entities from database
		for (UUID lobbyId : emptyLobbyIds) {
			try {
				gameRepository.deleteById(lobbyId);
				System.out.println("Deleted empty lobby from database: " + lobbyId);
			} catch (Exception e) {
				System.err.println("Failed to delete empty lobby from database: " + lobbyId + " - " + e.getMessage());
			}
		}
		
		int deleted = before - lobbies.size();
		System.out.println("Scheduled cleanup completed. Deleted " + deleted + " empty lobbies. Remaining: " + lobbies.size());
		return deleted;
	}

	// DTO classes for sending to WebSocket clients
	public static class LobbyUpdate {
		private List<User> users;
		public LobbyUpdate(List<User> users) { this.users = users; }
		public List<User> getUsers() { return this.users; }
		public void setUsers(List<User> users) { this.users = users; }
	}

	public static class GameStart {
		private String gameId;
		private List<User> users;
		
		public GameStart(String gameId, List<User> users) { 
			this.gameId = gameId; 
			this.users = users; 
		}
		
		public String getGameId() { return this.gameId; }
		public void setGameId(String gameId) { this.gameId = gameId; }
		
		public List<User> getUsers() { return this.users; }
		public void setUsers(List<User> users) { this.users = users; }
	}
}
