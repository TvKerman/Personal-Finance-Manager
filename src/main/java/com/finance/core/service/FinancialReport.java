package com.finance.core.service;

import com.finance.core.model.Wallet;

public class FinancialReport {
    private final double totalIncome;
    private final double totalExpenses;
    private final double balance;
    private final java.util.Map<String, Double> incomeByCategory;
    private final java.util.Map<String, Double> expensesByCategory;
    private final java.util.Map<String, com.finance.core.model.Budget> budgets;

    public FinancialReport(Wallet wallet) {
        this.totalIncome = wallet.getTotalIncome();
        this.totalExpenses = wallet.getTotalExpenses();
        this.balance = wallet.getBalance();
        this.incomeByCategory = wallet.getIncomeByCategory();
        this.expensesByCategory = wallet.getExpensesByCategory();
        this.budgets = wallet.getBudgets();
    }

    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpenses() { return totalExpenses; }
    public double getBalance() { return balance; }
    public java.util.Map<String, Double> getIncomeByCategory() { return incomeByCategory; }
    public java.util.Map<String, Double> getExpensesByCategory() { return expensesByCategory; }
    public java.util.Map<String, com.finance.core.model.Budget> getBudgets() { return budgets; }
}
