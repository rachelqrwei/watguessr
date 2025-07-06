//package com.gooners.watguessr.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    @Autowired
//    private ChatHandler chatHandler;
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(chatHandler, "/ws/chat")
//                .addInterceptors(new JwtHandshakeInterceptor()) // spring security
//                .setAllowedOrigins("*"); // allow all origins (for dev)
//    }
//}
