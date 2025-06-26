package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void create(Game game) {
        this.gameRepository.create(game);
    }

    public void update(Game game) {
        gameRepository.update(game);
    }

    public void delete(UUID id) {
        this.gameRepository.delete(id);
    }

    public Game findById(UUID id) {
        return this.gameRepository.find(id);
    }

    public List<Game> findAll() {
        return this.gameRepository.findAll();
    }
}
