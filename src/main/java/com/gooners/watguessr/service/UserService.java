package com.gooners.watguessr.service;

import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.UserRepository;
import com.gooners.watguessr.utils.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void update(User user) {
        userRepository.save(user);
    }

    public void create(User user) {
        if (!userRepository.existsByEmailAddress(user.getEmailAddress())
                && !userRepository.existsByUsername(user.getUsername())) {
            user.setCreatedAt(OffsetDateTime.now());
            userRepository.save(user);
        } else
            throw new RuntimeException("Username or Email already exists");
    }

    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    public List<User> findSorted(String keyword, String sortBy, int page, int pageSize) {
        return userRepository.findSorted(keyword, sortBy, PageRequest.of(page, pageSize));
    }

    public void signup(UserSignupDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new CustomException("Username already exists");
        }

        if (dto.getUsername().length() < 8) {
            throw new CustomException("Username must be at least 8 characters");
        }

        if (!isValidPassword(dto.getPassword())) {
            throw new CustomException("Password does not meet criteria");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = new User(dto.getEmail(), dto.getUsername(), hashedPassword);
        userRepository.save(user);
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$");
    }

    public void clearSession(){

    }

    public void clearToken() {

    }

    public void JWTTokenValidate() {

    }

    // wip
    public void getMatchHistory() {

    }



}
