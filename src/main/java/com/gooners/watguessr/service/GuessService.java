package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.RoundResult;
import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.utils.CustomException;
import com.gooners.watguessr.utils.PointsCalculator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GuessService {

    private final GuessRepository guessRepository;
    private final RoundRepository roundRepository;
    private UserService userService;
    private final MultiplayerGameStateService multiplayerGameStateService;

    public GuessService(GuessRepository guessRepository,
                        UserService userService, RoundService roundService, RoundRepository roundRepository,
                        MultiplayerGameStateService multiplayerGameStateService) {
        this.guessRepository = guessRepository;
        this.userService = userService;
        this.roundRepository = roundRepository;
        this.multiplayerGameStateService = multiplayerGameStateService;
    }
    public RoundResult createAndEvaluateGuess(Guess guess) {
        // First create the guess (validates and saves without points)
        Guess savedGuess = create(guess);
        
        // Then evaluate it (calculates points and updates the saved guess)
        return evaluateGuess(savedGuess.getRound(), savedGuess);
    }
    
    public Guess create(Guess guess) {
        if (guess.getUser() == null) throw new RuntimeException("Guess must have a valid user");
        if (guess.getRound() == null) throw new RuntimeException("Guess must have a valid round");
        if (guess.getBuilding() == null) throw new RuntimeException("Guess must have a building");
        if (guess.getFloor() == null) throw new RuntimeException("Guess must have a floor");
        if (guess.getTime() == null) throw new RuntimeException("Guess must have time");
        if (guess.getPoints() != null) throw new RuntimeException("Points can't be set during creation");

        Game game = guess.getRound().getGame();
        if (game.getWinner() != null) {
            throw new CustomException("Cannot create guesses for completed games");
        }

        Optional<Guess> existingGuess = guessRepository.findFirstByRoundIdAndUserId(
            guess.getRound().getId(), 
            guess.getUser().getId()
        );
        if (existingGuess.isPresent()) {
            throw new CustomException("User has already made a guess for this round");
        }

        Guess newGuess = new Guess();
        newGuess.setPoints(null);
        newGuess.setGuessX(guess.getGuessX());
        newGuess.setGuessY(guess.getGuessY());
        newGuess.setBuilding(guess.getBuilding());
        newGuess.setFloor(guess.getFloor());
        newGuess.setRound(guess.getRound());
        newGuess.setTime(guess.getTime());
        newGuess.setUser(guess.getUser());
        
        return this.guessRepository.save(newGuess);
    }

    public void update(Guess guess) {
        guessRepository.save(guess);
    }

    public void delete(UUID id) {
        this.guessRepository.deleteById(id);
    }

    public Guess findById(UUID id) {
        return this.guessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guess not found with id: " + id));
    }

    public List<Guess> findAll() {
        return guessRepository.findAll();
    }


    public RoundResult evaluateGuess(Round round, Guess guess) {
        guess.setRound(round);
        
        // calculate distance for UI feedback using centralized calculator
        Scene scene = round.getScene();
        double distance = PointsCalculator.calculateDistance(
                guess.getGuessX(), guess.getGuessY(),
                scene.getLocationX(), scene.getLocationY()
        );

        int points = PointsCalculator.calculatePoints(guess, roundRepository);
        if (guess.getPoints() != null) {guess.setPoints(points);}

        try {
            // Update multiplayer game state if this is a multiplayer game
            UUID gameId = round.getGame().getId();
            String gameMode = round.getGame().getGameMode();
            if ("Multiplayer".equals(gameMode) && guess.getUser() != null) {
                // Calculate total score for this user in this game
                Integer totalScore = PointsCalculator.getCurrentMultiplayerScore(gameId, guess.getUser().getId(), roundRepository);

                // Update player progress
                multiplayerGameStateService.updatePlayerProgress(
                    gameId,
                    guess.getUser().getId().toString(),
                    totalScore,
                    "ended" // Player completed this round
                );
            }
            // Set points on the current guess object
            guess.setPoints(points);
            guessRepository.save(guess);
        } catch (Exception ignored) {
        }
        // for singleplayer, PointsCalculator returns negative penalties; UI can display positive lost points
        int uiPoints = Math.abs(points);
        return new RoundResult(uiPoints, distance);
    }


}
