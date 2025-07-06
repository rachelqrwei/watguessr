//package com.gooners.watguessr.config;
//
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//
//import java.security.Key;
//import java.util.Date;
//
//public class JwtUtils {
//    private static final String SECRET_KEY = "e9c4d1a39fb3ad8f9d7hj4j6r8g9ga3c9d0n689su0bd7bs1n908ge8be8f7\n";
//    private static final long EXPIRATION_TIME = 864_000_000;
//
//    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//
//    public static String generateToken(String username) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public static boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    public static String getUsernameFromToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//}
