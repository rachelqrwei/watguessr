package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.SingleplayerGameState;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.utils.PointsCalculator;
import com.gooners.watguessr.utils.EloCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.gooners.watguessr.utils.PointsCalculator.*;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final UserService userService;
    private final RoundService roundService;
    private final RoundRepository roundRepository;

    public GameService(GameRepository gameRepository,
                       UserService userService, RoundService roundService, RoundRepository roundRepository) {
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.roundService = roundService;
        this.roundRepository = roundRepository;
    }

    public UUID createSingleplayerGame() {
        Game newGame = new Game();
        newGame.setGameMode("Singleplayer");
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public UUID createMultiplayerGame(Integer roundCount, Integer timer) {
        Game newGame = new Game();
        newGame.setGameMode("Multiplayer");
        newGame.setMultiplayerRoundCount(roundCount);
        newGame.setMultiplayerTimer(timer);
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public UUID updateMultiplayerGame(UUID gameId, Integer roundCount, Integer timer) {
        Game game = findById(gameId);
        game.setMultiplayerRoundCount(roundCount);
        game.setMultiplayerTimer(timer);
        return gameRepository.save(game).getId();
    }

    public UUID createRankedGame(Integer averageElo) {
        Game newGame = new Game();
        newGame.setGameMode("Ranked");
        newGame.setRankedAverageElo(averageElo);
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public Integer resolveSingleplayerGame(UUID gameId, UUID userId) {
        Game game = findById(gameId);
        User singleplayerUser = userService.findById(userId);
        game.setWinner(singleplayerUser);
        update(game);
        return roundService.getRoundCountForGame(gameId);
    }

    public HashMap<UUID, Integer> resolveMultiplayerGame(UUID gameId) {
        Game game = findById(gameId);
        HashMap<UUID, Integer> userPoints = getUserPointsForGame(gameId);
        UUID winnerId = findWinner(userPoints);
        
        User winner =  userService.findById(winnerId);
        game.setWinner(winner);
        update(game);

        return userPoints;
    }

    public HashMap<UUID, Integer> resolveRankedGame(UUID gameId) {
        Game game = findById(gameId);
        HashMap<UUID, Integer> userPoints = getUserPointsForGame(gameId);
        UUID winnerId = findWinner(userPoints);
        Integer averageElo = game.getRankedAverageElo();

        User winner = userService.findById(winnerId);
        game.setWinner(winner);
        update(game);

        updateEloRatings(userPoints, averageElo);

        return userPoints;
    }

    /**
     * Updates ELO ratings for all players in a ranked game
     * @param userPoints HashMap containing user IDs and their scores
     * @param averageElo The average ELO of all players in the match
     */
    private void updateEloRatings(HashMap<UUID, Integer> userPoints, Integer averageElo) {
        UUID winnerId = findWinner(userPoints);
        Integer winnerScore = userPoints.get(winnerId);
        
        for (HashMap.Entry<UUID, Integer> entry : userPoints.entrySet()) {
            UUID userId = entry.getKey();
            Integer userScore = entry.getValue();
            User user = userService.findById(userId);
            
            // Determine if user won
            boolean won = userId.equals(winnerId);
            
            // Calculate score difference from winner's score
            Integer scoreDifference = Math.abs(userScore - winnerScore);
            
            // Calculate ELO change using utility
            Integer eloChange = EloCalculator.calculateEloChange(averageElo, user.getElo(), won, scoreDifference);
            
            // Update user's ELO, ensuring it doesn't go below 0
            Integer newElo = user.getElo() + eloChange;
            user.setElo(Math.max(0, newElo));
            userService.update(user);
        }
    }

    private HashMap<UUID, Integer> getUserPointsForGame(UUID gameId) {
        List<Object[]> userPointsData = roundService.getUserPointsForGame(gameId);
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

    public Integer getCurrentSingleplayerScore(UUID gameId, UUID userId) {
        return PointsCalculator.getCurrentSingleplayerScore(gameId, userId, roundRepository);
    }

    public SingleplayerGameState getSingleplayerGameState(UUID gameId, UUID userId) {
        Integer currentScore = getCurrentSingleplayerScore(gameId, userId);
        Integer roundsCompleted = roundService.getRoundCountForGame(gameId);
        boolean shouldEnd = shouldEndSingleplayerGame(gameId, userId, roundRepository);

        Game game = findById(gameId);
        boolean isGameEnded = game.getWinner() != null;

        return new SingleplayerGameState(gameId, currentScore, roundsCompleted, shouldEnd, isGameEnded);
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
