package com.finance.core.model;

import java.io.Serializable;
import java.util.Objects;

public class Budget implements Serializable {
    private final String category;
    private double limit;
    private double spent;
    private double warningThreshold;

    public Budget(String category, double limit) {
        this(category, limit, 0.8); // По умолчанию предупреждение при 80%
    }

    public Budget(String category, double limit, double warningThreshold) {
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.limit = limit;
        this.spent = 0.0;
        this.warningThreshold = warningThreshold;

        validate();
    }

    private void validate() {
        if (category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative");
        }
        if (warningThreshold < 0 || warningThreshold > 1) {
            throw new IllegalArgumentException("Warning threshold must be between 0 and 1");
        }
    }

    public void addSpending(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Spending amount cannot be negative");
        }
        this.spent += amount;
    }

    public double getRemaining() {
        return limit - spent;
    }

    public boolean isExceeded() {
        return spent > limit;
    }

    public boolean isWarningThresholdReached() {
        return spent >= limit * warningThreshold;
    }

    public double getUsagePercentage() {
        return limit > 0 ? (spent / limit) * 100 : 0;
    }

    public String getCategory() { return category; }
    public double getLimit() { return limit; }
    public double getSpent() { return spent; }
    public double getWarningThreshold() { return warningThreshold; }

    public void setLimit(double limit) {
        this.limit = limit;
        validate();
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
        validate();
    }
}