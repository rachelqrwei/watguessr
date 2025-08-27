package com.gooners.watguessr.utils;

import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.RoundRepository;

import java.util.UUID;


public class PointsCalculator {

    //calculate points for a roundGuess
    public static int calculatePoints(Guess guess, RoundRepository roundRepository) {
        Round round = guess.getRound();
        Scene scene = round.getScene();
        Game game = round.getGame();

        // calculate base distance between guess and actual location
        double distance = calculateDistance(
                guess.getGuessX(), guess.getGuessY(),
                scene.getLocationX(), scene.getLocationY()
        );

        // check for perfect matches
        boolean buildingMatch = false;
        boolean floorMatch = false;

        if (guess.getBuilding() != null && scene.getBuilding() != null) {
            String guessedBuildingName = normalize(guess.getBuilding());
            String actualBuildingName = normalize(scene.getBuilding().getName());
            buildingMatch = guessedBuildingName.equals(actualBuildingName);
        }

        if (guess.getFloor() != null && scene.getFloor() != null) {
            floorMatch = normalize(guess.getFloor()).equals(normalize(scene.getFloor()));
        }

        int points;
        String mode = game.getGameMode();
        if ("Singleplayer".equals(mode)) {
            points = calculateSingleplayerPoints(distance, buildingMatch, floorMatch, game, roundRepository, guess.getUser()); // negative penalty
        } else if ("Multiplayer".equals(mode) || "Ranked".equals(mode)) {
            points = calculateMultiplayerPoints(distance, buildingMatch, floorMatch);
        } else {
            points = 0;
        }

        // Apply time-based modifier based on percentage time left
        double timeLeftFraction = computeTimeLeftFraction(game, guess); // 0.0 to 1.0

        if ("Singleplayer".equals(mode)) {
            // Reduce penalty when guessed faster (more time left)
            int penaltyMagnitude = -points; // points are negative for SP
            if (penaltyMagnitude < 0) penaltyMagnitude = 0;
            // Up to 40% reduction when guessed instantly
            double reductionFactor = 1.0 - (0.40 * timeLeftFraction);
            int adjustedPenalty = (int) Math.round(penaltyMagnitude * reductionFactor);
            points = -adjustedPenalty;
        } else if ("Multiplayer".equals(mode) || "Ranked".equals(mode)) {
            // Up to 40% boost when guessed instantly
            double boostFactor = 1.0 + (0.40 * timeLeftFraction);
            points = (int) Math.round(points * boostFactor);
        }

        return points;
    }

    //calculate euclidean distance
    public static double calculateDistance(Double x1, Double y1, Double x2, Double y2) {
        if (x1 == 0 || y1 == 0 || x2 == 0 || y2 == 0) {
            return Double.MAX_VALUE; 
        }

        // Convert lat/lng differences to approximate meters
        // 1 degree latitude ≈ 111,000 meters
        // 1 degree longitude ≈ 111,000 * cos(latitude) meters
        // For University of Waterloo (latitude ≈ 43.47°), cos(43.47°) ≈ 0.727

        double latDiffMeters = (y1 - y2) * 111000; // latitude difference in meters
        double lngDiffMeters = (x1 - x2) * 111000 * 0.727; // longitude difference in meters (adjusted for UW latitude)

        return Math.sqrt(latDiffMeters * latDiffMeters + lngDiffMeters * lngDiffMeters);
    }

    private static double computeTimeLeftFraction(Game game, Guess guess) {
        if (game == null) return 0.0;
        String mode = game.getGameMode();

        // total time in milliseconds per mode
        long totalMs;
        if ("Singleplayer".equals(mode)) {
            totalMs = 30_000L;
        } else if ("Ranked".equals(mode)) {
            totalMs = 20_000L;
        } else if ("Multiplayer".equals(mode)) {
            Integer configuredSeconds = game.getMultiplayerTimer();
            if (configuredSeconds == null || configuredSeconds <= 0) {
                configuredSeconds = 60; // sensible default
            }
            totalMs = configuredSeconds.longValue() * 1000L;
        } else {
            totalMs = 60_000L;
        }

        // time taken in milliseconds
        long timeTakenMs = (guess != null && guess.getTime() != null) ? guess.getTime().longValue() : totalMs;
        if (timeTakenMs < 0) timeTakenMs = 0;

        double left = (double) (totalMs - Math.min(timeTakenMs, totalMs));
        double fraction = left / (double) totalMs;
        if (fraction < 0.0) return 0.0;
        if (fraction > 1.0) return 1.0;
        return fraction;
    }

    //calculate for multiplayer/ranked modes
    private static int calculateMultiplayerPoints(double distance, boolean buildingMatch, boolean floorMatch) {
        // Smooth exponential falloff: 500 at 0m, approaches ~150 by ~200m, no hard clamp
        // base = 150 + 350 * exp(-distance / decayMeters)
        double decayMeters = 60.0; // tune so ~200m yields ~160–170 ("basically 150")
        int basePoints = (int) Math.round(150.0 + 350.0 * Math.exp(-distance / decayMeters));

        // Bonus points for correct building and floor (reduced floor influence)
        if (buildingMatch) {
            basePoints += 100;
            if (floorMatch) {
                basePoints += 20;
            }
        }

        return basePoints;
    }

    //calculate points for a singleplayer game
    private static int calculateSingleplayerPoints(double distance, boolean buildingMatch, boolean floorMatch, Game game, RoundRepository roundRepository, User user) {
        int penalty = 0;

        if (distance > 0) {
            // Polynomial growth tuned so ~100m ≈ ~200 points lost
            double maxDistance = 1500.0;   // meters
            double base = 800.0;           // overall penalty scale
            double gamma = 0.5;            // sqrt curve: higher sensitivity near zero

            double normalized = Math.min(distance / maxDistance, 1.0);
            penalty = (int) Math.round(base * Math.pow(normalized, gamma));

            // Scaled reductions so coordinates dominate
            double closeness = 1.0 - normalized; // 1 near, 0 far
            if (buildingMatch) {
                double buildingReductionMax = 0.50; // up to 50% reduction when very close
                penalty = (int) Math.round(penalty * (1.0 - buildingReductionMax * closeness));
                if (floorMatch) {
                    double floorReductionMax = 0.05; // make floor far less significant than coordinates
                    double nearWeight = closeness * closeness; // only meaningful when very close
                    penalty = (int) Math.round(penalty * (1.0 - floorReductionMax * nearWeight));
                }
            } else {
                // Proximity grace when building is wrong: mild relief when very close
                double graceRadiusMeters = 180.0;
                double proximity = Math.max(0.0, 1.0 - Math.min(distance / graceRadiusMeters, 1.0));
                double maxRelief = 0.30; // max 30% reduction when guessing close
                double factor = 1.0 - (maxRelief * proximity);
                penalty = (int) Math.round(penalty * factor);
            }
        }

        // Ensure small penalty for near-miss
        if (penalty == 0 && distance > 0) {
            penalty = 8;
        }

        // Return negative penalty to be stored in database
        return -penalty;
    }

    //Calculate current score for a singleplayer game
    public static int getCurrentSingleplayerScore(UUID gameId, UUID userId, RoundRepository roundRepository) {
        Integer totalPenalties = roundRepository.getUserPointsForGameAndUser(gameId, userId);
        if (totalPenalties == null) {
            totalPenalties = 0;
        }
        // since penalties are stored as negative numbers, we add them (which subtracts from 1000)
        return Math.max(1000 + totalPenalties, 0); // Ensure score doesn't go below 0
    }

    //Calculate current score for a multiplayer game
    public static Integer getCurrentMultiplayerScore(UUID gameId, UUID userId, RoundRepository roundRepository) {
        Integer totalPoints = roundRepository.getUserPointsForGameAndUser(gameId, userId);
        if (totalPoints == null) {
            totalPoints = 0;
        }
        // For multiplayer, points are positive and we sum them directly
        return Math.max(totalPoints, 0); // Ensure score doesn't go below 0
    }

    //check if a singleplayer game should end
    public static boolean shouldEndSingleplayerGame(UUID gameId, UUID userId, RoundRepository roundRepository) {
        return getCurrentSingleplayerScore(gameId, userId, roundRepository) <= 0;
    }

    private static String normalize(String input) {
        if (input == null) return null;
        return input.toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .trim();
    }
} 