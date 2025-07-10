package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.SingleplayerGameState;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.utils.PointsCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gooners.watguessr.utils.PointsCalculator.*;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final UserService userService;
    private final GameService gameService;
    private final RoundService roundService;
    private final RoundRepository roundRepository;

    public GameService(GameRepository gameRepository,
                       UserService userService, GameService gameService, RoundService roundService, RoundRepository roundRepository) {
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.gameService = gameService;
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

    public Integer resolveSingleplayerGame(UUID gameId) {
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
            
            // Calculate ELO change
            Integer eloChange = calculateEloChange(averageElo, user.getElo(), won, scoreDifference);
            
            // Update user's ELO, ensuring it doesn't go below 0
            Integer newElo = user.getElo() + eloChange;
            user.setElo(Math.max(0, newElo));
            userService.update(user);
        }
    }

    /**
     * Calculates ELO rating change for a player based on match performance
     * @param averageElo The average ELO of all players in the match
     * @param userElo The current ELO of the player
     * @param won Whether the player won the match
     * @param scoreDifference The point difference between the player and winner
     * @return The ELO change (positive for gain, negative for loss)
     */
    private Integer calculateEloChange(Integer averageElo, Integer userElo, boolean won, Integer scoreDifference) {
        // K-factor determines how much ELO can change per game
        // Higher K-factor for beginners, lower for experts
        Integer kFactor;
        if (userElo < 400) {
            kFactor = 40; // Beginners
        } else if (userElo < 1200) {
            kFactor = 30; // Intermediate players
        } else if (userElo < 1800) {
            kFactor = 20; // Advanced players
        } else {
            kFactor = 15; // Expert players
        }
        
        // Calculate expected score based on ELO difference
        // Using standard ELO formula: Expected = 1 / (1 + 10^((opponent_elo - player_elo) / 400))
        double eloDifference = averageElo - userElo;
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, eloDifference / 400.0));
        
        // Actual score: 1 for win, 0 for loss
        // Adjust based on score difference to reward/penalize margin of victory/defeat
        double actualScore = won ? 1.0 : 0.0;
        
        // Adjust for score difference (reduce impact of luck, reward skill)
        if (won && scoreDifference > 0) {
            // Bonus for winning by a large margin (up to 10% bonus)
            double bonus = Math.min(scoreDifference / 1000.0, 0.1);
            actualScore += bonus;
        } else if (!won && scoreDifference > 0) {
            // Reduce penalty for close losses (up to 10% reduction)
            double reduction = Math.min(scoreDifference / 1000.0, 0.1);
            actualScore += reduction;
        }
        
        // Ensure actualScore stays within bounds
        actualScore = Math.max(0.0, Math.min(1.1, actualScore));
        
        // Calculate ELO change
        double eloChange = kFactor * (actualScore - expectedScore);
        
        return (int) Math.round(eloChange);
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
