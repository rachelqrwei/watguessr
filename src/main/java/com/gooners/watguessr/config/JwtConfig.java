package com.gooners.watguessr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
public class JwtConfig {

    @Value("${jwt.private-key}")
    private String privateKeyPath;

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    @Bean
    public RSAPrivateKey privateKey() throws IOException {
        try {
            // Remove 'classpath:' prefix if present
            String resourcePath = privateKeyPath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(resourcePath);
            
            if (!resource.exists()) {
                throw new RuntimeException("Private key file not found: " + resourcePath);
            }
            
            String privateKeyPEM = new String(resource.getInputStream().readAllBytes());
            
            // Remove PEM headers and whitespace
            privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key from " + privateKeyPath, e);
        }
    }

    @Bean
    public RSAPublicKey publicKey() throws IOException {
        try {
            // Remove 'classpath:' prefix if present
            String resourcePath = publicKeyPath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(resourcePath);
            
            if (!resource.exists()) {
                throw new RuntimeException("Public key file not found: " + resourcePath);
            }
            
            String publicKeyPEM = new String(resource.getInputStream().readAllBytes());
            
            // Remove PEM headers and whitespace
            publicKeyPEM = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key from " + publicKeyPath, e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        final var jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("watguessr-jwt-key") // Add a key ID for better JWT management
                .build();

        return new NimbusJwtEncoder(
                new ImmutableJWKSet(new JWKSet(jwk)));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
