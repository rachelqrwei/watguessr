package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.LeaderboardMapper;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final LeaderboardMapper leaderboardMapper;
    private final GameRepository gameRepository;

    public UserService(UserRepository userRepository, LeaderboardMapper leaderboardMapper, GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.leaderboardMapper = leaderboardMapper;
        this.gameRepository = gameRepository;
    }

    public void update(User user) {
        userRepository.save(user);
    }

    public void create(User user) {
        if (!userRepository.existsByEmailAddress(user.getEmailAddress())
                && !userRepository.existsByUsername(user.getUsername())) {
            user.setCreatedAt(OffsetDateTime.now());
            userRepository.save(user);
        } else
            throw new RuntimeException("Username or Email already exists");
    }

    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    public List<User> findSorted(String keyword, String sortBy, int page, int pageSize) {
        return userRepository.findSorted(keyword, sortBy, PageRequest.of(page, pageSize));
    }

    public QueryResults<LeaderboardUser> getLeaderboard(String searchTerm, String sortBy, Integer limit, Integer offset) {
        String actualSortBy = sortBy != null ? sortBy : "elo";
        int actualLimit = limit != null ? limit : 20;
        int actualOffset = offset != null ? offset : 0;
        
        int page = actualOffset / actualLimit;

        List<User> users = userRepository.findSorted(searchTerm, actualSortBy, PageRequest.of(page, actualLimit));
        
        List<LeaderboardUser> leaderboardUsers = users.stream()
                .map(this::convertToLeaderboardUser)
                .collect(Collectors.toList());

        switch (actualSortBy) {
            case "gamesWonDesc" -> leaderboardUsers.sort((a, b) -> Integer.compare(b.getGamesWon(), a.getGamesWon()));
            case "gamesPlayedDesc" ->
                    leaderboardUsers.sort((a, b) -> Integer.compare(b.getGamesPlayed(), a.getGamesPlayed()));
            case "gamesLostDesc" ->
                    leaderboardUsers.sort((a, b) -> Integer.compare(b.getGamesLost(), a.getGamesLost()));
            case "winRateDesc" -> leaderboardUsers.sort((a, b) -> {
                double winRateA = a.getGamesPlayed() > 0 ? (double) a.getGamesWon() / a.getGamesPlayed() : 0;
                double winRateB = b.getGamesPlayed() > 0 ? (double) b.getGamesWon() / b.getGamesPlayed() : 0;
                return Double.compare(winRateB, winRateA);
            });
            case "winRateAsc" -> leaderboardUsers.sort((a, b) -> {
                double winRateA = a.getGamesPlayed() > 0 ? (double) a.getGamesWon() / a.getGamesPlayed() : 0;
                double winRateB = b.getGamesPlayed() > 0 ? (double) b.getGamesWon() / b.getGamesPlayed() : 0;
                return Double.compare(winRateA, winRateB);
            });
        }
        
        QueryResults<LeaderboardUser> queryResults = new QueryResults<>();
        queryResults.setResults(leaderboardUsers);
        
        return queryResults;
    }
    
    private LeaderboardUser convertToLeaderboardUser(User user) {
        LeaderboardUser leaderboardUser = leaderboardMapper.toLeaderboardUser(user);
        
        Integer gamesPlayed = gameRepository.countGamesPlayedByUser(user.getId());
        Integer gamesWon = gameRepository.countGamesWonByUser(user.getId());
        
        gamesPlayed = gamesPlayed != null ? gamesPlayed : 0;
        gamesWon = gamesWon != null ? gamesWon : 0;
        
        leaderboardUser.setGamesPlayed(gamesPlayed);
        leaderboardUser.setGamesWon(gamesWon);
        leaderboardUser.setGamesLost(gamesPlayed - gamesWon);
        
        return leaderboardUser;
    }

    public void clearSession(){

    }

    public void clearToken() {

    }

    public void JWTTokenValidate() {

    }

    // wip
    public void getMatchHistory() {

    }

}
