package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.GameRound;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.repository.RoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoundService {
    private final RoundRepository roundRepository;
    private final GameRoundService gameRoundService;
    private final SceneService sceneService;
    private final GameService gameService;

    public RoundService(RoundRepository roundRepository, GameRoundService gameRoundService, SceneService sceneService,
            GameService gameService) {
        this.roundRepository = roundRepository;
        this.gameRoundService = gameRoundService;
        this.sceneService = sceneService;
        this.gameService = gameService;
    }

    public UUID create(UUID gameId) {
        Game game = gameService.findById(gameId);

        Round newRound = new Round();
        newRound.setScene(sceneService.getRandom());
        Round savedRound = roundRepository.save(newRound);

        GameRound newGameRound = new GameRound();
        newGameRound.setGame(game);
        newGameRound.setRound(savedRound);
        gameRoundService.create(newGameRound);

        return savedRound.getId();
    }

    public Round update(Round round) {
        return roundRepository.save(round);
    }

    public void delete(UUID id) {
        roundRepository.deleteById(id);
    }

    public Round findById(UUID id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Round not found with id: " + id));
    }

    public List<Round> findAll() {
        return roundRepository.findAll();
    }
}
