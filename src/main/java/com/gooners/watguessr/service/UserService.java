package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
