package com.gooners.watguessr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.profile:}")
    private String profile;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        if (accessKeyId != null && !accessKeyId.trim().isEmpty() && 
            secretAccessKey != null && !secretAccessKey.trim().isEmpty()) {
            // Use environment variables (production setup)
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        } else if (profile != null && !profile.trim().isEmpty()) {
            // Use profile (local development)
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(ProfileCredentialsProvider.create(profile))
                    .build();
        } else {
            // Fallback to default credentials provider
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
    }
}
