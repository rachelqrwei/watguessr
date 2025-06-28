package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.RoundGuess;
import com.gooners.watguessr.repository.RoundGuessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoundGuessService {
    private final RoundGuessRepository roundGuessRepository;

    public RoundGuessService(RoundGuessRepository roundGuessRepository) {
        this.roundGuessRepository = roundGuessRepository;
    }

    public RoundGuess create(RoundGuess roundGuess) {
        return roundGuessRepository.save(roundGuess);
    }

    public RoundGuess update(RoundGuess roundGuess) {
        return roundGuessRepository.save(roundGuess);
    }

    public void delete(UUID id) {
        roundGuessRepository.deleteById(id);
    }

    public RoundGuess findById(UUID id) {
        return roundGuessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoundGuess not found with id: " + id));
    }

    public List<RoundGuess> findAll() {
        return roundGuessRepository.findAll();
    }

    public List<RoundGuess> findByGameId(UUID gameId) {
        return roundGuessRepository.findByGameId(gameId);
    }

    public List<Object[]> getUserPointsForGame(UUID gameId) {
        return roundGuessRepository.getUserPointsForGame(gameId);
    }
}