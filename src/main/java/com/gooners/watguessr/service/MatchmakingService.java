package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.MatchmakingQueue;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.MatchmakingQueueRepository;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class MatchmakingService {

	private final MatchmakingQueueRepository queueRepository;
	private final GameService gameService;
	private final UserService userService;
	private final SimpMessagingTemplate messagingTemplate;
	private final MultiplayerGameStateService multiplayerGameStateService;
	private final RankedGameStateService rankedGameStateService;

	public MatchmakingService(MatchmakingQueueRepository queueRepository,
							  GameService gameService,
							  UserService userService,
							  SimpMessagingTemplate messagingTemplate, 
							  MultiplayerGameStateService multiplayerGameStateService,
							  RankedGameStateService rankedGameStateService) {
		this.queueRepository = queueRepository;
		this.gameService = gameService;
		this.userService = userService;
		this.messagingTemplate = messagingTemplate;
		this.multiplayerGameStateService = multiplayerGameStateService;
		this.rankedGameStateService = rankedGameStateService;
	}

	// Constants for matchmaking
	private static final int MAX_PLAYERS_RANKED = 2;
	private static final int ELO_RANGE_INITIAL = 100;
	private static final int ELO_RANGE_MAX = 500;
	private static final long QUEUE_TIMEOUT_MINUTES = 5;

	// Users currently waiting, so the scheduled sweep can skip the database when
	// nobody is queued. Neon only scales to zero after 5 minutes without a query,
	// so an unconditional 30s poll would keep the compute billing 24/7.
	private final Set<UUID> waitingUsers = ConcurrentHashMap.newKeySet();

	/**
	 * Add a user to the matchmaking queue
	 */
	public UUID joinQueue(UUID userId) {
		// Remove user from any existing queues first
		leaveQueue(userId);

		User user = userService.findById(userId);

		MatchmakingQueue queueEntry = new MatchmakingQueue();
		queueEntry.setUser(user);
		queueEntry.setCreatedAt(OffsetDateTime.now());
		queueEntry.setStatus("waiting");

		MatchmakingQueue saved = queueRepository.save(queueEntry);
		waitingUsers.add(userId);

		// Immediately try to find a match
		findRankedMatch(saved);

		return saved.getId();
	}

	/**
	 * Remove a user from all queues
	 */
	public void leaveQueue(UUID userId) {
		waitingUsers.remove(userId);

		List<MatchmakingQueue> userQueues = queueRepository.findByUserIdAndStatus(userId, "waiting");
		for (MatchmakingQueue queue : userQueues) {
			queue.setStatus("expired");
			queueRepository.save(queue);
		}

		// Notify user they left the queue
		Map<String, Object> update = createMatchmakingUpdate("left_queue", null);
		String topic = "/topic/matchmaking/" + userId;
		messagingTemplate.convertAndSend(topic, update);
	}

	private void findRankedMatch(MatchmakingQueue newEntry) {
		User newUser = newEntry.getUser();
		int userElo = newUser.getElo();

		// Dynamic ELO range based on wait time
		long waitTimeMinutes = Duration.between(newEntry.getCreatedAt(), OffsetDateTime.now()).toMinutes();
		int eloRange = Math.min(ELO_RANGE_INITIAL + (int)(waitTimeMinutes * 100), ELO_RANGE_MAX);

		List<MatchmakingQueue> compatiblePlayers = queueRepository.findCompatibleRankedPlayers(
				userElo - eloRange,
				userElo + eloRange,
				newEntry.getCreatedAt().minusMinutes(QUEUE_TIMEOUT_MINUTES)
		);

		// Remove the current user from compatible players to prevent self-matching
		compatiblePlayers.removeIf(player -> player.getUser().getId().equals(newUser.getId()));

		if (!compatiblePlayers.isEmpty()) {
			// Found a match! Take the closest ELO player
			MatchmakingQueue opponent = findClosestEloPlayer(compatiblePlayers, userElo);
			createRankedSession(Arrays.asList(newEntry, opponent));
		} else {
			notifyUserInQueue(newEntry.getUser().getId());
		}
	}

	private MatchmakingQueue findClosestEloPlayer(List<MatchmakingQueue> compatiblePlayers, int targetElo) {
		return compatiblePlayers.stream()
				.min(Comparator.comparingInt(player -> Math.abs(player.getUser().getElo() - targetElo)))
				.orElse(null);
	}

	private void createRankedSession(List<MatchmakingQueue> players) {
		// Calculate average ELO
		int averageElo = (int) players.stream()
				.mapToInt(p -> p.getUser().getElo())
				.average()
				.orElse(150);

		// Create ranked game
		UUID gameId = gameService.createRankedGame(averageElo);

		// Update queue entries
		for (MatchmakingQueue player : players) {
			player.setStatus("matched");
			queueRepository.save(player);
			waitingUsers.remove(player.getUser().getId());

			notifyPlayerMatched(player.getUser().getId(), gameId, players);
		}
	}

	// Helper methods...
	private void notifyUserInQueue(UUID userId) {
		Map<String, Object> update = createMatchmakingUpdate("in_queue", null);
		String topic = "/topic/matchmaking/" + userId;
		messagingTemplate.convertAndSend(topic, update);
	}

	private void notifyPlayerMatched(UUID userId, UUID gameId, List<MatchmakingQueue> allPlayers) {
		List<Map<String, Object>> playerList = allPlayers.stream()
				.map(p -> {
					Map<String, Object> playerMap = new HashMap<>();
					playerMap.put("id", p.getUser().getId().toString());
					playerMap.put("username", p.getUser().getUsername());
					playerMap.put("elo", p.getUser().getElo());
					return playerMap;
				})
				.collect(Collectors.toList());

		Map<String, Object> data = new HashMap<>();
		data.put("gameId", gameId.toString());
		data.put("players", playerList);

		Map<String, Object> update = createMatchmakingUpdate("match_found", data);
		String topic = "/topic/matchmaking/" + userId;
		messagingTemplate.convertAndSend(topic, update);

		// Convert LobbyPlayerDto to User for game initialization
		List<User> users = allPlayers.stream()
				.map(p -> {
					User user = p.getUser();
					return user;
				})
				.toList();

		// Use rankedGameStateService for ranked games instead of multiplayerGameStateService
		rankedGameStateService.initializeGame(gameId, users, 5, 30);
	}

	private Map<String, Object> createMatchmakingUpdate(String type, Object data) {
		Map<String, Object> update = new HashMap<>();
		update.put("type", type);
		update.put("timestamp", OffsetDateTime.now().toString());
		if (data != null) {
			update.put("data", data);
		}
		return update;
	}

	/**
	 * Scheduled task to clean up expired queue entries and expand ELO ranges
	 */
	@Scheduled(fixedRate = 30000) // Run every 30 seconds
	public void processMatchmakingQueue() {
		if (waitingUsers.isEmpty()) {
			return;
		}

		// Clean up expired entries
		OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(QUEUE_TIMEOUT_MINUTES);
		List<MatchmakingQueue> expiredEntries = queueRepository.findExpiredEntries(cutoff);

		for (MatchmakingQueue expired : expiredEntries) {
			expired.setStatus("expired");
			queueRepository.save(expired);
			waitingUsers.remove(expired.getUser().getId());

			notifyUserQueueTimeout(expired.getUser().getId());
		}

		// Try to find matches for waiting players with expanded criteria
		List<MatchmakingQueue> waitingPlayers = queueRepository.findByStatus("waiting");

		// Reconcile against the database. Queue rows can be removed without going
		// through leaveQueue (account deletion does this), and a stranded entry
		// here would keep the sweep querying forever.
		waitingUsers.retainAll(waitingPlayers.stream()
				.map(p -> p.getUser().getId())
				.collect(Collectors.toSet()));

		for (MatchmakingQueue waiting : waitingPlayers) {
			findRankedMatch(waiting);
		}
	}

	private void notifyUserQueueTimeout(UUID userId) {
		Map<String, Object> data = new HashMap<>();
		data.put("message", "Queue timeout - please try again");

		Map<String, Object> update = createMatchmakingUpdate("queue_timeout", data);
		String topic = "/topic/matchmaking/" + userId;
		messagingTemplate.convertAndSend(topic, update);
	}
}