package com.finance.core.service;

import com.finance.core.model.User;
import com.finance.core.repository.UserRepository;

import java.util.List;

public class AuthService {
    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String username, String password) {
        if (userRepository.exists(username)) {
            return false;
        }

        try {
            User user = new User(username, password);
            userRepository.addUser(user);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.authenticate(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveChanges() {
        this.userRepository.save();
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean userExists(String username) {
        return userRepository.exists(username);
    }
}