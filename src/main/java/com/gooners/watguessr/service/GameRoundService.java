package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.GameRound;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.repository.GameRoundRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GameRoundService {

    private final GameRoundRepository gameRoundRepository;

    public GameRoundService(GameRoundRepository gameRoundRepository) {
        this.gameRoundRepository = gameRoundRepository;
    }

    public void create(GameRound gameRound) {
        gameRoundRepository.create(gameRound);
    }

    public void update(GameRound gameRound) {
        gameRoundRepository.update(gameRound);
    }

    public void delete(UUID id) {
        this.gameRoundRepository.delete(id);
    }

    public Game getGameFromRound(Round round) {
        GameRound gameRound = gameRoundRepository.findByRound(round);
        return gameRound.getGameId();
    }

    public GameRound findById(UUID id) {
        return this.gameRoundRepository.find(id);
    }

    public List<GameRound> findAll() {
        return this.gameRoundRepository.findAll();
    }
} 