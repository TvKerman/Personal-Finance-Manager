package com.finance.core.service;

import com.finance.core.model.*;

import java.util.List;
import java.util.Set;

public class FinanceService {
    private final BudgetService budgetService;
    private Wallet userWallet;

    public FinanceService() {
        this.budgetService = new BudgetService();
        this.userWallet = null;
    }

    public void setUserWallet(Wallet wallet) {
        userWallet = wallet;
    }

    public void addIncome(String category, double amount, String description) {
        Transaction transaction = new Transaction(category, amount, TransactionType.INCOME, description);
        userWallet.addTransaction(transaction);
    }

    public void addExpense(String category, double amount, String description) {
        Transaction transaction = new Transaction(category, amount, TransactionType.EXPENSE, description);
        userWallet.addTransaction(transaction);
    }

    public void setBudget(String category, double limit) {
        userWallet.setBudget(category, limit);
    }

    public void updateBudget(String category, double newLimit) {
        userWallet.updateBudget(category, newLimit);
    }

    public void removeBudget(String category) {
        userWallet.removeBudget(category);
    }

    public void transfer(Wallet target, double amount, String description) {
        if (userWallet.equals(target)) {
            throw new IllegalArgumentException("Перевод с кошелька на тот же кошелек");
        }
        if (userWallet.getBalance() < amount) {
            throw new IllegalStateException("Недостаточно средств для перевода");
        }

        addExpense("Перевод", amount, "Перевод пользователю " + target.getUsername() + ": " + description);

        Transaction income = new Transaction("Перевод", amount, TransactionType.INCOME,
                "Перевод от " + userWallet.getUsername() + ": " + description);
        target.addTransaction(income);
    }

    public FinancialReport generateReport() {
        return new FinancialReport(userWallet);
    }

    public double calculateExpensesForCategories(Set<String> categories) {
        return userWallet.calculateExpensesForCategories(categories);
    }

    public List<String> checkAlerts() {
        return userWallet.checkBudgetAlerts();
    }

    public Wallet getCurrentWallet() {
        return userWallet;
    }

    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets(userWallet);
    }

    public boolean hasBudgetForCategory(String category) {
        return budgetService.hasBudgetForCategory(userWallet, category);
    }

    public List<Budget> getExceededBudgets() {
        return budgetService.getExceededBudgets(userWallet);
    }

    public List<Budget> getBudgetsNearLimit(double thresholdPercentage) {
        return budgetService.getBudgetsNearLimit(userWallet, thresholdPercentage);
    }
}