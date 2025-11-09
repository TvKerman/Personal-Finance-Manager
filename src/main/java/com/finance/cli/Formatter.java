package com.finance.cli;

import com.finance.core.model.Budget;
import com.finance.core.service.FinancialReport;

import java.util.Comparator;
import java.util.Map;

public class Formatter {

    public static void printReport(FinancialReport report) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ФИНАНСОВАЯ СТАТИСТИКА");
        System.out.println("=".repeat(60));

        System.out.printf("%-25s: %,12.2f руб.%n", "Общий доход", report.getTotalIncome());
        System.out.printf("%-25s: %,12.2f руб.%n", "Общие расходы", report.getTotalExpenses());
        System.out.printf("%-25s: %,12.2f руб.%n", "Текущий баланс", report.getBalance());

        if (!report.getIncomeByCategory().isEmpty()) {
            System.out.println("\nДОХОДЫ ПО КАТЕГОРИЯМ:");
            System.out.println("-".repeat(40));
            report.getIncomeByCategory().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(entry ->
                            System.out.printf("  %-20s: %,10.2f руб.%n",
                                    entry.getKey(), entry.getValue())
                    );
        }

        if (!report.getExpensesByCategory().isEmpty()) {
            System.out.println("\nРАСХОДЫ ПО КАТЕГОРИЯМ:");
            System.out.println("-".repeat(40));
            report.getExpensesByCategory().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(entry ->
                            System.out.printf("  %-20s: %,10.2f руб.%n",
                                    entry.getKey(), entry.getValue())
                    );
        }

        if (!report.getBudgets().isEmpty()) {
            System.out.println("\nБЮДДЖЕТЫ:");
            System.out.println("-".repeat(75));
            System.out.printf("%-20s %10s %12s %12s %12s%n",
                    "Категория", "Лимит", "Потрачено", "Остаток", "Статус");
            System.out.println("-".repeat(75));

            report.getBudgets().values().stream()
                    .sorted(Comparator.comparing(Budget::getCategory))
                    .forEach(budget -> {
                        double remaining = budget.getRemaining();
                        String remainingStr = String.format("%,10.2f", remaining);

                        String status = "";
                        if (remaining < 0) {
                            status = "ПРЕВЫШЕН";
                        } else if (budget.isWarningThresholdReached()) {
                            status = "ВНИМАНИЕ";
                        }

                        System.out.printf("%-20s %,10.2f %,10.2f %12s %12s%n",
                                budget.getCategory(),
                                budget.getLimit(),
                                budget.getSpent(),
                                remainingStr,
                                status);
                    });
        }

        System.out.println("=".repeat(75));
    }

    public static void printTransactionTable(java.util.List<com.finance.core.model.Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("Нет транзакций для отображения");
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.printf("%-12s %-10s %-20s %-12s %-20s%n",
                "Дата", "Тип", "Категория", "Сумма", "Описание");
        System.out.println("=".repeat(80));

        for (com.finance.core.model.Transaction transaction : transactions) {
            String typeStr = transaction.getType() == com.finance.core.model.TransactionType.INCOME ?
                    "Доход" : "Расход";
            String amountStr = String.format("%,.2f", transaction.getAmount());

            System.out.printf("%-12s %-10s %-20s %-12s %-20s%n",
                    transaction.getDate().toLocalDate(),
                    typeStr,
                    transaction.getCategory(),
                    amountStr,
                    transaction.getDescription());
        }
        System.out.println("=".repeat(80));
    }
}
