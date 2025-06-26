package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.repository.RoundRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoundService {
    private final RoundRepository roundRepository;

    public RoundService(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    public void create(Round round) {
        this.roundRepository.create(round);
    }

    public void update(Round round) {
        roundRepository.update(round);
    }

    public void delete(UUID id) {
        this.roundRepository.delete(id);
    }

    public Round findById(UUID id) {
        return this.roundRepository.find(id);
    }

    public List<Round> findAll() {
        return this.roundRepository.findAll();
    }
}
