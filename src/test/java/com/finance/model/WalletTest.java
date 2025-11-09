package com.finance.model;

import com.finance.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("testuser");
    }

    @Test
    @DisplayName("Создание кошелька с валидными данными")
    void testWalletCreation() {
        assertEquals("testuser", wallet.getUsername());
        assertEquals(0.0, wallet.getBalance());
        assertTrue(wallet.getTransactions().isEmpty());
        assertTrue(wallet.getBudgets().isEmpty());
        assertTrue(wallet.getCategories().isEmpty());
    }

    @Test
    @DisplayName("Добавление дохода увеличивает баланс")
    void testAddIncomeIncreasesBalance() {
        Transaction income = new Transaction("Зарплата", 50000, TransactionType.INCOME, "Тест");
        wallet.addTransaction(income);

        assertEquals(50000, wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
        assertTrue(wallet.getCategories().contains("Зарплата"));
    }

    @Test
    @DisplayName("Добавление расхода уменьшает баланс")
    void testAddExpenseDecreasesBalance() {
        Transaction income = new Transaction("Зарплата", 50000, TransactionType.INCOME, "Тест");
        wallet.addTransaction(income);

        Transaction expense = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");
        wallet.addTransaction(expense);

        assertEquals(45000, wallet.getBalance());
        assertEquals(2, wallet.getTransactions().size());
        assertTrue(wallet.getCategories().contains("Еда"));
    }

    @Test
    @DisplayName("Добавление расхода без достаточного баланса")
    void testAddExpenseWithoutSufficientBalance() {
        Transaction expense = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");
        wallet.addTransaction(expense);

        assertEquals(-5000, wallet.getBalance());
        assertEquals(1, wallet.getTransactions().size());
    }

    @Test
    @DisplayName("Установка бюджета для категории")
    void testSetBudget() {
        wallet.setBudget("Еда", 10000);

        assertTrue(wallet.getBudgets().containsKey("Еда"));
        Budget budget = wallet.getBudgets().get("Еда");
        assertEquals("Еда", budget.getCategory());
        assertEquals(10000, budget.getLimit());
        assertEquals(0.0, budget.getSpent());
    }

    @Test
    @DisplayName("Расходы учитываются в бюджете")
    void testExpenseUpdatesBudget() {
        wallet.setBudget("Еда", 10000);
        Transaction expense = new Transaction("Еда", 3000, TransactionType.EXPENSE, "Продукты");
        wallet.addTransaction(expense);

        Budget budget = wallet.getBudgets().get("Еда");
        assertEquals(3000, budget.getSpent());
        assertEquals(7000, budget.getRemaining());
        assertFalse(budget.isExceeded());
    }

    @Test
    @DisplayName("Превышение бюджета")
    void testBudgetExceeded() {
        wallet.setBudget("Еда", 1000);
        Transaction expense = new Transaction("Еда", 1200, TransactionType.EXPENSE, "Дорогие продукты");
        wallet.addTransaction(expense);

        Budget budget = wallet.getBudgets().get("Еда");
        assertTrue(budget.isExceeded());
        assertEquals(-200, budget.getRemaining());
    }

    @Test
    @DisplayName("Обновление существующего бюджета")
    void testUpdateBudget() {
        wallet.setBudget("Еда", 1000);
        wallet.addTransaction(new Transaction("Еда", 500, TransactionType.EXPENSE, "Продукты"));

        wallet.updateBudget("Еда", 2000);

        Budget budget = wallet.getBudgets().get("Еда");
        assertEquals(2000, budget.getLimit());
        assertEquals(500, budget.getSpent());
        assertEquals(1500, budget.getRemaining());
    }

    @Test
    @DisplayName("Обновление несуществующего бюджета")
    void testUpdateNonExistentBudget() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.updateBudget("Несуществующая", 1000);
        });
    }

    @Test
    @DisplayName("Удаление бюджета")
    void testRemoveBudget() {
        wallet.setBudget("Еда", 1000);
        wallet.removeBudget("Еда");

        assertFalse(wallet.getBudgets().containsKey("Еда"));
    }

    @Test
    @DisplayName("Подсчет общих доходов")
    void testGetTotalIncome() {
        wallet.addTransaction(new Transaction("Зарплата", 50000, TransactionType.INCOME, "Основная"));
        wallet.addTransaction(new Transaction("Бонус", 10000, TransactionType.INCOME, "Премия"));
        wallet.addTransaction(new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты"));

        assertEquals(60000, wallet.getTotalIncome());
    }

    @Test
    @DisplayName("Подсчет общих расходов")
    void testGetTotalExpenses() {
        wallet.addTransaction(new Transaction("Зарплата", 50000, TransactionType.INCOME, "Основная"));
        wallet.addTransaction(new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты"));
        wallet.addTransaction(new Transaction("Транспорт", 3000, TransactionType.EXPENSE, "Такси"));

        assertEquals(8000, wallet.getTotalExpenses());
    }

    @Test
    @DisplayName("Подсчет расходов по нескольким категориям")
    void testCalculateExpensesForCategories() {
        wallet.addTransaction(new Transaction("Еда", 1000, TransactionType.EXPENSE, "Продукты"));
        wallet.addTransaction(new Transaction("Транспорт", 2000, TransactionType.EXPENSE, "Такси"));
        wallet.addTransaction(new Transaction("Еда", 1500, TransactionType.EXPENSE, "Ресторан"));
        wallet.addTransaction(new Transaction("Развлечения", 3000, TransactionType.EXPENSE, "Кино"));

        double total = wallet.calculateExpensesForCategories(Set.of("Еда", "Транспорт"));

        assertEquals(4500, total);
    }

    @Test
    @DisplayName("Подсчет расходов по несуществующим категориям")
    void testCalculateExpensesForNonExistentCategories() {
        wallet.addTransaction(new Transaction("Еда", 1000, TransactionType.EXPENSE, "Продукты"));

        double total = wallet.calculateExpensesForCategories(Set.of("Несуществующая"));

        assertEquals(0, total);
    }

    @Test
    @DisplayName("Подсчет расходов по пустому набору категорий")
    void testCalculateExpensesForEmptyCategories() {
        wallet.addTransaction(new Transaction("Еда", 1000, TransactionType.EXPENSE, "Продукты"));

        assertThrows(IllegalArgumentException.class, () -> {
            wallet.calculateExpensesForCategories(Set.of());
        });
    }

    @Test
    @DisplayName("Получение доходов по категориям")
    void testGetIncomeByCategory() {
        wallet.addTransaction(new Transaction("Зарплата", 50000, TransactionType.INCOME, "Основная"));
        wallet.addTransaction(new Transaction("Бонус", 10000, TransactionType.INCOME, "Премия"));
        wallet.addTransaction(new Transaction("Зарплата", 20000, TransactionType.INCOME, "Аванс"));

        var incomeByCategory = wallet.getIncomeByCategory();

        assertEquals(70000, incomeByCategory.get("Зарплата"));
        assertEquals(10000, incomeByCategory.get("Бонус"));
        assertEquals(2, incomeByCategory.size());
    }

    @Test
    @DisplayName("Получение расходов по категориям")
    void testGetExpensesByCategory() {
        wallet.addTransaction(new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты"));
        wallet.addTransaction(new Transaction("Транспорт", 3000, TransactionType.EXPENSE, "Такси"));
        wallet.addTransaction(new Transaction("Еда", 2000, TransactionType.EXPENSE, "Ресторан"));

        var expensesByCategory = wallet.getExpensesByCategory();

        assertEquals(7000, expensesByCategory.get("Еда"));
        assertEquals(3000, expensesByCategory.get("Транспорт"));
        assertEquals(2, expensesByCategory.size());
    }

    @Test
    @DisplayName("Проверка предупреждений о превышении бюджета")
    void testBudgetAlerts() {
        wallet.setBudget("Еда", 1000);
        wallet.addTransaction(new Transaction("Еда", 1200, TransactionType.EXPENSE, "Дорогие продукты"));

        List<String> alerts = wallet.checkBudgetAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.getFirst().contains("Превышен бюджет"));
        assertTrue(alerts.getFirst().contains("Еда"));
    }

    @Test
    @DisplayName("Проверка предупреждений при отрицательном балансе")
    void testNegativeBalanceAlert() {
        wallet.addTransaction(new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты"));

        List<String> alerts = wallet.checkBudgetAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.stream().anyMatch(s -> s.contains("Отрицательный баланс")));
    }

    @Test
    @DisplayName("Проверка предупреждений когда расходы превышают доходы")
    void testExpensesExceedIncomeAlert() {
        wallet.addTransaction(new Transaction("Зарплата", 5000, TransactionType.INCOME, "Основная"));
        wallet.addTransaction(new Transaction("Еда", 3000, TransactionType.EXPENSE, "Продукты"));
        wallet.addTransaction(new Transaction("Транспорт", 4000, TransactionType.EXPENSE, "Машина"));

        List<String> alerts = wallet.checkBudgetAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.getFirst().contains("Расходы превысили доходы"));
    }

    @Test
    @DisplayName("Проверка предупреждений при достижении порога бюджета")
    void testBudgetWarningThresholdAlert() {
        wallet.setBudget("Еда", 1000);
        wallet.addTransaction(new Transaction("Еда", 800, TransactionType.EXPENSE, "Продукты"));

        List<String> alerts = wallet.checkBudgetAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.getFirst().contains("Еда"));
    }

    @Test
    @DisplayName("Отсутствие предупреждений при нормальной ситуации")
    void testNoAlertsInNormalSituation() {
        wallet.addTransaction(new Transaction("Зарплата", 50000, TransactionType.INCOME, "Основная"));
        wallet.addTransaction(new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты"));
        wallet.setBudget("Еда", 10000);

        List<String> alerts = wallet.checkBudgetAlerts();

        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Транзакции добавляются в хронологическом порядке")
    void testTransactionsInChronologicalOrder() {
        Transaction first = new Transaction("Зарплата", 50000, TransactionType.INCOME, "Первая");
        Transaction second = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Вторая");

        wallet.addTransaction(first);
        wallet.addTransaction(second);

        List<Transaction> transactions = wallet.getTransactions();
        assertEquals(2, transactions.size());

        assertEquals("Зарплата", transactions.get(0).getCategory());
        assertEquals("Еда", transactions.get(1).getCategory());
    }
}