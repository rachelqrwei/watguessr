package com.gooners.watguessr.service;

import com.gooners.watguessr.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.gooners.watguessr.repository.EmailVerificationRepository;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    public JpaUserDetailsService(UserRepository userRepository, EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {


        return userRepository.findByUsername(username)
                .map(user -> {
                    // Check if user has verified email verification
                    boolean isEmailVerified = emailVerificationRepository
                            .findFirstVerifiedByEmail(user.getEmailAddress())
                            .isPresent();

                    if (!isEmailVerified) {
                        throw new UsernameNotFoundException(
                                "User with username [%s] has not verified their email".formatted(username));
                    }

                    // User is verified, grant USER authority
                    return User.builder()
                            .username(username)
                            .password(user.getPassword())
                            .authorities("ROLE_USER") // Use ROLE_ prefix for proper Spring Security integration
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(false)
                            .build();// the above false are necessary to build

                })
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with username [%s] not found".formatted(username)));
    }
}