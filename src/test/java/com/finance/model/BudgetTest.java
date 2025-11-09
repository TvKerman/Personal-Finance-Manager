package com.finance.model;

import com.finance.core.model.Budget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTest {

    @Test
    @DisplayName("Создание бюджета с валидными параметрами")
    void testBudgetCreation() {
        Budget budget = new Budget("Еда", 10000);

        assertEquals("Еда", budget.getCategory());
        assertEquals(10000, budget.getLimit());
        assertEquals(0.0, budget.getSpent());
        assertEquals(10000, budget.getRemaining());
        assertFalse(budget.isExceeded());
        assertEquals(0.8, budget.getWarningThreshold());
    }

    @Test
    @DisplayName("Создание бюджета с кастомным порогом предупреждения")
    void testBudgetCreationWithCustomThreshold() {
        Budget budget = new Budget("Еда", 10000, 0.9);

        assertEquals(0.9, budget.getWarningThreshold());
    }

    @Test
    @DisplayName("Добавление расходов в бюджет")
    void testAddSpending() {
        Budget budget = new Budget("Еда", 10000);

        budget.addSpending(3000);
        assertEquals(3000, budget.getSpent());
        assertEquals(7000, budget.getRemaining());
        assertFalse(budget.isExceeded());

        budget.addSpending(5000);
        assertEquals(8000, budget.getSpent());
        assertEquals(2000, budget.getRemaining());
        assertFalse(budget.isExceeded());
    }

    @Test
    @DisplayName("Превышение лимита бюджета")
    void testBudgetExceeded() {
        Budget budget = new Budget("Еда", 10000);

        budget.addSpending(12000);
        assertTrue(budget.isExceeded());
        assertEquals(-2000, budget.getRemaining());
    }

    @Test
    @DisplayName("Точное достижение лимита бюджета")
    void testBudgetExactlyAtLimit() {
        Budget budget = new Budget("Еда", 10000);

        budget.addSpending(10000);
        assertFalse(budget.isExceeded());
        assertEquals(0, budget.getRemaining());
    }

    @Test
    @DisplayName("Достижение порога предупреждения")
    void testWarningThresholdReached() {
        Budget budget = new Budget("Еда", 10000, 0.8);

        budget.addSpending(7000);
        assertFalse(budget.isWarningThresholdReached());

        budget.addSpending(1000);
        assertTrue(budget.isWarningThresholdReached());
        assertEquals(80.0, budget.getUsagePercentage());
    }

    @Test
    @DisplayName("Процент использования бюджета")
    void testUsagePercentage() {
        Budget budget = new Budget("Еда", 1000);

        budget.addSpending(250);
        assertEquals(25.0, budget.getUsagePercentage());

        budget.addSpending(500);
        assertEquals(75.0, budget.getUsagePercentage());
    }

    @Test
    @DisplayName("Процент использования при нулевом лимите")
    void testUsagePercentageWithZeroLimit() {
        Budget budget = new Budget("Еда", 0);

        budget.addSpending(100);
        assertEquals(0.0, budget.getUsagePercentage());
    }

    @Test
    @DisplayName("Обновление лимита бюджета")
    void testUpdateLimit() {
        Budget budget = new Budget("Еда", 10000);
        budget.addSpending(5000);

        budget.setLimit(15000);
        assertEquals(15000, budget.getLimit());
        assertEquals(10000, budget.getRemaining());
    }

    @Test
    @DisplayName("Обновление порога предупреждения")
    void testUpdateWarningThreshold() {
        Budget budget = new Budget("Еда", 10000, 0.8);

        budget.setWarningThreshold(0.7);
        assertEquals(0.7, budget.getWarningThreshold());
    }

    @Test
    @DisplayName("Создание бюджета с невалидными параметрами")
    void testInvalidBudgetCreation() {
        assertThrows(IllegalArgumentException.class, () -> new Budget("", 1000));
        assertThrows(IllegalArgumentException.class, () -> new Budget("Еда", -1000));
        assertThrows(IllegalArgumentException.class, () -> new Budget("Еда", 1000, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new Budget("Еда", 1000, -0.5));
    }

    @Test
    @DisplayName("Обновление с невалидными параметрами")
    void testInvalidUpdate() {
        Budget budget = new Budget("Еда", 10000);

        assertThrows(IllegalArgumentException.class, () -> budget.setLimit(-1000));
        assertThrows(IllegalArgumentException.class, () -> budget.setWarningThreshold(1.5));
    }

    @Test
    @DisplayName("Добавление отрицательных расходов")
    void testAddNegativeSpending() {
        Budget budget = new Budget("Еда", 10000);

        assertThrows(IllegalArgumentException.class, () -> budget.addSpending(-1000));
    }

    @Test
    @DisplayName("Добавление нулевых расходов")
    void testAddZeroSpending() {
        Budget budget = new Budget("Еда", 10000);

        budget.addSpending(0);
        assertEquals(0, budget.getSpent());
        assertEquals(10000, budget.getRemaining());
    }

    @Test
    @DisplayName("Порог предупреждения при разных значениях")
    void testWarningThresholdWithDifferentValues() {
        Budget budget50 = new Budget("Еда", 1000, 0.5);
        budget50.addSpending(500);
        assertTrue(budget50.isWarningThresholdReached());

        Budget budget100 = new Budget("Транспорт", 1000, 1.0);
        budget100.addSpending(1000);
        assertTrue(budget100.isWarningThresholdReached());
        assertFalse(budget100.isExceeded());
        budget100.addSpending(1);
        assertTrue(budget100.isWarningThresholdReached());
        assertTrue(budget100.isExceeded());
    }

    @Test
    @DisplayName("Полное использование бюджета")
    void testFullBudgetUsage() {
        Budget budget = new Budget("Еда", 1000);

        budget.addSpending(1000);
        assertEquals(1000, budget.getSpent());
        assertEquals(0, budget.getRemaining());
        assertEquals(100.0, budget.getUsagePercentage());
        assertFalse(budget.isExceeded());
        assertTrue(budget.isWarningThresholdReached());
    }
}