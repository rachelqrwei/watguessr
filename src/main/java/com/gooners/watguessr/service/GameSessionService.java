package com.gooners.watguessr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    // Store game sessions: userId -> gameId
    private final Map<String, UUID> userGameSessions = new ConcurrentHashMap<>();
    // Store session tokens: token -> userId
    private final Map<String, String> sessionTokens = new ConcurrentHashMap<>();
    // Store session creation times for expiration
    private final Map<String, Instant> sessionCreationTimes = new ConcurrentHashMap<>();
    
    private final JwtDecoder jwtDecoder;
    
    @Value("${game.session.expiration.hours:24}")
    private long sessionExpirationHours;
    
    public GameSessionService(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    
    public void createGameSession(String userId, UUID gameId) {
        userGameSessions.put(userId, gameId);
        
        // Generate a session token for reconnection
        String token = UUID.randomUUID().toString();
        sessionTokens.put(token, userId);
        sessionCreationTimes.put(token, Instant.now());
    }
    
    public UUID getGameIdForUser(String userId) {
        return userGameSessions.get(userId);
    }
    
    public String getUserIdForToken(String token) {
        return sessionTokens.get(token);
    }
    
    public void removeGameSession(String userId) {
        UUID gameId = userGameSessions.remove(userId);
        // Also remove any session tokens for this user
        sessionTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(userId)) {
                sessionCreationTimes.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    public boolean canRejoinGame(String userId, UUID gameId) {
        UUID storedGameId = userGameSessions.get(userId);
        return storedGameId != null && storedGameId.equals(gameId);
    }
    
    public String getSessionTokenForUser(String userId) {
        return sessionTokens.entrySet().stream()
            .filter(entry -> entry.getValue().equals(userId))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
    
    public boolean validateReconnection(String userId, UUID gameId, String token) {
        // Check if the token is valid and matches the user
        String tokenUserId = getUserIdForToken(token);
        if (tokenUserId == null || !tokenUserId.equals(userId)) {
            return false;
        }
        
        // Check if the user was originally in this game
        UUID storedGameId = getGameIdForUser(userId);
        if (storedGameId == null || !storedGameId.equals(gameId)) {
            return false;
        }
        
        // Check if the token is not expired
        Instant creationTime = sessionCreationTimes.get(token);
        if (creationTime == null || creationTime.isBefore(Instant.now().minusSeconds(sessionExpirationHours * 3600))) {
            // Token expired, remove it
            sessionTokens.remove(token);
            sessionCreationTimes.remove(token);
            return false;
        }
        
        return true;
    }
    
    public boolean validateJwtToken(String jwtToken, String expectedUserId) {
        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            
            // Check if token is not expired
            if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now())) {
                return false;
            }
            
            // Check if the subject (user ID) matches
            String subject = jwt.getSubject();
            return subject != null && subject.equals(expectedUserId);
            
        } catch (JwtException e) {
            return false;
        }
    }
    
    public void invalidateSession(String userId) {
        // Remove all session data for a user
        userGameSessions.remove(userId);
        sessionTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(userId)) {
                sessionCreationTimes.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    public boolean isSessionValid(String userId, UUID gameId) {
        UUID storedGameId = getGameIdForUser(userId);
        return storedGameId != null && storedGameId.equals(gameId);
    }
}
