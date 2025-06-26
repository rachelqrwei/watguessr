package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.repository.GuessRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GuessService {
    private final GuessRepository guessRepository;

    public GuessService(GuessRepository guessRepository) {
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
}
