package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GameRoundRepository;
import com.gooners.watguessr.repository.GuessRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GuessService {

    private final GuessRepository guessRepository;
    private final GameRoundService gameRoundService;
    private final RoundGuessService roundGuessService;
    private final RoundService roundService;
    private final UserService userService;
    private final EntityManager entityManager;
    private final GameRoundRepository gameRoundRepository;

    public GuessService(
            GuessRepository guessRepository,
            GameRoundService gameRoundService,
            RoundGuessService roundGuessService,
            RoundService roundService,
            UserService userService,
            EntityManager entityManager,
            GameRoundRepository gameRoundRepository) {
        this.guessRepository = guessRepository;
        this.gameRoundService = gameRoundService;
        this.roundGuessService = roundGuessService;
        this.roundService = roundService;
        this.userService = userService;
        this.entityManager = entityManager;
        this.gameRoundRepository = gameRoundRepository;
    }

    public void create(Guess guess, UUID userId, UUID roundId) {
        User user = userService.findById(userId);
        Round round = roundService.findById(roundId);
        guess.setUser(user);

        RoundGuess roundGuess = new RoundGuess();
        roundGuess.setRound(round);
        roundGuess.setGuess(guess);
        roundGuess.setUser(user);
        roundGuess.setPoints(calculatePoints(roundGuess));
        roundGuessService.create(roundGuess);

        this.guessRepository.create(guess);
    }

    public void update(Guess guess) {
        guessRepository.update(guess);
    }

    public void delete(UUID id) {
        this.guessRepository.delete(id);
    }

    public Guess findById(UUID id) {
        return this.guessRepository.find(id);
    }

    public List<Guess> findAll() {
        return this.guessRepository.findAll();
    }

    public int calculatePoints(RoundGuess roundGuess) {
        Round round = roundGuess.getRound();
        Game game = gameRoundService.getGameFromRound(round);
        Guess guess = roundGuess.getGuess();
        Scene scene = round.getScene();

        // calculate base distance between guess and actual location
        double distance = calculateDistance(
                guess.getGuessX(), guess.getGuessY(),
                scene.getLocationX(), scene.getLocationY()
        );

        // check for perfect matches
        boolean buildingMatch = false;
        boolean floorMatch = false;

        if (guess.getBuilding() != null && scene.getBuilding() != null) {
            buildingMatch = guess.getBuilding().getId().equals(scene.getBuilding().getId());
        }

        if (guess.getFloor() != null && scene.getFloor() != null) {
            floorMatch = guess.getFloor().equals(scene.getFloor());
        }

        if (game.getGameMode().equals("Singleplayer")) {
            return calculateSingleplayerPoints(distance, buildingMatch, floorMatch, game);
        }
        else if (game.getGameMode().equals("Multiplayer") || game.getGameMode().equals("Ranked")) {
            return calculateMultiplayerPoints(distance, buildingMatch, floorMatch);
        }

        return 0; // Default fallback
    }

    /**
     * Calculate distance between two points using Euclidean distance
     * For lat/lng coordinates, this approximation works well for small distances like a campus
     */
    private double calculateDistance(Double x1, Double y1, Double x2, Double y2) {
        if (x1 == null || y1 == null || x2 == null || y2 == null) {
            return Double.MAX_VALUE; // maximum penalty for missing coordinates
        }
        
        // Convert lat/lng differences to approximate meters
        // 1 degree latitude ≈ 111,000 meters
        // 1 degree longitude ≈ 111,000 * cos(latitude) meters
        // For University of Waterloo (latitude ≈ 43.47°), cos(43.47°) ≈ 0.727
        
        double latDiffMeters = (y1 - y2) * 111000; // latitude difference in meters
        double lngDiffMeters = (x1 - x2) * 111000 * 0.727; // longitude difference in meters (adjusted for UW latitude)
        
        return Math.sqrt(latDiffMeters * latDiffMeters + lngDiffMeters * lngDiffMeters);
    }

    /**
     * Calculate points for multiplayer/ranked modes
     * Players start at 0 and gain points based on accuracy
     */
    private int calculateMultiplayerPoints(double distance, boolean buildingMatch, boolean floorMatch) {
        int basePoints = 0;
        
        // Distance-based scoring with less steep exponential falloff
        if (distance == 0) {
            basePoints = 1000;
        } else {
            // Maximum reasonable distance on campus: ~2km (2000 meters)
            double maxDistance = 2000.0; // meters
            double normalizedDistance = Math.min(distance / maxDistance, 1.0);
            
            // Less steep exponential decay: points = 1000 * e^(-1.5 * normalizedDistance)
            // This gives more generous scoring:
            // - 100m away: ~861 points
            // - 250m away: ~688 points  
            // - 500m away: ~472 points
            // - 1000m away: ~223 points
            // - 2000m+ away: ~50 points
            basePoints = (int) (1000 * Math.exp(-1.5 * normalizedDistance));
        }
        
        // Bonus points for correct building and floor
        if (buildingMatch) {
            basePoints += 200; // Building bonus
            if (floorMatch) {
                basePoints += 100; // Additional floor bonus
            }
        }
        
        return Math.max(basePoints, 50); // Minimum 50 points for any guess
    }

    /**
     * Calculate points for singleplayer mode
     * Players start at 1000 points and lose points based on inaccuracy
     */
    private int calculateSingleplayerPoints(double distance, boolean buildingMatch, boolean floorMatch, Game game) {
        // Check if this is the first round of the game
        boolean isFirstRound = isFirstRoundOfGame(game);
        
        if (!isFirstRound) {
            // If not first round, calculate as normal multiplayer points
            return calculateMultiplayerPoints(distance, buildingMatch, floorMatch);
        }
        
        int startingPoints = 1000;
        int penalty = 0;
        
        // Distance-based penalty with less steep falloff
        if (distance > 0) {
            // Maximum reasonable distance on campus: ~2km (2000 meters)
            double maxDistance = 2000.0; // meters
            double normalizedDistance = Math.min(distance / maxDistance, 1.0);
            
            // Exponential penalty that's less harsh:
            // penalty = 850 * (1 - e^(-1.2 * normalizedDistance))
            // This gives:
            // - 100m away: ~49 point penalty
            // - 250m away: ~179 point penalty
            // - 500m away: ~328 point penalty
            // - 1000m away: ~567 point penalty
            // - 2000m+ away: ~850 point penalty
            penalty = (int) (850 * (1 - Math.exp(-1.2 * normalizedDistance)));
        }
        
        // Reduce penalty for correct building/floor
        if (buildingMatch) {
            penalty = (int) (penalty * 0.4); // 60% penalty reduction for correct building
            if (floorMatch) {
                penalty = (int) (penalty * 0.6); // Additional 40% reduction for correct floor
            }
        }
        
        return Math.max(startingPoints - penalty, 50); // Minimum 50 points, ensure non-negative points
    }

    /**
     * Check if this is the first round of a singleplayer game
     */
    private boolean isFirstRoundOfGame(Game game) {
        try {
            // Count existing GameRounds for this game
            Long roundCount = entityManager.createQuery(
                            "SELECT COUNT(gr) FROM GameRound gr WHERE gr.gameId.id = :gameId", Long.class)
                    .setParameter("gameId", game.getId())
                    .getSingleResult();

            return roundCount <= 1; // first round (current round is already created)
        } catch (Exception e) {
            // if query fails, assume it's the first round
            return true;
        }
    }

}
