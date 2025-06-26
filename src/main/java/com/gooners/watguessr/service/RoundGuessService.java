package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.RoundGuess;
import com.gooners.watguessr.repository.RoundGuessRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoundGuessService {
    private final RoundGuessRepository roundGuessRepository;

    public RoundGuessService(RoundGuessRepository roundGuessRepository) {
        this.roundGuessRepository = roundGuessRepository;
    }

    public void create(RoundGuess roundGuess) {
        this.roundGuessRepository.create(roundGuess);
    }

    public void update(RoundGuess roundGuess) {
        roundGuessRepository.update(roundGuess);
    }

    public void delete(UUID id) {
        this.roundGuessRepository.delete(id);
    }

    public RoundGuess findById(UUID id) {
        return this.roundGuessRepository.find(id);
    }

    public List<RoundGuess> findAll() {
        return this.roundGuessRepository.findAll();
    }
} 