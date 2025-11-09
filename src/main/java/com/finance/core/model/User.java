package com.finance.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final Wallet wallet;

    public User(String username, String password) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.wallet = new Wallet(username);

        validate();
    }

    private void validate() {
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public String getUsername() { return username; }
    public Wallet getWallet() { return wallet; }
}