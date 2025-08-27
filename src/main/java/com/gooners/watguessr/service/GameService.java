package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.*;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.RoundRepository;
import com.gooners.watguessr.utils.PointsCalculator;
import com.gooners.watguessr.utils.EloCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.gooners.watguessr.utils.PointsCalculator.*;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final UserService userService;
    private final RoundService roundService;
    private final RoundRepository roundRepository;
    private final LobbyService lobbyService;
    private final UserMapper userMapper;
    private final MultiplayerGameStateService multiplayerGameStateService;

    public GameService(GameRepository gameRepository,
            UserService userService, RoundService roundService, RoundRepository roundRepository,
            LobbyService lobbyService, UserMapper userMapper, MultiplayerGameStateService multiplayerGameStateService) {
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.roundService = roundService;
        this.roundRepository = roundRepository;
        this.lobbyService = lobbyService;
        this.userMapper = userMapper;
        this.multiplayerGameStateService = multiplayerGameStateService;
    }

    public UUID createSingleplayerGame() {
        Game newGame = new Game();
        newGame.setGameMode("Singleplayer");
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public UUID createMultiplayerGame(Integer roundCount, Integer timer) {
        Game newGame = new Game();
        newGame.setGameMode("Multiplayer");
        newGame.setMultiplayerRoundCount(roundCount);
        newGame.setMultiplayerTimer(timer);
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public UUID updateMultiplayerGame(UUID gameId, Integer roundCount, Integer timer) {
        Game game = findById(gameId);
        game.setMultiplayerRoundCount(roundCount);
        game.setMultiplayerTimer(timer);
        return gameRepository.save(game).getId();
    }

    public UUID createRankedGame(Integer averageElo) {
        Game newGame = new Game();
        newGame.setGameMode("Ranked");
        newGame.setRankedAverageElo(averageElo);
        newGame.setCreatedAt(OffsetDateTime.now());
        return gameRepository.save(newGame).getId();
    }

    public LobbyDto createLobby(LobbyCreateDto lobbyCreateDto) {
        Game newGame = new Game();
        newGame.setGameMode(lobbyCreateDto.getGameMode());
        newGame.setLobbyName(lobbyCreateDto.getLobbyName());
        newGame.setIsPrivate(lobbyCreateDto.getIsPrivate());
        newGame.setMaxPlayers(lobbyCreateDto.getMaxPlayers());
        newGame.setMultiplayerTimer(lobbyCreateDto.getMultiplayerTimer());
        newGame.setMultiplayerRoundCount(lobbyCreateDto.getMultiplayerRoundCount());
        newGame.setCreatedAt(OffsetDateTime.now());

        // Generate lobby code for private lobbies
        if (lobbyCreateDto.getIsPrivate()) {
            newGame.setLobbyCode(generateLobbyCode());
        }

        Game savedGame = gameRepository.save(newGame);

        // Convert to LobbyDto
        LobbyDto lobbyDto = new LobbyDto();
        lobbyDto.setId(savedGame.getId());
        lobbyDto.setLobbyName(savedGame.getLobbyName());
        lobbyDto.setGameMode(savedGame.getGameMode());
        lobbyDto.setIsPrivate(savedGame.getIsPrivate());
        lobbyDto.setLobbyCode(savedGame.getLobbyCode());
        lobbyDto.setMaxPlayers(savedGame.getMaxPlayers());
        lobbyDto.setCurrentPlayers(0); // Will be updated when players join
        lobbyDto.setMultiplayerTimer(savedGame.getMultiplayerTimer());
        lobbyDto.setMultiplayerRoundCount(savedGame.getMultiplayerRoundCount());
        lobbyDto.setCreatedAt(savedGame.getCreatedAt());
        lobbyDto.setPlayers(List.of());

        return lobbyDto;
    }

    public List<LobbyDto> getPublicLobbies() {
        List<Game> publicGames = gameRepository.findByIsPrivateFalseAndGameMode("Multiplayer");

        // Filter out games that don't have active lobbies in LobbyService
        return publicGames.stream()
                .filter(game -> {
                    // Only include games that have active lobbies with players
                    List<User> lobbyUsers = lobbyService.getUsers(game.getId());
                    return !lobbyUsers.isEmpty();
                })
                .map(this::convertGameToLobbyDto)
                .collect(Collectors.toList());
    }

    public LobbyDto getLobbyByCode(String lobbyCode) {
        Game game = gameRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new RuntimeException("Lobby not found with code: " + lobbyCode));
        return convertGameToLobbyDto(game);
    }

    public LobbyDto getLobbyById(UUID lobbyId) {
        Game game = findById(lobbyId);
        return convertGameToLobbyDto(game);
    }

    private String generateLobbyCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    private LobbyDto convertGameToLobbyDto(Game game) {
        LobbyDto lobbyDto = new LobbyDto();
        lobbyDto.setId(game.getId());
        lobbyDto.setLobbyName(game.getLobbyName());
        lobbyDto.setGameMode(game.getGameMode());
        lobbyDto.setIsPrivate(game.getIsPrivate());
        lobbyDto.setLobbyCode(game.getLobbyCode());
        lobbyDto.setMaxPlayers(game.getMaxPlayers());

        // Get actual current player count from lobby service
        int currentPlayers = lobbyService.getUsers(game.getId()).size();
        lobbyDto.setCurrentPlayers(currentPlayers);

        lobbyDto.setMultiplayerTimer(game.getMultiplayerTimer());
        lobbyDto.setMultiplayerRoundCount(game.getMultiplayerRoundCount());
        lobbyDto.setCreatedAt(game.getCreatedAt());

        // Get actual players from lobby service and convert to DTOs
        List<User> players = lobbyService.getUsers(game.getId());
        lobbyDto.setPlayers(players.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList()));

        return lobbyDto;
    }

    public Integer resolveSingleplayerGame(UUID gameId, UUID userId) {
        Game game = findById(gameId);
        User singleplayerUser = userService.findById(userId);
        game.setWinner(singleplayerUser);
        update(game);
        return roundService.getRoundCountForGame(gameId);
    }

    public HashMap<UUID, Integer> resolveMultiplayerGame(UUID gameId) {
        Game game = findById(gameId);
        HashMap<UUID, Integer> userPoints = getUserPointsForGame(gameId);
        UUID winnerId = findWinner(userPoints);

        User winner = userService.findById(winnerId);
        game.setWinner(winner);
        update(game);

        return userPoints;
    }

    public RankedGameResultDto resolveRankedGame(UUID gameId) {
        Game game = findById(gameId);

        HashMap<UUID, Integer> userPoints = getUserPointsForGame(gameId);
        
        // Check if game has already been resolved (has a winner)
        if (game.getWinner() != null) {
            // Game already resolved, but we still need to return ELO changes
            // Calculate ELO changes based on the existing winner and user points
            Integer averageElo = game.getRankedAverageElo();
            HashMap<UUID, Integer> userEloChanges = calculateEloChangesForResolvedGame(userPoints, game.getWinner().getId(), averageElo);
            
            RankedGameResultDto rankedGameResultDto = new RankedGameResultDto();
            rankedGameResultDto.setEloChanges(userEloChanges);
            rankedGameResultDto.setUserPoints(userPoints);
            return rankedGameResultDto;
        }

        // Game not yet resolved, determine winner and calculate ELO changes
        UUID winnerId = findWinner(userPoints);
        Integer averageElo = game.getRankedAverageElo();

        User winner = userService.findById(winnerId);
        game.setWinner(winner);
        update(game);

        HashMap<UUID, Integer> userEloChanges = updateEloRatings(userPoints, averageElo);
        RankedGameResultDto rankedGameResultDto = new RankedGameResultDto();
        rankedGameResultDto.setEloChanges(userEloChanges);
        rankedGameResultDto.setUserPoints(userPoints);

        return rankedGameResultDto;
    }

    public List<MatchHistoryItem> getUserMatchHistory(UUID userId, Integer limit, Integer offset) {
        int actualLimit = limit != null ? limit : 20;
        int actualOffset = offset != null ? offset : 0;
        int page = actualOffset / actualLimit;

        Page<Game> gamesPage = gameRepository.findGamesByUser(userId, PageRequest.of(page, actualLimit));

        return gamesPage.getContent().stream().map(game -> {
            MatchHistoryItem item = new MatchHistoryItem();
            item.setGameId(game.getId());
            item.setGameMode(game.getGameMode());
            item.setPlayedAt(game.getCreatedAt());
            User winner = game.getWinner();
            
            // Determine if game is actually finished based on rounds completed
            boolean isFinished = false;
            if ("Singleplayer".equalsIgnoreCase(game.getGameMode())) {
                // Singleplayer games are finished if they have a winner
                isFinished = winner != null;
            } else if ("Multiplayer".equalsIgnoreCase(game.getGameMode())) {
                // Multiplayer games are finished if they have a winner AND completed all intended rounds
                Integer currentRounds = roundService.getRoundCountForGame(game.getId());
                Integer intendedRounds = game.getMultiplayerRoundCount();
                isFinished = winner != null && intendedRounds != null && currentRounds >= intendedRounds;
            } else if ("Ranked".equalsIgnoreCase(game.getGameMode())) {
                // Ranked games are finished if they have a winner AND completed all 5 rounds
                Integer currentRounds = roundService.getRoundCountForGame(game.getId());
                isFinished = winner != null && currentRounds >= 5;
            } else {
                // Default: finished if has winner
                isFinished = winner != null;
            }
            
            item.setFinished(isFinished);

            if ("Singleplayer".equalsIgnoreCase(game.getGameMode())) {
                Integer rounds = roundService.getRoundCountForGame(game.getId());
                item.setRoundsSurvived(rounds);
                item.setNumPlayers(null);
            } else {
                item.setRoundsSurvived(null);
                item.setWon(winner != null && winner.getId() != null && winner.getId().equals(userId));
                // number of players = distinct users who guessed in this game's rounds
                int numPlayers = roundService.getUserPointsForGame(game.getId()).size();
                item.setNumPlayers(numPlayers);
            }

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Updates ELO ratings for all players in a ranked game
     * 
     * @param userPoints HashMap containing user IDs and their scores
     * @param averageElo The average ELO of all players in the match
     */
    private HashMap<UUID, Integer> updateEloRatings(HashMap<UUID, Integer> userPoints, Integer averageElo) {
        UUID winnerId = findWinner(userPoints);
        Integer winnerScore = userPoints.get(winnerId);
        HashMap<UUID, Integer> userEloChanges = new HashMap<>();

        for (HashMap.Entry<UUID, Integer> entry : userPoints.entrySet()) {
            UUID userId = entry.getKey();
            Integer userScore = entry.getValue();
            User user = userService.findById(userId);

            // Determine if user won
            boolean won = userId.equals(winnerId);

            // Calculate score difference from winner's score
            Integer scoreDifference = Math.abs(userScore - winnerScore);

            // Calculate ELO change using utility
            Integer eloChange = EloCalculator.calculateEloChange(averageElo, user.getElo(), won, scoreDifference);

            userEloChanges.put(userId, eloChange);

            // Update user's ELO, ensuring it doesn't go below 0
            Integer newElo = user.getElo() + eloChange;
            user.setElo(Math.max(0, newElo));
            userService.update(user);
        }

        return userEloChanges;
    }


//     Calculates ELO changes for a game that has already been resolved
//     This method doesn't update the database, just calculates the changes
    private HashMap<UUID, Integer> calculateEloChangesForResolvedGame(HashMap<UUID, Integer> userPoints, UUID winnerId, Integer averageElo) {
        Integer winnerScore = userPoints.get(winnerId);
        HashMap<UUID, Integer> userEloChanges = new HashMap<>();

        for (HashMap.Entry<UUID, Integer> entry : userPoints.entrySet()) {
            UUID userId = entry.getKey();
            Integer userScore = entry.getValue();
            User user = userService.findById(userId);

            // Determine if user won
            boolean won = userId.equals(winnerId);

            // Calculate score difference from winner's score
            Integer scoreDifference = Math.abs(userScore - winnerScore);

            // Calculate ELO change using utility
            Integer eloChange = EloCalculator.calculateEloChange(averageElo, user.getElo(), won, scoreDifference);

            userEloChanges.put(userId, eloChange);
        }

        return userEloChanges;
    }

    private HashMap<UUID, Integer> getUserPointsForGame(UUID gameId) {
        List<Object[]> userPointsData = roundService.getUserPointsForGame(gameId);
        HashMap<UUID, Integer> userPoints = new HashMap<>();

        for (Object[] row : userPointsData) {
            UUID userId = (UUID) row[0];
            Integer points = ((Number) row[1]).intValue();
            userPoints.put(userId, points);
        }

        return userPoints;
    }

    private UUID findWinner(HashMap<UUID, Integer> userPoints) {
        if (userPoints.isEmpty()) {
            return null;
        }

        UUID winnerId = null;
        Integer maxPoints = Integer.MIN_VALUE;

        for (HashMap.Entry<UUID, Integer> entry : userPoints.entrySet()) {
            if (entry.getValue() > maxPoints) {
                maxPoints = entry.getValue();
                winnerId = entry.getKey();
            }
        }

        return winnerId;
    }

    public Integer getCurrentSingleplayerScore(UUID gameId, UUID userId) {
        return PointsCalculator.getCurrentSingleplayerScore(gameId, userId, roundRepository);
    }

    public SingleplayerGameState getSingleplayerGameState(UUID gameId, UUID userId) {
        Integer currentScore = getCurrentSingleplayerScore(gameId, userId);
        Integer roundsCompleted = roundService.getRoundCountForGame(gameId);
        boolean shouldEnd = shouldEndSingleplayerGame(gameId, userId, roundRepository);

        Game game = findById(gameId);
        boolean isGameEnded = game.getWinner() != null;

        return new SingleplayerGameState(gameId, currentScore, roundsCompleted, shouldEnd, isGameEnded);
    }

    public void update(Game game) {
        gameRepository.save(game);
    }

    public Game findById(UUID id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + id));
    }

    public int cleanupExpiredGames() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(2); // 2 hours before now
        List<Game> oldGames = gameRepository.findByWinnerIsNullAndCreatedAtBefore(cutoff);
        
        List<Game> gamesToDelete = new ArrayList<>();
        
        // Check each old game to see if it should be deleted
        for (Game game : oldGames) {
            String gameMode = game.getGameMode();
            Integer currentRounds = roundService.getRoundCountForGame(game.getId());
            boolean shouldDelete = false;
            String reason = "";
            
            if ("Ranked".equalsIgnoreCase(gameMode)) {
                // Ranked games should have 5 rounds
                if (currentRounds < 5) {
                    shouldDelete = true;
                    reason = "Ranked game with only " + currentRounds + " rounds (required: 5)";
                }
            } else if ("Multiplayer".equalsIgnoreCase(gameMode)) {
                // Multiplayer games should have the specified number of rounds
                Integer requiredRounds = game.getMultiplayerRoundCount();
                if (requiredRounds != null && currentRounds < requiredRounds) {
                    shouldDelete = true;
                    reason = "Multiplayer game with only " + currentRounds + " rounds (required: " + requiredRounds + ")";
                }
            } else {
                // For singleplayer games or any other game mode, delete if they're old and unfinished
                shouldDelete = true;
                reason = "Old unfinished " + gameMode + " game";
            }
            
            if (shouldDelete) {
                gamesToDelete.add(game);
                System.out.println("Cleaning up game " + game.getId() + " (" + gameMode + "): " + reason);
            }
        }

        if (!gamesToDelete.isEmpty()) {
            gameRepository.deleteAll(gamesToDelete);
            System.out.println("Cleaned up " + gamesToDelete.size() + " expired games");
        }

        return gamesToDelete.size();
    }
}
