package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final RoundGuessService roundGuessService;
    private final GameRoundService gameRoundService;
    private final UserService userService;

    public GameService(GameRepository gameRepository, RoundGuessService roundGuessService,
            GameRoundService gameRoundService, UserService userService) {
        this.gameRepository = gameRepository;
        this.roundGuessService = roundGuessService;
        this.gameRoundService = gameRoundService;
        this.userService = userService;
    }

    public UUID create(Game newGame) {
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public HashMap<String, Object> finishGame(UUID gameId) {
        Game game = findById(gameId);
        HashMap<String, Object> result = new HashMap<>();

        if (game.getGameMode().equals("Singleplayer")) {
            // end stats for sp game is number of rounds survived
            Integer roundsSurvived = gameRoundService.getRoundCountForGame(gameId);
            result.put("singlePlayerRoundsSurvived", roundsSurvived);
        } else if ((game.getGameMode().equals("Multiplayer")) || game.getGameMode().equals("Ranked")) {
            // get user points for multiplayer game and determine winner
            HashMap<UUID, Integer> userPoints = getUserPointsForGame(gameId);
            result.put("userPoints", userPoints);

            // find winner (user with most points)
            UUID winnerId = findWinner(userPoints);
            if (winnerId != null) {
                User winner = userService.findById(winnerId);
                game.setWinner(winner);
                update(game);
            }
        }

        return result;
    }

    private HashMap<UUID, Integer> getUserPointsForGame(UUID gameId) {
        List<Object[]> userPointsData = roundGuessService.getUserPointsForGame(gameId);
        HashMap<UUID, Integer> userPoints = new HashMap<>();

        for (Object[] row : userPointsData) {
            UUID userId = (UUID) row[0];
            Integer points = ((Number) row[1]).intValue();
            userPoints.put(userId, points);
        }

        return userPoints;
    }

    private UUID findWinner(HashMap<UUID, Integer> userPoints) {
        if (userPoints.isEmpty()) {
            return null;
        }

        UUID winnerId = null;
        Integer maxPoints = Integer.MIN_VALUE;

        for (HashMap.Entry<UUID, Integer> entry : userPoints.entrySet()) {
            if (entry.getValue() > maxPoints) {
                maxPoints = entry.getValue();
                winnerId = entry.getKey();
            }
        }

        return winnerId;
    }

    public void update(Game game) {
        gameRepository.save(game);
    }

    public void delete(UUID id) {
        gameRepository.deleteById(id);
    }

    public Game findById(UUID id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + id));
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }
}
