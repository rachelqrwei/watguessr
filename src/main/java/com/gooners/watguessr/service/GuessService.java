package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.RoundGuess;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.GuessRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GuessService {

    private final GuessRepository guessRepository;

    private GuessService guessService;

    @Autowired
    private RoundGuessService roundGuessService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    public GuessService(GuessService guessService, GuessRepository guessRepository) {

        this.guessService = guessService;
        this.guessRepository = guessRepository;
    }

    public void create(Guess guess) {
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

    public void createRoundGuess(UUID roundId, UUID userId, UUID guessId, Integer points) {
        RoundGuess roundGuess = new RoundGuess();
        Round round = roundService.findById(roundId);
        if (round != null) {
            roundGuess.setRound(round);
        }
        User user = userService.findById(userId);
        if (user != null) {
            roundGuess.setUser(user);
        }
        Guess guess = guessService.findById(guessId);
        if (guess != null) {
            roundGuess.setGuess(guess);
        }
        roundGuess.setPoints(points);
        entityManager.persist(roundGuess);
    }

    public List<Round> getAllRound() {
        return roundService.findAll();
    }
}
