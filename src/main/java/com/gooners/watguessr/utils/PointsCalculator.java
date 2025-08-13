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

        if (game.getGameMode().equals("Singleplayer")) {
            return calculateSingleplayerPoints(distance, buildingMatch, floorMatch, game, roundRepository, guess.getUser());
        } else if (game.getGameMode().equals("Multiplayer") || game.getGameMode().equals("Ranked")) {
            return calculateMultiplayerPoints(distance, buildingMatch, floorMatch);
        }

        return 0;
    }

    //calculate euclidean distance
    public static double calculateDistance(Double x1, Double y1, Double x2, Double y2) {
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

    //calculate for multiplayer/ranked modes
    private static int calculateMultiplayerPoints(double distance, boolean buildingMatch, boolean floorMatch) {
        int basePoints = 0;

        // Less exponential: polynomial falloff based on normalized distance
        // points ≈ 500 * (1 - normalizedDistance)^beta
        double maxDistance = 2000.0; // meters
        double normalizedDistance = Math.min(distance / maxDistance, 1.0);
        double beta = 1.1; // 1.0 is linear; >1 curves slightly
        basePoints = (int) Math.round(500 * Math.pow(1.0 - normalizedDistance, beta));

        // Bonus points for correct building and floor (reduced floor influence)
        if (buildingMatch) {
            basePoints += 100;
            if (floorMatch) {
                basePoints += 20; // floor matters less than coordinates
            }
        }

        return Math.max(basePoints, 25); // Minimum 25 points for any guess
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