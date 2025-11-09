package com.finance.service;

import com.finance.core.model.Budget;
import com.finance.core.model.Wallet;
import com.finance.core.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {
    private BudgetService budgetService;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService();
        wallet = new Wallet("testuser");
    }

    @Test
    @DisplayName("Поиск бюджета по существующей категории")
    void testFindBudgetByExistingCategory() {
        wallet.setBudget("Еда", 10000);

        Optional<Budget> budget = budgetService.findBudgetByCategory(wallet, "Еда");

        assertTrue(budget.isPresent());
        assertEquals("Еда", budget.get().getCategory());
        assertEquals(10000, budget.get().getLimit());
    }

    @Test
    @DisplayName("Поиск бюджета по несуществующей категории")
    void testFindBudgetByNonExistentCategory() {
        Optional<Budget> budget = budgetService.findBudgetByCategory(wallet, "Несуществующая");

        assertFalse(budget.isPresent());
    }

    @Test
    @DisplayName("Получение всех бюджетов")
    void testGetAllBudgets() {
        wallet.setBudget("Еда", 10000);
        wallet.setBudget("Транспорт", 5000);
        wallet.setBudget("Развлечения", 3000);

        List<Budget> budgets = budgetService.getAllBudgets(wallet);

        assertEquals(3, budgets.size());
    }

    @Test
    @DisplayName("Получение всех бюджетов при их отсутствии")
    void testGetAllBudgetsWhenEmpty() {
        List<Budget> budgets = budgetService.getAllBudgets(wallet);

        assertTrue(budgets.isEmpty());
    }

    @Test
    @DisplayName("Проверка наличия бюджета для категории")
    void testHasBudgetForCategory() {
        wallet.setBudget("Еда", 10000);

        assertTrue(budgetService.hasBudgetForCategory(wallet, "Еда"));
        assertFalse(budgetService.hasBudgetForCategory(wallet, "Транспорт"));
    }

    @Test
    @DisplayName("Получение общего лимита бюджетов")
    void testGetTotalBudgetLimit() {
        wallet.setBudget("Еда", 10000);
        wallet.setBudget("Транспорт", 5000);

        double totalLimit = budgetService.getTotalBudgetLimit(wallet);

        assertEquals(15000, totalLimit);
    }

    @Test
    @DisplayName("Получение общей потраченной суммы по бюджетам")
    void testGetTotalBudgetSpent() {
        wallet.setBudget("Еда", 10000);
        wallet.setBudget("Транспорт", 5000);

        wallet.addTransaction(new com.finance.core.model.Transaction("Еда", 3000,
                com.finance.core.model.TransactionType.EXPENSE, "Продукты"));
        wallet.addTransaction(new com.finance.core.model.Transaction("Транспорт", 2000,
                com.finance.core.model.TransactionType.EXPENSE, "Такси"));

        double totalSpent = budgetService.getTotalBudgetSpent(wallet);

        assertEquals(5000, totalSpent);
    }

    @Test
    @DisplayName("Получение превышенных бюджетов")
    void testGetExceededBudgets() {
        wallet.setBudget("Еда", 1000);
        wallet.setBudget("Транспорт", 2000);

        wallet.addTransaction(new com.finance.core.model.Transaction("Еда", 1200,
                com.finance.core.model.TransactionType.EXPENSE, "Дорогие продукты"));
        wallet.addTransaction(new com.finance.core.model.Transaction("Транспорт", 1500,
                com.finance.core.model.TransactionType.EXPENSE, "Такси"));

        List<Budget> exceededBudgets = budgetService.getExceededBudgets(wallet);

        assertEquals(1, exceededBudgets.size());
        assertEquals("Еда", exceededBudgets.getFirst().getCategory());
    }

    @Test
    @DisplayName("Получение бюджетов близких к лимиту")
    void testGetBudgetsNearLimit() {
        wallet.setBudget("Еда", 1000);
        wallet.setBudget("Транспорт", 2000);

        wallet.addTransaction(new com.finance.core.model.Transaction("Еда", 900,
                com.finance.core.model.TransactionType.EXPENSE, "Продукты"));
        wallet.addTransaction(new com.finance.core.model.Transaction("Транспорт", 1000,
                com.finance.core.model.TransactionType.EXPENSE, "Бензин"));

        List<Budget> nearLimitBudgets = budgetService.getBudgetsNearLimit(wallet, 80.0);

        assertEquals(1, nearLimitBudgets.size());
        assertEquals("Еда", nearLimitBudgets.getFirst().getCategory());
    }

    @Test
    @DisplayName("Получение бюджетов близких к лимиту при разных порогах")
    void testGetBudgetsNearLimitWithDifferentThresholds() {
        wallet.setBudget("Еда", 1000);
        wallet.addTransaction(new com.finance.core.model.Transaction("Еда", 850,
                com.finance.core.model.TransactionType.EXPENSE, "Продукты"));

        List<Budget> at80 = budgetService.getBudgetsNearLimit(wallet, 80.0);
        assertEquals(1, at80.size());

        List<Budget> at90 = budgetService.getBudgetsNearLimit(wallet, 90.0);
        assertTrue(at90.isEmpty());
    }

    @Test
    @DisplayName("Работа с пустым кошельком")
    void testWithEmptyWallet() {
        assertTrue(budgetService.getAllBudgets(wallet).isEmpty());
        assertFalse(budgetService.hasBudgetForCategory(wallet, "Любая"));
        assertEquals(0.0, budgetService.getTotalBudgetLimit(wallet));
        assertEquals(0.0, budgetService.getTotalBudgetSpent(wallet));
        assertTrue(budgetService.getExceededBudgets(wallet).isEmpty());
        assertTrue(budgetService.getBudgetsNearLimit(wallet, 80.0).isEmpty());
    }
}