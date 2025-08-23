package com.gooners.watguessr.controller;

import java.io.IOException;

import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.AuthenticationService;
import com.gooners.watguessr.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooners.watguessr.config.RateLimit;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("api/auth/google")
public class GoogleAuthController {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String frontendBaseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public GoogleAuthController(
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret,
            @Value("${oauth.google.redirect-uri}") String redirectUri,
            @Value("${frontend.base-url}") String frontendBaseUrl,
            UserService userService,
            AuthenticationService authenticationService) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.frontendBaseUrl = frontendBaseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/start")
    @RateLimit(requests = 10, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many OAuth start requests.")
    public void start(HttpServletResponse res, HttpSession session) throws IOException {
        var state = randomUrlSafe();
        var nonce = randomUrlSafe();
        session.setAttribute("oauth_state", state);
        session.setAttribute("oauth_nonce", nonce);

        String encodedScope = java.net.URLEncoder.encode("openid email profile", "UTF-8");

        var authUrl = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", encodedScope)
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .build(true).toUriString();

        res.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    @RateLimit(requests = 15, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many OAuth callback requests.")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        // Check for OAuth errors
        if (error != null) {
            response.sendRedirect(frontendBaseUrl + "?error=oauth_cancelled");
            return;
        }

        // Verify state parameter
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState == null || !sessionState.equals(state)) {
            response.sendRedirect(frontendBaseUrl + "?error=invalid_state");
            return;
        }

        try {
            // Exchange authorization code for access token
            String tokenResponse = exchangeCodeForToken(code);
            JsonNode tokenData = objectMapper.readTree(tokenResponse);
            String accessToken = tokenData.get("access_token").asText();

            // Get user info from Google
            String userInfoResponse = getUserInfo(accessToken);
            JsonNode userInfo = objectMapper.readTree(userInfoResponse);

            // Extract user details
            String email = userInfo.get("email").asText();
            String name = userInfo.get("name").asText();
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            // Create or get user and authenticate them
            User user = userService.createOrGetUserFromGoogle(email, name, picture);
            UserDto userDto = authenticationService.authenticateGoogleUser(user, response);

            // Redirect to frontend with success and user data
            String userData = String.format(
                    "?google_auth=true&email=%s&name=%s&picture=%s&login=success",
                    java.net.URLEncoder.encode(email, "UTF-8"),
                    java.net.URLEncoder.encode(name, "UTF-8"),
                    picture != null ? java.net.URLEncoder.encode(picture, "UTF-8") : "");

            response.sendRedirect(frontendBaseUrl + userData);

        } catch (Exception e) {
            response.sendRedirect(frontendBaseUrl + "?error=auth_failed");
        }
    }

    @GetMapping("/google-login")
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many Google login requests.")
    public void googleLogin(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response,
            HttpSession session) throws IOException {

        // Step 1: Handle OAuth errors
        if (error != null) {
            response.sendRedirect(frontendBaseUrl + "?error=oauth_cancelled");
            return;
        }

        // Step 2: If code is not present, redirect to Google OAuth start URL
        if (code == null) {
            var oauthState = randomUrlSafe();
            var oauthNonce = randomUrlSafe();
            session.setAttribute("oauth_state", oauthState);
            session.setAttribute("oauth_nonce", oauthNonce);


            String scope = java.net.URLEncoder.encode("openid email profile", "UTF-8");

            String authUrl = UriComponentsBuilder
                    .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                    .queryParam("client_id", clientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", scope)
                    .queryParam("state", oauthState)
                    .queryParam("nonce", oauthNonce)
                    .build(true).toUriString();

            response.sendRedirect(authUrl);
            return;
        }

        // Step 3: Verify state
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState == null || !sessionState.equals(state)) {
            response.sendRedirect(frontendBaseUrl + "?error=invalid_state");
            return;
        }

        try {
            // Exchange code for access token
            String tokenResponse = exchangeCodeForToken(code);
            JsonNode tokenData = new ObjectMapper().readTree(tokenResponse);
            String accessToken = tokenData.get("access_token").asText();

            // Get user info from Google
            String userInfoResponse = getUserInfo(accessToken);
            JsonNode userInfo = new ObjectMapper().readTree(userInfoResponse);

            String email = userInfo.get("email").asText();
            String name = userInfo.get("name").asText();
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            // Create or get user
            User user = userService.createOrGetUserFromGoogle(email, name, picture);

            // Authenticate user and set JWT cookie
            UserDto userDto = authenticationService.authenticateGoogleUser(user, response);

            // Redirect to frontend with success
            String redirectData = String.format(
                    "?google_auth=true&email=%s&name=%s&picture=%s&login=success",
                    java.net.URLEncoder.encode(email, "UTF-8"),
                    java.net.URLEncoder.encode(name, "UTF-8"),
                    picture != null ? java.net.URLEncoder.encode(picture, "UTF-8") : "");

            response.sendRedirect(frontendBaseUrl + redirectData);

        } catch (Exception e) {
            response.sendRedirect(frontendBaseUrl + "?error=auth_failed");
        }
    }

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                request,
                String.class);

        return response.getBody();
    }

    private String getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                request,
                String.class);

        return response.getBody();
    }

    private static String randomUrlSafe() {
        var bytes = new byte[24];
        new java.security.SecureRandom().nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
