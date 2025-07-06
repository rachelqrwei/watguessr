package com.gooners.watguessr.utils;

import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GameRoundRepository;
import com.gooners.watguessr.repository.RoundGuessRepository;

import java.util.UUID;


public class PointsCalculator {

    //calculate points for a roundGuess
    public static int calculatePoints(RoundGuess roundGuess, Game game, RoundGuessRepository roundGuessRepository) {
        Round round = roundGuess.getRound();
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
            return calculateSingleplayerPoints(distance, buildingMatch, floorMatch, game, roundGuessRepository, roundGuess.getUser());
        } else if (game.getGameMode().equals("Multiplayer") || game.getGameMode().equals("Ranked")) {
            return calculateMultiplayerPoints(distance, buildingMatch, floorMatch);
        }

        return 0;
    }

    //calculate euclidean distance
    private static double calculateDistance(Double x1, Double y1, Double x2, Double y2) {
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

        // Distance-based scoring with less steep exponential falloff
        if (distance == 0) {
            basePoints = 500; // Reduced from 1000 to 500
        } else {
            // Maximum reasonable distance on campus: ~2km (2000 meters)
            double maxDistance = 2000.0; // meters
            double normalizedDistance = Math.min(distance / maxDistance, 1.0);

            // Less steep exponential decay: points = 500 * e^(-1.5 * normalizedDistance)
            // This gives more balanced scoring:
            // - 100m away: ~430 points
            // - 250m away: ~344 points  
            // - 500m away: ~236 points
            // - 1000m away: ~111 points
            // - 2000m+ away: ~25 points
            basePoints = (int) (500 * Math.exp(-1.5 * normalizedDistance));
        }

        // Bonus points for correct building and floor (reduced proportionally)
        if (buildingMatch) {
            basePoints += 100; // Reduced from 200 to 100
            if (floorMatch) {
                basePoints += 50; // Reduced from 100 to 50
            }
        }

        return Math.max(basePoints, 25); // Minimum 25 points for any guess (reduced from 50)
    }

    //calculate points for a singleplayer game
    private static int calculateSingleplayerPoints(double distance, boolean buildingMatch, boolean floorMatch, Game game, RoundGuessRepository roundGuessRepository, User user) {
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

        // Ensure minimum penalty of 10 points for any guess (except perfect)
        if (penalty == 0 && distance > 0) {
            penalty = 10;
        }

        // Return negative penalty to be stored in database
        return -penalty;
    }

    //Calculate current score for a singleplayer game
    public static int getCurrentSingleplayerScore(UUID gameId, UUID userId, RoundGuessRepository roundGuessRepository) {
        Integer totalPenalties = roundGuessRepository.getUserPointsForGameAndUser(gameId, userId);
        if (totalPenalties == null) {
            totalPenalties = 0;
        }
        // since penalties are stored as negative numbers, we add them (which subtracts from 1000)
        return Math.max(1000 + totalPenalties, 0); // Ensure score doesn't go below 0
    }

    //check if a singleplayer game should end
    public static boolean shouldEndSingleplayerGame(UUID gameId, UUID userId, RoundGuessRepository roundGuessRepository) {
        return getCurrentSingleplayerScore(gameId, userId, roundGuessRepository) <= 0;
    }
} 