package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.AuthenticationResponseDto;
import com.gooners.watguessr.dto.UserLoginDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

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
    public AuthenticationResponseDto authenticate(final UserLoginDto request) {
        // First, perform all the security checks that were in UserController.login()
        User user = userService.login(request.getUsername(), request.getPassword());
        
        // If we get here, all checks passed (user found, verified, password correct)
        // Now authenticate with Spring Security
        final var authToken = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getUsername(), request.getPassword());

        final var authentication = authenticationManager
                .authenticate(authToken);

        // Generate JWT token
        final var token = jwtService.generateToken(authentication);
        
        // Return token and user info
        return new AuthenticationResponseDto(token, userMapper.toDto(user));
    }
}