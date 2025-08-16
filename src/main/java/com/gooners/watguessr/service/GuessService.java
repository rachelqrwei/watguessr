package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.RoundResult;
import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.utils.PointsCalculator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GuessService {

    private final GuessRepository guessRepository;
    private final RoundRepository roundRepository;
    private UserService userService;

    public GuessService(GuessRepository guessRepository,
                        UserService userService, RoundService roundService, RoundRepository roundRepository) {
        this.guessRepository = guessRepository;
        this.userService = userService;
        this.roundRepository = roundRepository;
    }

    public Guess create(Guess guess) {
        // check if guess can be instantiated
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

    public List<Guess> findAllGuessByRoundId(UUID roundId) {
        return guessRepository.findAllByRoundId(roundId);
    }

    String normalize(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9 ]", "") // remove punctuation
                .trim();
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
        guess.setPoints(points);

        try {
            // ensure user exists if provided
            if (guess.getUser() != null && guess.getUser().getId() != null) {
                userService.findById(guess.getUser().getId());
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

    public List<Object[]> findUserPointsByGame (UUID gameId) {
        return guessRepository.findUserPointsByGame(gameId);
    }


}
