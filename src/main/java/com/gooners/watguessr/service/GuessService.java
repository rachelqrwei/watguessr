package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.repository.RoundGuessRepository;
import com.gooners.watguessr.utils.PointsCalculator;
import jakarta.transaction.Transactional;
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
    private final RoundGuessRepository roundGuessRepository;

    public GuessService(GuessRepository guessRepository, GameRoundService gameRoundService,
                       RoundGuessService roundGuessService, RoundService roundService,
                       UserService userService, RoundGuessRepository roundGuessRepository) {
        this.guessRepository = guessRepository;
        this.gameRoundService = gameRoundService;
        this.roundGuessService = roundGuessService;
        this.roundService = roundService;
        this.userService = userService;
        this.roundGuessRepository = roundGuessRepository;
    }

    public void create(Guess guess, UUID userId, UUID roundId) {
        User user = userService.findById(userId);
        Round round = roundService.findById(roundId);
        guess.setUser(user);

        RoundGuess roundGuess = new RoundGuess();
        roundGuess.setRound(round);
        roundGuess.setGuess(guess);
        roundGuess.setUser(user);
        
        // Calculate points using utility class
        Game game = gameRoundService.getGameFromRound(round);
        roundGuess.setPoints(PointsCalculator.calculatePoints(roundGuess, game, roundGuessRepository));
        
        roundGuessService.create(roundGuess);

        this.guessRepository.save(guess);
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
        return this.guessRepository.findAll();
    }
}
