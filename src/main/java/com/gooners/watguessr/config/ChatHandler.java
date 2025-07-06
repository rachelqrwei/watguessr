//package com.gooners.watguessr.config;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import java.io.IOException;
//
//@Component
//public class ChatHandler extends TextWebSocketHandler {
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        System.out.println("Client connected: " + session.getId());
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
//        String payload = message.getPayload();
//        System.out.println("Received: " + payload);
//        session.sendMessage(new TextMessage("Echo: " + payload));
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
//        System.out.println("Client disconnected: " + session.getId());
//    }
//}
