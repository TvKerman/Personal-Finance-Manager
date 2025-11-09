package com.finance.model;

import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    @DisplayName("Создание транзакции дохода с валидными параметрами")
    void testIncomeTransactionCreation() {
        Transaction transaction = new Transaction("Зарплата", 50000, TransactionType.INCOME, "Основная зарплата");

        assertNotNull(transaction.getId());
        assertEquals("Зарплата", transaction.getCategory());
        assertEquals(50000, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals("Основная зарплата", transaction.getDescription());
        assertNotNull(transaction.getDate());
    }

    @Test
    @DisplayName("Создание транзакции расхода с валидными параметрами")
    void testExpenseTransactionCreation() {
        Transaction transaction = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты на неделю");

        assertEquals("Еда", transaction.getCategory());
        assertEquals(5000, transaction.getAmount());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertEquals("Продукты на неделю", transaction.getDescription());
    }

    @Test
    @DisplayName("Создание транзакции с пустым описанием")
    void testTransactionWithEmptyDescription() {
        Transaction transaction = new Transaction("Еда", 5000, TransactionType.EXPENSE, "");

        assertEquals("", transaction.getDescription());
    }

    @Test
    @DisplayName("Создание транзакции с null описанием")
    void testTransactionWithNullDescription() {
        Transaction transaction = new Transaction("Еда", 5000, TransactionType.EXPENSE, null);

        assertEquals("", transaction.getDescription());
    }

    @Test
    @DisplayName("Создание транзакции с невалидными параметрами")
    void testInvalidTransactionCreation() {
        assertThrows(IllegalArgumentException.class, () ->
                new Transaction("", 5000, TransactionType.EXPENSE, "Описание"));

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction("Еда", 0, TransactionType.EXPENSE, "Описание"));

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction("Еда", -100, TransactionType.EXPENSE, "Описание"));

        assertThrows(NullPointerException.class, () ->
                new Transaction("Еда", 5000, null, "Описание"));

        assertThrows(NullPointerException.class, () ->
                new Transaction(null, 5000, TransactionType.EXPENSE, "Описание"));
    }

    @Test
    @DisplayName("Уникальность ID транзакций")
    void testTransactionIdUniqueness() {
        Transaction transaction1 = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");
        Transaction transaction2 = new Transaction("Транспорт", 3000, TransactionType.EXPENSE, "Такси");

        assertNotEquals(transaction1.getId(), transaction2.getId());
    }

    @Test
    @DisplayName("Равенство транзакций по ID")
    void testTransactionEquality() {
        Transaction transaction1 = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");
        Transaction transaction2 = transaction1;

        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    @DisplayName("Транзакции с разными ID не равны")
    void testTransactionInequality() {
        Transaction transaction1 = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");
        Transaction transaction2 = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");

        assertNotEquals(transaction1, transaction2);
    }

    @Test
    @DisplayName("Сравнение транзакции с null и другим типом")
    void testTransactionComparisonWithNullAndOtherTypes() {
        Transaction transaction = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");

        assertNotEquals(null, transaction);
        assertNotEquals("some string", transaction);
    }

    @Test
    @DisplayName("Транзакция не равна null")
    void testTransactionNotEqualToNull() {
        Transaction transaction = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");

        assertNotEquals(null, transaction);
    }

    @Test
    @DisplayName("Рефлексивность равенства транзакций")
    void testTransactionEqualityReflexivity() {
        Transaction transaction = new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты");

        assertEquals(transaction, transaction);
    }
}