package com.gooners.watguessr.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.entity.EmailVerification;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.LeaderboardMapper;
import com.gooners.watguessr.repository.EmailVerificationRepository;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.repository.GuessRepository;
import com.gooners.watguessr.repository.UserRepository;
import com.gooners.watguessr.utils.CustomException;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final LeaderboardMapper leaderboardMapper;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailVerificationService emailVerificationService;
    private final GuessRepository guessRepository;

    public UserService(UserRepository userRepository, LeaderboardMapper leaderboardMapper, GameRepository gameRepository, PasswordEncoder passwordEncoder, EmailVerificationRepository emailVerificationRepository, EmailVerificationService emailVerificationService, GuessRepository guessRepository) {
        this.userRepository = userRepository;
        this.leaderboardMapper = leaderboardMapper;
        this.gameRepository = gameRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailVerificationService = emailVerificationService;
        this.guessRepository = guessRepository;
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
        if (userRepository.existsByUsername(dto.getUsername().toLowerCase())) {
            throw new CustomException("Username already exists");
        }

        if (userRepository.existsByEmailAddress(dto.getEmail())) {
            throw new CustomException("Email already exists");
        }

        // username and password check
        isValidUsername(dto.getUsername());
        isValidPassword(dto.getPassword());

        //        -when saving the email, normalize to lowercase (so email@gmail.com is the same as Email@gmail.com, etc)
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User(dto.getEmail(), dto.getUsername(), hashedPassword);
        userRepository.save(user);
        
        // Automatically send OTP email after signup
        try {
            emailVerificationService.prepareToSendEmail(dto.getEmail(), dto.getUsername());
        } catch (Exception e) {
            // Log the error but don't fail the signup
            System.err.println("Failed to send OTP email after signup: " + e.getMessage());
        }
    }

    public User login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found"));

        if (!emailVerificationRepository.findFirstVerifiedByEmail(user.getEmailAddress()).isPresent() &&
                !userRepository.findFirstByEmailAddressAndVerifiedTrue(user.getEmailAddress()).isPresent()
        ) {
            throw new CustomException("User not verified");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException("Invalid password");
        }
        updateStreakAndLastLogin(user);
        return user;
    }

    public void isValidPassword(String password) {
        if (!(password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$"))) {
            throw new CustomException("Password does not meet criteria");
        }
    }

    public void isValidUsername(String username) {
        if (username.length() < 3) {
            throw new CustomException("Username must be at least 3 characters");
        }

        if (username.length() > 24) {
            throw new CustomException("Username must be at most 24 characters");
        }

        if (username.contains(" ")) {
            throw new CustomException("Username cannot contain spaces");
        }
    }

    public QueryResults<LeaderboardUser> getLeaderboard(String searchTerm, String sortBy, Integer limit, Integer offset) {
        String actualSortBy = sortBy != null ? sortBy : "elo";
        int actualLimit = limit != null ? limit : 5;
        int actualOffset = offset != null ? offset : 0;

        int page = actualOffset / actualLimit;

        List<Object[]> results = userRepository.findSortedWithGameStats(searchTerm, actualSortBy, PageRequest.of(page, actualLimit));

        List<LeaderboardUser> leaderboardUsers = results.stream()
                .map(result -> {
                    User user = (User) result[0];
                    Long gamesWon = (Long) result[1];
                    Long gamesLost = (Long) result[2];
                    Long gamesPlayed = (Long) result[3];
                    
                    LeaderboardUser leaderboardUser = leaderboardMapper.toLeaderboardUser(user);
                    leaderboardUser.setGamesWon(gamesWon != null ? gamesWon.intValue() : 0);
                    leaderboardUser.setGamesLost(gamesLost != null ? gamesLost.intValue() : 0);
                    leaderboardUser.setGamesPlayed(gamesPlayed != null ? gamesPlayed.intValue() : 0);
                    
                    return leaderboardUser;
                })
                .collect(Collectors.toList());

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

        // Get total games played (all modes)
        Integer totalGamesPlayed = gameRepository.countGamesPlayedByUser(user.getId());
        
        // Get ranked-only statistics for winrate calculation
        Integer rankedGamesWon = gameRepository.countRankedGamesWonByUser(user.getId());
        Integer rankedGamesLost = gameRepository.countRankedGamesLostByUser(user.getId());

        totalGamesPlayed = totalGamesPlayed != null ? totalGamesPlayed : 0;
        rankedGamesWon = rankedGamesWon != null ? rankedGamesWon : 0;
        rankedGamesLost = rankedGamesLost != null ? rankedGamesLost : 0;

        leaderboardUser.setGamesPlayed(totalGamesPlayed);
        leaderboardUser.setGamesWon(rankedGamesWon);
        leaderboardUser.setGamesLost(rankedGamesLost);

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

    public User createUserFromGoogle(String email, String name, String picture) {
        // Check if user already exists
        if (userRepository.existsByEmailAddress(email)) {
            throw new CustomException("An account with this email already exists");
        }

        // Generate a unique username from the name
        String baseUsername = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        // Create user with Google credentials (no password needed for OAuth)
        User user = new User();
        user.setEmailAddress(email);
        user.setUsername(username);
        user.setUsername(name);
        user.setProfilePictureUrl(picture);
        user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.setElo(150);
        user.setStreak(1);
        user.setVerified(true); // Google users are pre-verified
        
        // Set a random password (won't be used for OAuth login)
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        
        userRepository.save(user);
        return user;
    }

    public User createOrGetUserFromGoogle(String email, String name, String picture) {
        // Check if user already exists by email
        User existingUser = userRepository.findByEmailAddress(email);
        if (existingUser != null) {
            // Return existing user
            return existingUser;
        }

        // Generate a unique username from the name
        String baseUsername = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        // Create new user with Google credentials
        User user = new User();
        user.setEmailAddress(email);
        user.setUsername(username);
        user.setProfilePictureUrl(picture);
        user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        user.setElo(150);
        user.setStreak(1);
        user.setVerified(true); // Google users are pre-verified

        // Set a random password (won't be used for OAuth login)
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        userRepository.save(user);
        return user;
    }

    public void changePassword(String emailAddress, String newPassword) {
        User user = userRepository.findByEmailAddress(emailAddress);
        if (user == null) {
            throw new CustomException("User not found with email: " + emailAddress);
        }
        // password check
        isValidPassword(newPassword);

        try {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } catch (Exception e) {
            throw new CustomException("Failed to change passWord user: " + e.getMessage());
        }

    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAddress(email);
    }

    @Transactional
    public void deleteUser(String emailAddress) {
        User user = userRepository.findByEmailAddress(emailAddress);
        if (user == null) {
            throw new CustomException("User not found with email: " + emailAddress);
        }

        try {
            UUID userId = user.getId();
            
            // Step 1: Delete all guesses made by the user
            List<Guess> userGuesses = guessRepository.findAllByUserId(userId);
            if (!userGuesses.isEmpty()) {
                guessRepository.deleteAllByUserId(userId);
                System.out.println("Deleted " + userGuesses.size() + " guesses for user: " + userId);
            }

            // Step 2: Clear winner references in games where this user was the winner
            List<Game> gamesWon = gameRepository.findGamesWonByUser(userId);
            if (!gamesWon.isEmpty()) {
                gameRepository.clearWinnerForUser(userId);
                System.out.println("Cleared winner reference in " + gamesWon.size() + " games for user: " + userId);
            }

            // Step 3: Delete email verification records
            Optional<EmailVerification> evOptional = emailVerificationRepository.findFirstVerifiedByEmail(user.getEmailAddress());
            if (evOptional.isPresent()) {
                EmailVerification ev = evOptional.get();
                emailVerificationRepository.delete(ev);
                System.out.println("Deleted email verification record for user: " + userId);
            }

            // Step 4: Finally delete the user
            userRepository.delete(user);
            System.out.println("Successfully deleted user: " + userId);
            
        } catch (Exception e) {
            throw new CustomException("Failed to delete user: " + e.getMessage());
        }
    }

    public void changeUsername(String emailAddress, String newUsername) {
        User user = userRepository.findByEmailAddress(emailAddress);

        // username check
        isValidUsername(user.getUsername());

        if (user.getUsernameChangedAt() == null || user.getUsernameChangedAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC).minusDays(7))){
            user.setUsername(newUsername);
            user.setUsernameChangedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userRepository.save(user);
        } else {
            throw new CustomException("You can only change your username once every 7 days. Last changed at " + user.getUsernameChangedAt());
        }
    }
}
