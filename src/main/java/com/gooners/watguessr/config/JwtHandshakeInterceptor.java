//package com.gooners.watguessr.config;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import java.util.Map;
//
//public class JwtHandshakeInterceptor implements HandshakeInterceptor {
//
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request,
//                                   ServerHttpResponse response,
//                                   WebSocketHandler wsHandler,
//                                   Map<String, Object> attributes) throws Exception {
//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            String token = servletRequest.getServletRequest().getParameter("token");
//
//            if (token != null && JwtUtils.validateToken(token)) {
//                String username = JwtUtils.getUsernameFromToken(token);
//                attributes.put("username", username);
//                return true;
//            }
//        }
//
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        return false;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request,
//                               ServerHttpResponse response,
//                               WebSocketHandler wsHandler,
//                               Exception exception) {
//        // no-op
//    }
//}
