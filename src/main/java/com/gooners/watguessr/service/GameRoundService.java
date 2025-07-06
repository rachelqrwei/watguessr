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
        gameRoundRepository.save(gameRound);
    }

    public Game getGameFromRound(Round round) {
        GameRound gameRound = gameRoundRepository.findByRoundId(round.getId());
        return gameRound.getGame();
    }

    public List<GameRound> findAll() {
        return this.gameRoundRepository.findAll();
    }

    public List<GameRound> findByGameId(UUID gameId) {
        return this.gameRoundRepository.findByGameId(gameId);
    }

    public Integer getRoundCountForGame(UUID gameId) {
        return this.gameRoundRepository.getRoundCountForGame(gameId);
    }
} 