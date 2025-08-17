package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GameRepository;
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
			
			// If no users left, remove the lobby
			if (users.isEmpty()) {
				lobbies.remove(lobbyId);
			}
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	public List<User> getUsers(UUID lobbyId) {
		return lobbies.getOrDefault(lobbyId, Collections.emptyList());
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

	public void tryStartGame(UUID lobbyId, Integer roundCount, Integer timer) {
		List<User> users = getUsers(lobbyId);
		if (users.size() >= 2) { // min 2 players
			// Get game details
			UUID gameId = gameService.createMultiplayerGame(roundCount, timer);
			if (gameId != null) {
							// Initialize multiplayer game state with full user objects
			multiplayerGameStateService.initializeGame(
				lobbyId, 
				users,
				roundCount,
				timer
			);
			}
			
			messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/start", new GameStart(users));
			lobbies.remove(lobbyId); // reset lobby after game starts
			
			// Also broadcast to public lobby list subscribers
			broadcastPublicLobbyUpdate();
		}
	}

	// DTO classes for sending to WebSocket clients
	public static class LobbyUpdate {
		private List<User> users;
		public LobbyUpdate(List<User> users) { this.users = users; }
		public List<User> getUsers() { return users; }
		public void setUsers(List<User> users) { this.users = users; }
	}

	public static class GameStart {
		private List<User> users;
		public GameStart(List<User> users) { this.users = users; }
		public List<User> getUsers() { return users; }
		public void setUsers(List<User> users) { this.users = users; }
	}
}
