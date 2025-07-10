package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.RoundRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoundService {
    private final RoundRepository roundRepository;
    private final SceneService sceneService;
    private final GameRepository gameRepository;

    public RoundService(RoundRepository roundRepository, SceneService sceneService,
            GameRepository gameRepository) {
        this.roundRepository = roundRepository;
        this.sceneService = sceneService;
        this.gameRepository = gameRepository;
    }

    public UUID create(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

        Round newRound = new Round();
        newRound.setScene(sceneService.getRandom());
        newRound.setGame(game);
        Round savedRound = roundRepository.save(newRound);

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

    public List<Round> findByGameId(UUID gameId) {
        return roundRepository.findByGameId(gameId);
    }

    List<Object[]> getUserPointsForGame(@Param("gameId") UUID gameId){ return roundRepository.getUserPointsForGame(gameId); }


    public Integer getRoundCountForGame(UUID gameId) {
        return roundRepository.getRoundCountForGame(gameId);
    }
}
