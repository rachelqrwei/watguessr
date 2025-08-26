package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.RoundResult;
import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.mapper.GuessMapper;
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

    public Guess create(Guess guess) {
        Game game = guess.getRound().getGame();
        if (game.getWinner() != null) {
            throw new CustomException("Cannot create guesses for completed games");
        }

        // Check if user has already made a guess for this round
        if (guess.getUser() != null && guess.getUser().getId() != null && guess.getRound() != null && guess.getRound().getId() != null) {
            Optional<Guess> existingGuess = guessRepository.findFirstByRoundIdAndUserId(guess.getRound().getId(), guess.getUser().getId());
            if (existingGuess.isPresent()) {
                throw new CustomException("User has already made a guess for this round");
            }
        }

        if (guess.getPoints() != null) {
            throw new RuntimeException("points can't be set");
        }

        // object instantiation
        Guess newGuess = new Guess();
        newGuess.setPoints(null);
        newGuess.setGuessX(guess.getGuessX());
        newGuess.setGuessY(guess.getGuessY());
        if (guess.getBuilding() != null ) {
            newGuess.setBuilding(guess.getBuilding());
            newGuess.setFloor(guess.getFloor());
        }
        newGuess.setRound(guess.getRound());
        newGuess.setTime(guess.getTime());
        newGuess.setUser(guess.getUser());

        if (newGuess.getUser() != null && newGuess.getUser().getId() != null) {
            userService.findById(newGuess.getUser().getId());
        }

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

        // fill minimal required fields if null to satisfy schema during timeouts BEFORE calculations
        if (guess.getTime() == null) guess.setTime(60000);
        if (guess.getGuessX() == null) guess.setGuessX(0.0);
        if (guess.getGuessY() == null) guess.setGuessY(0.0);
        if (guess.getBuilding() == null) guess.setBuilding("NO_GUESS");
        if (guess.getFloor() == null) {
            String defaultFloor = null;
            Scene scene = round.getScene();
            Building building = scene != null ? scene.getBuilding() : null;
            if (building != null && building.getFloors() != null && !building.getFloors().isEmpty()) {
                defaultFloor = building.getFloors().get(0);
            }
            if (defaultFloor == null) defaultFloor = "UNKNOWN";
            guess.setFloor(defaultFloor);
        }

        // calculate distance for UI feedback using centralized calculator
        Scene scene = round.getScene();
        double distance = PointsCalculator.calculateDistance(
                guess.getGuessX(), guess.getGuessY(),
                scene.getLocationX(), scene.getLocationY()
        );

        int points = PointsCalculator.calculatePoints(guess, roundRepository);
        if (guess.getPoints() == null) {guess.setPoints(points);}

        try {
            // ensure user exists if provided
            if (guess.getUser() != null && guess.getUser().getId() != null) {
                userService.findById(guess.getUser().getId());
            }

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
            // Upsert: if a guess already exists for (round, user), update it
            if (guess.getUser() != null && guess.getUser().getId() != null && round.getId() != null) {
                guessRepository.findFirstByRoundIdAndUserId(round.getId(), guess.getUser().getId())
                        .ifPresent(existing -> {
                            existing.setGuessX(guess.getGuessX());
                            existing.setGuessY(guess.getGuessY());
                            existing.setBuilding(guess.getBuilding());
                            existing.setFloor(guess.getFloor());
                            existing.setTime(guess.getTime());
                            existing.setPoints(points);
                            // persist update
                            guessRepository.save(existing);
                        });
                // If none existed, save as new
                if (!guessRepository.findFirstByRoundIdAndUserId(round.getId(), guess.getUser().getId()).isPresent()) {
                    guessRepository.save(guess);
                }
            } else {
                // If no user or round id, fallback to save incoming guess
                guessRepository.save(guess);
            }
        } catch (Exception ignored) {
        }

        // for singleplayer, PointsCalculator returns negative penalties; UI can display positive lost points
        int uiPoints = Math.abs(points);
        return new RoundResult(uiPoints, distance);
    }

}
