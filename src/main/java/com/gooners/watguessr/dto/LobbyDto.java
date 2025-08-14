package com.gooners.watguessr.dto;

import java.util.List;
import java.util.UUID;

public class LobbyDto {
	// Player info for frontend
	public record LobbyPlayer(UUID id, String username, boolean ready) {}

	// Lobby update message
	public record LobbyUpdate(List<LobbyPlayer> players) {}

	// Game start info
	public record GameStartInfo(String gameId) {}
}
