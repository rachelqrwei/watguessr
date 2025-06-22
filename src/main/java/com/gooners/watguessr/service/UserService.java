package com.gooners.watguessr.service;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.UserRepository;
import com.gooners.watguessr.repository.EntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public void update(User user) {
        userRepository.update(user);
    }
    public void create(User user) {
        if (!userRepository.emailAddressExists(user.getUsername())
                && !userRepository.usernameExists(user.getUsername())) {
            this.userRepository.create(user);
        }
        else throw new RuntimeException("Username or Email already exists");
    }
    public void delete(UUID id) {
        this.userRepository.delete(id);
    }

    public User findById(UUID id) {
        return this.userRepository.find(id);
    }
    public List<User> findAll(UUID id) {
        return this.userRepository.findAll();
    }
    public User findByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    public List<User> findSorted(String keyword, String sortBy, int page, int pageSize) {
        return this.userRepository.findSorted(keyword, sortBy, page, pageSize);
    }


}
