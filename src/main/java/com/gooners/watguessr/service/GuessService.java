package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.repository.GameRoundRepository;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.utils.PointsCalculator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GuessService {

    private final GuessRepository guessRepository;

    @Autowired
    private GameRoundService gameRoundService;

    @Autowired
    private RoundGuessService roundGuessService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameRoundRepository gameRoundRepository;

    @Autowired
    public GuessService(GuessRepository guessRepository) {
        this.guessRepository = guessRepository;
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
        roundGuess.setPoints(PointsCalculator.calculatePoints(roundGuess, game, gameRoundRepository));
        
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
