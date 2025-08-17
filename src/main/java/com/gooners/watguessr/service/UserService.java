package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.LeaderboardMapper;
import com.gooners.watguessr.repository.EmailVerificationRepository;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.UserRepository;
import com.gooners.watguessr.utils.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final LeaderboardMapper leaderboardMapper;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;

    public UserService(UserRepository userRepository, LeaderboardMapper leaderboardMapper, GameRepository gameRepository, PasswordEncoder passwordEncoder, EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.leaderboardMapper = leaderboardMapper;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationRepository = emailVerificationRepository;
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

    public void signup(UserSignupDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new CustomException("Username already exists");
        }

        if (userRepository.existsByEmailAddress(dto.getEmail())) {
            throw new CustomException("Email already exists");
        }

        if (dto.getUsername().length() < 8) {
            throw new CustomException("Username must be at least 8 characters");
        }

        if (!isValidPassword(dto.getPassword())) {
            throw new CustomException("Password does not meet criteria");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User(dto.getEmail(), dto.getUsername(), hashedPassword);
        userRepository.save(user);
    }

    public User login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!emailVerificationRepository.findFirstVerifiedByEmail(user.getEmailAddress()).isPresent()) {
            throw new CustomException("User not verified");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException("Invalid password");
        }
        updateStreakAndLastLogin(user);
        return user;
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$");
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

    public LeaderboardUser getLeaderboardUserById(UUID userId) {
        User user = findById(userId);
        return convertToLeaderboardUser(user);
    }

    private LeaderboardUser convertToLeaderboardUser(User user) {
        LeaderboardUser leaderboardUser = leaderboardMapper.toLeaderboardUser(user);

        Integer gamesPlayed = gameRepository.countNonSingleplayerGamesPlayedByUser(user.getId());
        Integer gamesWon = gameRepository.countGamesWonByUser(user.getId());

        gamesPlayed = gamesPlayed != null ? gamesPlayed : 0;
        gamesWon = gamesWon != null ? gamesWon : 0;

        leaderboardUser.setGamesPlayed(gamesPlayed);
        leaderboardUser.setGamesWon(gamesWon);
        leaderboardUser.setGamesLost(gamesPlayed - gamesWon);

        return leaderboardUser;
    }

    public void updateStreakAndLastLogin(User user) {
        LocalDate today = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        LocalDate lastLogin = user.getLastLoginAt().toLocalDate();

        if (lastLogin.equals(today.minusDays(1))) {
            user.setStreak(user.getStreak() + 1);
        } else if (!lastLogin.equals(today)) {
            user.setStreak(1);
        }

        user.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

}
