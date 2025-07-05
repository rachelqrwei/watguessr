package com.gooners.watguessr.utils;

/**
 * Utility class for ELO rating calculations
 */
public class EloCalculator {

    /**
     * Calculates ELO rating change for a player based on match performance
     * @param averageElo The average ELO of all players in the match
     * @param userElo The current ELO of the player
     * @param won Whether the player won the match
     * @param scoreDifference The point difference between the player and winner
     * @return The ELO change (positive for gain, negative for loss)
     */
    public static Integer calculateEloChange(Integer averageElo, Integer userElo, boolean won, Integer scoreDifference) {
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
} 