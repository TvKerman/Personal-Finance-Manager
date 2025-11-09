package com.finance.core.repository;

import com.finance.core.model.User;
import java.util.*;

public interface UserRepository {
    User findByUsername(String username);

    void save();

    void addUser(User user);

    List<User> findAll();

    boolean exists(String username);
}