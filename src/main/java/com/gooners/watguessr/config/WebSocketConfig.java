package com.gooners.watguessr.config;

import com.gooners.watguessr.dto.MultiplayerGameStateDto;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.gooners.watguessr.service.MultiplayerGameStateService;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic"); // Client subscribes here
		config.setApplicationDestinationPrefixes("/app"); // Client sends here
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-game") // WebSocket endpoint
				.setAllowedOriginPatterns("*") // allow CORS for Vue dev server
				.withSockJS();
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setMessageSizeLimit(64 * 1024); // 64KB
		registration.setSendBufferSizeLimit(512 * 1024); // 512KB
		registration.setSendTimeLimit(20000); // 20 seconds
	}
}

@Component
class WebSocketEventListener {
	
	@Autowired
	private MultiplayerGameStateService multiplayerGameStateService;

	@Autowired
	private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
	
	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

		// Extract user information from the session attributes
		Map<String, Object> sessionAttributes = sha.getSessionAttributes();
		if (sessionAttributes != null) {
			String userId = (String) sessionAttributes.get("userId");
			UUID gameId = (UUID) sessionAttributes.get("gameId"); // make sure you store this in session

			if (userId != null && gameId != null) {
				System.out.println("🔌 WebSocket disconnected for user: " + userId);

				try {
					multiplayerGameStateService.removeUserSession(userId);

					// Now broadcast the updated game state
					MultiplayerGameStateDto updatedGameState = multiplayerGameStateService.getGameState(gameId);
					messagingTemplate.convertAndSend(
							"/topic/game/" + gameId + "/state",
							updatedGameState
					);

				} catch (Exception e) {
					System.err.println("❌ Failed to handle player disconnection: " + e.getMessage());
				}
			}
		}
	}
}
