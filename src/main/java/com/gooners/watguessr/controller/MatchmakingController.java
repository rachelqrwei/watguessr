package com.gooners.watguessr.controller;

import com.gooners.watguessr.service.MatchmakingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

	private final MatchmakingService matchmakingService;

	@PostMapping("/join")
	public ResponseEntity<Map<String, Object>> joinQueue(
			@RequestParam UUID userId,
			@RequestParam String gameMode,
			@RequestParam(required = false, defaultValue = "5") Integer roundCount,
			@RequestParam(required = false, defaultValue = "30") Integer timer) {

		try {
			UUID queueId = matchmakingService.joinQueue(userId, gameMode, roundCount, timer);

			return ResponseEntity.ok(Map.of(
					"success", true,
					"queueId", queueId.toString(),
					"message", "Successfully joined matchmaking queue"
			));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of(
					"success", false,
					"error", e.getMessage()
			));
		}
	}

	@PostMapping("/leave")
	public ResponseEntity<Map<String, Object>> leaveQueue(@RequestParam UUID userId) {
		try {
			matchmakingService.leaveQueue(userId);

			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Successfully left matchmaking queue"
			));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of(
					"success", false,
					"error", e.getMessage()
			));
		}
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> getQueueStatus(@RequestParam UUID userId) {
		// Implementation to get current queue status
		return ResponseEntity.ok(matchmakingService.getQueueStatus(userId));
	}
}