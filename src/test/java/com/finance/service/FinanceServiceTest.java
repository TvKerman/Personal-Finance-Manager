package com.finance.service;

import com.finance.core.model.*;
import com.finance.core.service.FinanceService;
import com.finance.core.service.FinancialReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {
    private FinanceService financeService;
    private Wallet userWallet;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService();
        userWallet = new Wallet("testuser");
        financeService.setUserWallet(userWallet);
    }

    @Test
    @DisplayName("Установка кошелька пользователя")
    void testSetUserWallet() {
        Wallet newWallet = new Wallet("newuser");
        financeService.setUserWallet(newWallet);

        assertEquals(newWallet, financeService.getCurrentWallet());
    }

    @Test
    @DisplayName("Добавление дохода увеличивает баланс")
    void testAddIncomeIncreasesBalance() {
        financeService.addIncome("Зарплата", 50000, "Основная зарплата");

        assertEquals(50000, userWallet.getBalance());
        assertEquals(50000, userWallet.getTotalIncome());
        assertEquals(1, userWallet.getTransactions().size());
    }

    @Test
    @DisplayName("Добавление расхода уменьшает баланс")
    void testAddExpenseDecreasesBalance() {
        financeService.addIncome("Зарплата", 50000, "Основная зарплата");
        financeService.addExpense("Еда", 5000, "Продукты");

        assertEquals(45000, userWallet.getBalance());
        assertEquals(5000, userWallet.getTotalExpenses());
    }

    @Test
    @DisplayName("Добавление расхода без достаточного баланса")
    void testAddExpenseWithoutSufficientBalance() {
        financeService.addExpense("Еда", 5000, "Продукты");

        assertEquals(-5000, userWallet.getBalance());
        assertEquals(1, userWallet.getTransactions().size());
    }

    @Test
    @DisplayName("Установка бюджета для категории")
    void testSetBudget() {
        financeService.setBudget("Еда", 10000);

        assertTrue(userWallet.getBudgets().containsKey("Еда"));
        assertEquals(10000, userWallet.getBudgets().get("Еда").getLimit());
    }

    @Test
    @DisplayName("Обновление существующего бюджета")
    void testUpdateBudget() {
        financeService.setBudget("Еда", 10000);
        financeService.updateBudget("Еда", 15000);

        assertEquals(15000, userWallet.getBudgets().get("Еда").getLimit());
    }

    @Test
    @DisplayName("Обновление несуществующего бюджета")
    void testUpdateNonExistentBudget() {
        assertThrows(IllegalArgumentException.class, () -> {
            financeService.updateBudget("Несуществующая", 1000);
        });
    }

    @Test
    @DisplayName("Удаление бюджета")
    void testRemoveBudget() {
        financeService.setBudget("Еда", 10000);
        financeService.removeBudget("Еда");

        assertFalse(userWallet.getBudgets().containsKey("Еда"));
    }

    @Test
    @DisplayName("Успешный перевод между кошельками")
    void testSuccessfulTransfer() {
        Wallet targetWallet = new Wallet("recipient");

        financeService.addIncome("Зарплата", 50000, "Основная зарплата");
        financeService.transfer(targetWallet, 10000, "Тестовый перевод");

        assertEquals(40000, userWallet.getBalance());
        assertEquals(10000, userWallet.getTotalExpenses());

        assertEquals(10000, targetWallet.getBalance());
        assertEquals(10000, targetWallet.getTotalIncome());
        assertEquals(1, targetWallet.getTransactions().size());
    }

    @Test
    @DisplayName("Перевод с недостаточным балансом")
    void testTransferWithInsufficientBalance() {
        Wallet targetWallet = new Wallet("recipient");

        financeService.addIncome("Зарплата", 5000, "Основная зарплата");

        assertThrows(IllegalStateException.class, () -> {
            financeService.transfer(targetWallet, 10000, "Тестовый перевод");
        });
    }

    @Test
    @DisplayName("Перевод с нулевым балансом")
    void testTransferWithZeroBalance() {
        Wallet targetWallet = new Wallet("recipient");

        assertThrows(IllegalStateException.class, () -> {
            financeService.transfer(targetWallet, 1000, "Тестовый перевод");
        });
    }

    @Test
    @DisplayName("Перевод на тот же кошелек")
    void testTransferToSameWallet() {
        financeService.addIncome("Зарплата", 50000, "Основная зарплата");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> financeService.transfer(userWallet, 10000, "Перевод самому себе")
        );
    }

    @Test
    @DisplayName("Подсчет расходов по нескольким категориям")
    void testCalculateExpensesForCategories() {
        financeService.addExpense("Еда", 5000, "Продукты");
        financeService.addExpense("Транспорт", 3000, "Такси");
        financeService.addExpense("Еда", 2000, "Ресторан");
        financeService.addExpense("Развлечения", 4000, "Кино");

        double total = financeService.calculateExpensesForCategories(Set.of("Еда", "Транспорт"));

        assertEquals(10000, total);
    }

    @Test
    @DisplayName("Подсчет расходов по несуществующим категориям")
    void testCalculateExpensesForNonExistentCategories() {
        financeService.addExpense("Еда", 1000, "Продукты");

        double total = financeService.calculateExpensesForCategories(Set.of("Несуществующая"));

        assertEquals(0, total);
    }

    @Test
    @DisplayName("Подсчет расходов по пустому набору категорий")
    void testCalculateExpensesForEmptyCategories() {
        financeService.addExpense("Еда", 1000, "Продукты");

        assertThrows(IllegalArgumentException.class, () -> {
            financeService.calculateExpensesForCategories(Set.of());
        });
    }

    @Test
    @DisplayName("Проверка предупреждений о превышении бюджета")
    void testBudgetAlerts() {
        financeService.setBudget("Еда", 1000);
        financeService.addExpense("Еда", 1200, "Дорогие продукты");

        List<String> alerts = financeService.checkAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.getFirst().contains("Превышен бюджет"));
        assertTrue(alerts.getFirst().contains("Еда"));
    }

    @Test
    @DisplayName("Проверка предупреждений при отрицательном балансе")
    void testNegativeBalanceAlert() {

        financeService.addExpense("Еда", 5000, "Продукты");

        List<String> alerts = financeService.checkAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.stream().anyMatch(s -> s.contains("Отрицательный баланс")));
    }

    @Test
    @DisplayName("Проверка предупреждений когда расходы превышают доходы")
    void testExpensesExceedIncomeAlert() {
        financeService.addIncome("Зарплата", 5000, "Основная");
        financeService.addExpense("Еда", 3000, "Продукты");
        financeService.addExpense("Транспорт", 4000, "Машина");

        List<String> alerts = financeService.checkAlerts();

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.getFirst().contains("Расходы превысили доходы"));
    }

    @Test
    @DisplayName("Отсутствие предупреждений при нормальной ситуации")
    void testNoAlertsInNormalSituation() {
        financeService.addIncome("Зарплата", 50000, "Основная");
        financeService.addExpense("Еда", 5000, "Продукты");
        financeService.setBudget("Еда", 10000);

        List<String> alerts = financeService.checkAlerts();

        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("Генерация финансового отчета")
    void testGenerateReport() {
        financeService.addIncome("Зарплата", 50000, "Основная");
        financeService.addExpense("Еда", 15000, "Продукты");
        financeService.setBudget("Еда", 20000);

        FinancialReport report = financeService.generateReport();

        assertEquals(50000, report.getTotalIncome());
        assertEquals(15000, report.getTotalExpenses());
        assertEquals(35000, report.getBalance());
        assertTrue(report.getIncomeByCategory().containsKey("Зарплата"));
        assertTrue(report.getExpensesByCategory().containsKey("Еда"));
        assertTrue(report.getBudgets().containsKey("Еда"));
    }

    @Test
    @DisplayName("Получение всех бюджетов")
    void testGetAllBudgets() {
        financeService.setBudget("Еда", 10000);
        financeService.setBudget("Транспорт", 5000);
        financeService.setBudget("Развлечения", 3000);

        List<Budget> budgets = financeService.getAllBudgets();

        assertEquals(3, budgets.size());
    }

    @Test
    @DisplayName("Получение всех бюджетов при их отсутствии")
    void testGetAllBudgetsWhenEmpty() {
        List<Budget> budgets = financeService.getAllBudgets();

        assertTrue(budgets.isEmpty());
    }

    @Test
    @DisplayName("Проверка наличия бюджета для категории")
    void testHasBudgetForCategory() {
        financeService.setBudget("Еда", 10000);

        assertTrue(financeService.hasBudgetForCategory("Еда"));
        assertFalse(financeService.hasBudgetForCategory("Транспорт"));
    }

    @Test
    @DisplayName("Получение превышенных бюджетов")
    void testGetExceededBudgets() {
        financeService.setBudget("Еда", 1000);
        financeService.setBudget("Транспорт", 2000);

        financeService.addExpense("Еда", 1200, "Дорогие продукты");
        financeService.addExpense("Транспорт", 1500, "Такси");

        List<Budget> exceededBudgets = financeService.getExceededBudgets();

        assertEquals(1, exceededBudgets.size());
        assertEquals("Еда", exceededBudgets.getFirst().getCategory());
    }

    @Test
    @DisplayName("Получение бюджетов близких к лимиту")
    void testGetBudgetsNearLimit() {
        financeService.setBudget("Еда", 1000);
        financeService.setBudget("Транспорт", 2000);

        financeService.addExpense("Еда", 900, "Продукты");
        financeService.addExpense("Транспорт", 1000, "Бензин");

        List<Budget> nearLimitBudgets = financeService.getBudgetsNearLimit(80.0);

        assertEquals(1, nearLimitBudgets.size());
        assertEquals("Еда", nearLimitBudgets.getFirst().getCategory());
    }

    @Test
    @DisplayName("Получение бюджетов близких к лимиту при разных порогах")
    void testGetBudgetsNearLimitWithDifferentThresholds() {
        financeService.setBudget("Еда", 1000);
        financeService.addExpense("Еда", 850, "Продукты");

        List<Budget> at80 = financeService.getBudgetsNearLimit(80.0);
        assertEquals(1, at80.size());

        List<Budget> at90 = financeService.getBudgetsNearLimit(90.0);
        assertTrue(at90.isEmpty());
    }

    @Test
    @DisplayName("Работа с незаданным кошельком")
    void testOperationsWithoutWallet() {
        FinanceService emptyService = new FinanceService();

        assertThrows(NullPointerException.class, () -> {
            emptyService.addIncome("Зарплата", 50000, "Тест");
        });

        assertNull(emptyService.getCurrentWallet());
    }

    @Test
    @DisplayName("Множественные транзакции корректно обрабатываются")
    void testMultipleTransactions() {
        financeService.addIncome("Зарплата", 50000, "Основная");
        financeService.addIncome("Бонус", 10000, "Премия");
        financeService.addExpense("Еда", 5000, "Продукты");
        financeService.addExpense("Транспорт", 3000, "Такси");
        financeService.addExpense("Еда", 2000, "Ресторан");

        assertEquals(60000, userWallet.getTotalIncome());
        assertEquals(10000, userWallet.getTotalExpenses());
        assertEquals(50000, userWallet.getBalance());
        assertEquals(5, userWallet.getTransactions().size());

        assertTrue(userWallet.getCategories().contains("Зарплата"));
        assertTrue(userWallet.getCategories().contains("Бонус"));
        assertTrue(userWallet.getCategories().contains("Еда"));
        assertTrue(userWallet.getCategories().contains("Транспорт"));
    }

    @Test
    @DisplayName("Транзакции добавляются в правильном порядке")
    void testTransactionsOrder() {
        financeService.addIncome("Зарплата", 50000, "Первая");
        financeService.addExpense("Еда", 5000, "Вторая");
        financeService.addIncome("Бонус", 10000, "Третья");

        List<Transaction> transactions = userWallet.getTransactions();
        assertEquals(3, transactions.size());
        assertEquals("Зарплата", transactions.get(0).getCategory());
        assertEquals("Еда", transactions.get(1).getCategory());
        assertEquals("Бонус", transactions.get(2).getCategory());
    }

    @Test
    @DisplayName("Комплексный сценарий: полный цикл работы")
    void testFullWorkflowScenario() {

        assertEquals(0, userWallet.getBalance());
        assertTrue(userWallet.getTransactions().isEmpty());

        financeService.addIncome("Зарплата", 50000, "Основная зарплата");
        financeService.addIncome("Бонус", 10000, "Квартальная премия");
        assertEquals(60000, userWallet.getBalance());

        financeService.setBudget("Еда", 15000);
        financeService.setBudget("Транспорт", 10000);
        financeService.setBudget("Развлечения", 5000);
        assertEquals(3, userWallet.getBudgets().size());

        financeService.addExpense("Еда", 5000, "Продукты на неделю");
        financeService.addExpense("Транспорт", 3000, "Бензин");
        financeService.addExpense("Развлечения", 2000, "Кино");
        financeService.addExpense("Еда", 3000, "Ресторан");
        assertEquals(47000, userWallet.getBalance());

        Budget foodBudget = userWallet.getBudgets().get("Еда");
        assertEquals(8000, foodBudget.getSpent());
        assertEquals(7000, foodBudget.getRemaining());
        assertFalse(foodBudget.isExceeded());

        FinancialReport report = financeService.generateReport();
        assertEquals(60000, report.getTotalIncome());
        assertEquals(13000, report.getTotalExpenses());
        assertEquals(47000, report.getBalance());

        List<String> alerts = financeService.checkAlerts();
        assertTrue(alerts.isEmpty());

        Wallet friendWallet = new Wallet("friend");
        financeService.transfer(friendWallet, 5000, "Одолжил другу");
        assertEquals(42000, userWallet.getBalance());
        assertEquals(5000, friendWallet.getBalance());

        double foodAndTransport = financeService.calculateExpensesForCategories(Set.of("Еда", "Транспорт"));
        assertEquals(11000, foodAndTransport);
    }
}