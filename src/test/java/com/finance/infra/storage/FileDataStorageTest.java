package com.finance.infra.storage;

import com.finance.core.model.User;
import com.finance.core.model.Transaction;
import com.finance.core.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileDataStorageTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Сохранение и загрузка пользователя")
    void testSaveAndLoadUser() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        User user = new User("testuser", "password");
        storage.addUser(user);
        storage.saveData();

        FileDataStorage newStorage = new FileDataStorage(testFile.getAbsolutePath());
        User loadedUser = newStorage.findByUsername("testuser");

        assertNotNull(loadedUser);
        assertEquals("testuser", loadedUser.getUsername());
        assertTrue(loadedUser.authenticate("password"));
    }

    @Test
    @DisplayName("Сохранение и загрузка пользователя с транзакциями")
    void testSaveAndLoadUserWithTransactions() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        User user = new User("testuser", "password");
        user.getWallet().addTransaction(new Transaction("Зарплата", 50000, TransactionType.INCOME, "Основная"));
        user.getWallet().addTransaction(new Transaction("Еда", 5000, TransactionType.EXPENSE, "Продукты"));
        user.getWallet().setBudget("Еда", 10000);

        storage.addUser(user);
        storage.saveData();


        FileDataStorage newStorage = new FileDataStorage(testFile.getAbsolutePath());
        User loadedUser = newStorage.findByUsername("testuser");

        assertNotNull(loadedUser);
        assertEquals(2, loadedUser.getWallet().getTransactions().size());
        assertEquals(45000, loadedUser.getWallet().getBalance());
        assertTrue(loadedUser.getWallet().getBudgets().containsKey("Еда"));
    }

    @Test
    @DisplayName("Сохранение нескольких пользователей")
    void testSaveAndLoadMultipleUsers() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        user1.getWallet().addTransaction(new Transaction("Зарплата", 30000, TransactionType.INCOME, ""));
        user2.getWallet().addTransaction(new Transaction("Бизнес", 50000, TransactionType.INCOME, ""));

        storage.addUser(user1);
        storage.addUser(user2);
        storage.saveData();

        FileDataStorage newStorage = new FileDataStorage(testFile.getAbsolutePath());

        User loaded1 = newStorage.findByUsername("user1");
        User loaded2 = newStorage.findByUsername("user2");

        assertNotNull(loaded1);
        assertNotNull(loaded2);
        assertEquals(30000, loaded1.getWallet().getTotalIncome());
        assertEquals(50000, loaded2.getWallet().getTotalIncome());
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя")
    void testFindNonExistentUser() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        assertNull(storage.findByUsername("nonexistent"));
    }

    @Test
    @DisplayName("Проверка существования пользователя")
    void testUserExists() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        storage.addUser(new User("existing", "pass"));

        assertTrue(storage.exists("existing"));
        assertFalse(storage.exists("nonexistent"));
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testGetAllUsers() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        storage.addUser(new User("user1", "pass1"));
        storage.addUser(new User("user2", "pass2"));

        assertEquals(2, storage.findAll().size());
    }

    @Test
    @DisplayName("Работа с несуществующим файлом")
    void testWithNonExistentFile() {
        File testFile = new File(tempDir.toFile(), "non_existent.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        storage.addUser(new User("testuser", "password"));
        storage.saveData();

        assertTrue(testFile.exists());
    }

    @Test
    @DisplayName("Перезапись существующего пользователя")
    void testOverwriteExistingUser() {
        File testFile = new File(tempDir.toFile(), "test_data.ser");
        FileDataStorage storage = new FileDataStorage(testFile.getAbsolutePath());

        User user1 = new User("sameuser", "pass1");
        user1.getWallet().addTransaction(new Transaction("Зарплата", 10000, TransactionType.INCOME, ""));
        storage.addUser(user1);

        User user2 = new User("sameuser", "pass2");
        user2.getWallet().addTransaction(new Transaction("Бонус", 20000, TransactionType.INCOME, ""));
        storage.addUser(user2);

        User loadedUser = storage.findByUsername("sameuser");

        assertNotNull(loadedUser);
        assertTrue(loadedUser.authenticate("pass2"));
        assertEquals(20000, loadedUser.getWallet().getTotalIncome());
    }
}