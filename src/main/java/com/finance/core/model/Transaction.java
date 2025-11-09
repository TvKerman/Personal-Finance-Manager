package com.finance.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Transaction implements Serializable {
    private final String id;
    private final String category;
    private final double amount;
    private final TransactionType type;
    private final LocalDateTime date;
    private final String description;

    public Transaction(String category, double amount, TransactionType type, String description) {
        this.id = UUID.randomUUID().toString();
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.amount = amount;
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.date = LocalDateTime.now();
        this.description = description != null ? description : "";

        validate();
    }

    private void validate() {
        if (category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}