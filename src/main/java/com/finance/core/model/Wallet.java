package com.finance.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Wallet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private double balance;
    private final List<Transaction> transactions;
    private final Map<String, Budget> budgets;
    private final Set<String> categories;

    public Wallet(String username) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
        this.budgets = new HashMap<>();
        this.categories = new HashSet<>();
    }

    public void addTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");

        transactions.add(transaction);

        if (transaction.getType() == TransactionType.INCOME) {
            balance += transaction.getAmount();
        } else {
            balance -= transaction.getAmount();

            Budget budget = budgets.get(transaction.getCategory());
            if (budget != null) {
                budget.addSpending(transaction.getAmount());
            }
        }

        categories.add(transaction.getCategory());
    }

    public void setBudget(String category, double limit) {
        Budget budget = budgets.get(category);
        if (budget == null) {
            budget = new Budget(category, limit);
            budget.addSpending(calculateExpensesForCategories(new HashSet<>(Collections.singletonList(category))));
            budgets.put(category, budget);
        } else {
            budget.setLimit(limit);
        }
    }

    public void updateBudget(String category, double newLimit) {
        Budget budget = budgets.get(category);
        if (budget != null) {
            budget.setLimit(newLimit);
        } else {
            throw new IllegalArgumentException("Budget for category '" + category + "' not found");
        }
    }

    public void removeBudget(String category) {
        budgets.remove(category);
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<String, Double> getIncomeByCategory() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public Map<String, Double> getExpensesByCategory() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public double calculateExpensesForCategories(Set<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Categories cannot be null or empty");
        }

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> categories.contains(t.getCategory()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public List<String> checkBudgetAlerts() {
        List<String> alerts = new ArrayList<>();

        for (Budget budget : budgets.values()) {
            if (budget.isExceeded()) {
                alerts.add(String.format("Превышен бюджет для категории '%s'. Лимит: %.2f, Потрачено: %.2f",
                        budget.getCategory(), budget.getLimit(), budget.getSpent()));
            } else if (budget.isWarningThresholdReached()) {
                alerts.add(String.format("Внимание! Израсходовано %.1f%% бюджета для категории '%s'",
                        budget.getUsagePercentage(), budget.getCategory()));
            }
        }

        if (getTotalExpenses() > getTotalIncome()) {
            alerts.add("Внимание! Расходы превысили доходы");
        }

        if (balance < 0) {
            alerts.add("Внимание! Отрицательный баланс");
        }

        return alerts;
    }

    public boolean isExistsBudget(String category) {
        return budgets.containsKey(category);
    }

    public String getUsername() { return username; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }
    public Map<String, Budget> getBudgets() { return new HashMap<>(budgets); }
    public Set<String> getCategories() { return new HashSet<>(categories); }
}