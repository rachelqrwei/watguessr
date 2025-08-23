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
	public void handleJoinRankedQueue(Map<String, Object> userInfo) {
        // TODO: Can't join ranked queue if already in a game or another queue.
		String userIdString = (String) userInfo.get("userId");

		if (userIdString == null || userIdString.trim().isEmpty()) {
			return;
		}

		UUID userId = UUID.fromString(userIdString);
		matchmakingService.joinQueue(userId);
	}

	@MessageMapping("/matchmaking/leave")
	public void handleLeaveRankedQueue(Map<String, Object> userInfo) {
		String userIdString = (String) userInfo.get("userId");

		if (userIdString == null || userIdString.trim().isEmpty()) {
			return;
		}

		UUID userId = UUID.fromString(userIdString);
		matchmakingService.leaveQueue(userId);
	}
}
