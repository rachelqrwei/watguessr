package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyService {

	private final Map<String, List<User>> lobbies = new ConcurrentHashMap<>();
	private final int MAX_PLAYERS = 8;

	private final SimpMessagingTemplate messagingTemplate;

	@Autowired
	public LobbyService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void joinLobby(String lobbyId, User user) {
		lobbies.computeIfAbsent(lobbyId, k -> new ArrayList<>());
		List<User> users = lobbies.get(lobbyId);

		// Prevent duplicates
		if (users.stream().noneMatch(u -> u.getId().equals(user.getId())) && users.size() < MAX_PLAYERS) {
			users.add(user);
			broadcastLobbyUpdate(lobbyId);
		}
	}

	public void leaveLobby(String lobbyId, User user) {
		List<User> users = lobbies.get(lobbyId);
		if (users != null) {
			users.removeIf(u -> u.getId().equals(user.getId()));
			broadcastLobbyUpdate(lobbyId);
		}
	}

	public List<User> getUsers(String lobbyId) {
		return lobbies.getOrDefault(lobbyId, Collections.emptyList());
	}

	private void broadcastLobbyUpdate(String lobbyId) {
		List<User> users = getUsers(lobbyId);
		messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, new LobbyUpdate(users));
	}

	public void tryStartGame(String lobbyId) {
		List<User> users = getUsers(lobbyId);
		if (users.size() >= 2) { // min 2 players
			messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/start", new GameStart(users));
			lobbies.remove(lobbyId); // reset lobby after game starts
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
