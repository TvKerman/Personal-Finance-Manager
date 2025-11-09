package com.finance.model;

import com.finance.core.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Создание пользователя с валидными учетными данными")
    void testUserCreation() {
        User user = new User("testuser", "password123");

        assertEquals("testuser", user.getUsername());
        assertNotNull(user.getWallet());
        assertEquals("testuser", user.getWallet().getUsername());
    }

    @Test
    @DisplayName("Успешная аутентификация с правильным паролем")
    void testSuccessfulAuthentication() {
        User user = new User("testuser", "password123");

        assertTrue(user.authenticate("password123"));
    }

    @Test
    @DisplayName("Неуспешная аутентификация с неправильным паролем")
    void testFailedAuthentication() {
        User user = new User("testuser", "password123");

        assertFalse(user.authenticate("wrongpassword"));
    }

    @Test
    @DisplayName("Создание пользователя с невалидными параметрами")
    void testInvalidUserCreation() {
        assertThrows(IllegalArgumentException.class, () -> new User("", "password"));
        assertThrows(IllegalArgumentException.class, () -> new User("user", ""));
        assertThrows(IllegalArgumentException.class, () -> new User("ab", "password"));
        assertThrows(NullPointerException.class, () -> new User(null, "password"));
        assertThrows(NullPointerException.class, () -> new User("user", null));
    }

    @Test
    @DisplayName("Аутентификация с null паролем")
    void testAuthenticationWithNullPassword() {
        User user = new User("testuser", "password123");

        assertFalse(user.authenticate(null));
    }

    @Test
    @DisplayName("Аутентификация с пустым паролем")
    void testAuthenticationWithEmptyPassword() {
        User user = new User("testuser", "password123");

        assertFalse(user.authenticate(""));
    }

    @Test
    @DisplayName("Создание пользователя с минимально допустимым именем")
    void testUserCreationWithMinimalUsername() {
        User user = new User("abc", "password");

        assertEquals("abc", user.getUsername());
        assertTrue(user.authenticate("password"));
    }

    @Test
    @DisplayName("Кошелек привязан к пользователю")
    void testWalletBoundToUser() {
        User user = new User("testuser", "password");

        assertNotNull(user.getWallet());
        assertEquals("testuser", user.getWallet().getUsername());
    }

    @Test
    @DisplayName("Разные пользователи имеют разные кошельки")
    void testDifferentUsersHaveDifferentWallets() {
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        assertNotSame(user1.getWallet(), user2.getWallet());
        assertEquals("user1", user1.getWallet().getUsername());
        assertEquals("user2", user2.getWallet().getUsername());
    }
}