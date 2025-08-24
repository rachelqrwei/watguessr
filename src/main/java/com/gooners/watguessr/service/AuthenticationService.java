package com.gooners.watguessr.service;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserLoginDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;

    public AuthenticationService(AuthenticationManager authenticationManager, UserService userService, UserMapper userMapper, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    // returns token and user info after all security checks
    public UserDto authenticate(final UserLoginDto request, HttpServletResponse response) {
        // 1️⃣ Perform all security checks
        User user = userService.login(request.getUsername(), request.getPassword());

        // 2️⃣ Authenticate with Spring Security
        var authToken = UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword());
        var authentication = authenticationManager.authenticate(authToken);

        // 3️⃣ Generate JWT token
        String token = jwtService.generateToken(authentication);

        // 4️⃣ Set JWT as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)           // use true for HTTPS in production
                .path("/")
                .maxAge(3600)           // 1 hour
                .sameSite("None")       // Allow cross-site requests for CORS
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // 5️⃣ Return user info only (no token in JSON)
        return userMapper.toDto(user);
    }

    // Authenticate Google OAuth user
    public UserDto authenticateGoogleUser(User user, HttpServletResponse response) {
        // Update last login and streak
        userService.updateStreakAndLastLogin(user);
        
        // Generate JWT token for the user
        String token = jwtService.generateTokenForUser(user);
        
        // Set JWT as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)          // use true in production with HTTPS
                .path("/")
                .maxAge(3600)           // 1 hour
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        
        // Return user info
        return userMapper.toDto(user);
    }

}
