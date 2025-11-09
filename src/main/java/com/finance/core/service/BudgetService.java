package com.finance.core.service;

import com.finance.core.model.Budget;
import com.finance.core.model.Wallet;
import java.util.List;
import java.util.Optional;

public class BudgetService {

    public Optional<Budget> findBudgetByCategory(Wallet wallet, String category) {
        return Optional.ofNullable(wallet.getBudgets().get(category));
    }

    public List<Budget> getAllBudgets(Wallet wallet) {
        return List.copyOf(wallet.getBudgets().values());
    }

    public boolean hasBudgetForCategory(Wallet wallet, String category) {
        return wallet.getBudgets().containsKey(category);
    }

    public double getTotalBudgetLimit(Wallet wallet) {
        return wallet.getBudgets().values().stream()
                .mapToDouble(Budget::getLimit)
                .sum();
    }

    public double getTotalBudgetSpent(Wallet wallet) {
        return wallet.getBudgets().values().stream()
                .mapToDouble(Budget::getSpent)
                .sum();
    }

    public List<Budget> getExceededBudgets(Wallet wallet) {
        return wallet.getBudgets().values().stream()
                .filter(Budget::isExceeded)
                .toList();
    }

    public List<Budget> getBudgetsNearLimit(Wallet wallet, double thresholdPercentage) {
        return wallet.getBudgets().values().stream()
                .filter(budget -> !budget.isExceeded())
                .filter(budget -> budget.getUsagePercentage() >= thresholdPercentage)
                .toList();
    }
}