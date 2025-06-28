package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.GameRound;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.GameRoundRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.repository.SceneRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoundService {
    private final RoundRepository roundRepository;
    private final GameRoundRepository gameRoundRepository;
    private final SceneService sceneService;
    private final GameService gameService;

    @Autowired
    public RoundService(RoundRepository roundRepository, GameRoundRepository gameRoundRepository, SceneService sceneService, GameService gameService) {
        this.roundRepository = roundRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.sceneService = sceneService;
        this.gameService = gameService;
    }

    public UUID create(UUID gameId) {
        Game game = gameService.findById(gameId);
        Round newRound = new Round();
        newRound.setScene(sceneService.getRandom());
        UUID newRoundId = newRound.getId();
        this.roundRepository.create(newRound);

        GameRound newGameRound = new GameRound();
        newGameRound.setGame(game);
        newGameRound.setRound(newRound);

        this.gameRoundRepository.create(newGameRound);

        return newRoundId;
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
