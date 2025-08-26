package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.utils.CustomException;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RoundService {
    private final RoundRepository roundRepository;
    private final SceneService sceneService;
    private final GameRepository gameRepository;
    private final GuessRepository guessRepository;

    public RoundService(RoundRepository roundRepository, SceneService sceneService,
            GameRepository gameRepository, GuessRepository guessRepository) {
        this.roundRepository = roundRepository;
        this.sceneService = sceneService;
        this.gameRepository = gameRepository;
        this.guessRepository = guessRepository;
    }

    public Round create(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

        if (game.getWinner() != null) {
            throw new CustomException("Cannot create rounds for completed games");
        }
        Round newRound = new Round();
        newRound.setScene(sceneService.getRandom());
        newRound.setGame(game);

        Round savedRound = roundRepository.save(newRound);

        return savedRound;
    }

    public Round update(Round round) {
        return roundRepository.save(round);
    }

    public Round findById(UUID id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Round not found with id: " + id));
    }

    List<Object[]> getUserPointsForGame(@Param("gameId") UUID gameId) {
        return roundRepository.getUserPointsForGame(gameId);
    }

    public Integer getRoundCountForGame(UUID gameId) {
        return roundRepository.getRoundCountForGame(gameId);
    }

    public Optional<Guess> findGuessForUserAndRound(UUID userId, UUID roundId) {
        return guessRepository.findFirstByRoundIdAndUserId(roundId, userId);
    }

    public int cleanupEmptyRoundsFromFinishedGames() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(2); // 2 hours before now
        List<Round> emptyRounds = roundRepository.findEmptyRoundsFromFinishedGamesOlderThan(cutoff);
        
        if (!emptyRounds.isEmpty()) {
            roundRepository.deleteAll(emptyRounds);
            System.out.println("Cleaned up " + emptyRounds.size() + " empty rounds from finished games");
        }

        return emptyRounds.size();
    }

}
