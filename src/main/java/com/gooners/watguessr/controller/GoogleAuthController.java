package com.gooners.watguessr.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.AuthenticationService;
import com.gooners.watguessr.service.UserService;

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
                .queryParam("prompt", "select_account")
                .build(true).toUriString();

        res.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (error != null) {
            response.sendRedirect(frontendBaseUrl + "?error=oauth_cancelled");
            return;
        }

        // verify state
        String sessionState = (String) session.getAttribute("oauth_state");
        if (sessionState == null || !sessionState.equals(state)) {
            response.sendRedirect(frontendBaseUrl + "?error=invalid_state");
            return;
        }

        try {
            // Exchange code for access token
            String tokenResponse = exchangeCodeForToken(code);
            JsonNode tokenData = objectMapper.readTree(tokenResponse);
            String accessToken = tokenData.get("access_token").asText();

            // Fetch user info
            String userInfoResponse = getUserInfo(accessToken);
            JsonNode userInfo = objectMapper.readTree(userInfoResponse);

            String email = userInfo.get("email").asText();
            String name = userInfo.get("name").asText();
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            // Check if user exists before creating/getting
            boolean isNewUser = !userService.existsByEmail(email);
            
            // Persist / authenticate user
            User user = userService.createOrGetUserFromGoogle(email, name, picture);
            authenticationService.authenticateGoogleUser(user, response);

            // Redirect frontend with new_user flag
            String redirectParams = String.format(
                    "?google_auth=true&email=%s&name=%s&picture=%s&login=success&new_user=%s",
                    URLEncoder.encode(email, StandardCharsets.UTF_8),
                    URLEncoder.encode(name, StandardCharsets.UTF_8),
                    picture != null ? URLEncoder.encode(picture, StandardCharsets.UTF_8) : "",
                    isNewUser ? "true" : "false");

            response.sendRedirect(frontendBaseUrl + redirectParams);

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
