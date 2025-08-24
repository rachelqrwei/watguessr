package com.gooners.watguessr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
				.setAllowedOrigins(
					"https://watguessr-frontend-x2gln.ondigitalocean.app", // Production frontend
					"https://watguessr.io", // Custom domain frontend
					"http://localhost:5173", // Local development
					"http://localhost:3000"  // Alternative local development port
				)
				.withSockJS();
		registry.addEndpoint("/ws-matchmaking") // WebSocket endpoint
				.setAllowedOrigins(
					"https://watguessr-frontend-x2gln.ondigitalocean.app", // Production frontend
					"https://watguessr.io", // Custom domain frontend
					"http://localhost:5173", // Local development
					"http://localhost:3000"  // Alternative local development port
				)
				.withSockJS();
	}
}
