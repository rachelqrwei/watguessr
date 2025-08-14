package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.LobbyService;
import com.gooners.watguessr.service.LobbyService.GameStart;
import com.gooners.watguessr.service.LobbyService.LobbyUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyController {

	private final LobbyService lobbyService;

	@Autowired
	public LobbyController(LobbyService lobbyService) {
		this.lobbyService = lobbyService;
	}

	/**
	 * User joins a lobby
	 */
	@MessageMapping("/lobby/join")
	public void joinLobby(@Payload JoinLobbyRequest request) {
		lobbyService.joinLobby(request.getLobbyId(), request.getUser());
	}

	/**
	 * User leaves a lobby
	 */
	@MessageMapping("/lobby/leave")
	public void leaveLobby(@Payload JoinLobbyRequest request) {
		lobbyService.leaveLobby(request.getLobbyId(), request.getUser());
	}

	/**
	 * Attempt to start the game manually (optional)
	 */
	@MessageMapping("/lobby/start")
	public void startGame(@Payload StartGameRequest request) {
		lobbyService.tryStartGame(request.getLobbyId());
	}

	/**
	 * Request DTOs
	 */
	public static class JoinLobbyRequest {
		private String lobbyId;
		private User user;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

		public User getUser() { return user; }
		public void setUser(User user) { this.user = user; }
	}

	public static class StartGameRequest {
		private String lobbyId;

		public String getLobbyId() { return lobbyId; }
		public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }
	}
}
