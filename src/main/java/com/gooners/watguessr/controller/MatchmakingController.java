package com.gooners.watguessr.controller;

import com.gooners.watguessr.service.MatchmakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

	private final MatchmakingService matchmakingService;
    
    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

	@MessageMapping("/matchmaking/join")
	@SendTo("/topic/matchmaking/updates")
	public Map<String, Object> handleJoinRankedQueue(Map<String, Object> userInfo) {
		String userIdString = (String) userInfo.get("userId");

		if (userIdString == null || userIdString.trim().isEmpty()) {
			return Map.of(
				"type", "error",
				"message", "Invalid user ID",
				"timestamp", java.time.Instant.now().toString()
			);
		}

		try {
			UUID userId = UUID.fromString(userIdString);
			matchmakingService.joinQueue(userId);
			
			System.out.println("🎯 User " + userId + " joined ranked queue");
			
			return Map.of(
				"type", "queue_joined",
				"message", "Successfully joined ranked queue",
				"userId", userIdString,
				"timestamp", java.time.Instant.now().toString()
			);
		} catch (Exception e) {
			System.err.println("❌ Error joining queue: " + e.getMessage());
			return Map.of(
				"type", "error",
				"message", "Failed to join queue: " + e.getMessage(),
				"timestamp", java.time.Instant.now().toString()
			);
		}
	}

	@MessageMapping("/matchmaking/leave")
	public void handleLeaveRankedQueue(Map<String, Object> userInfo) {

	}

	// WebSocket message handlers for testing
	@MessageMapping("/matchmaking/test")
	@SendTo("/topic/matchmaking/updates")
	public Map<String, Object> handleTestMessage(Map<String, Object> testMessage) {
		System.out.println("🧪 Received test message: " + testMessage);

		return Map.of(
				"type", "test_response",
				"message", "Hello from matchmaking server!",
				"timestamp", java.time.Instant.now().toString(),
				"receivedMessage", testMessage
		);
	}
}