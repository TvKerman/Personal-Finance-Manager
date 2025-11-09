package com.finance.service;

import com.finance.core.model.User;
import com.finance.core.repository.UserRepository;
import com.finance.core.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private AuthService authService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authService = new AuthService(userRepository);
    }

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void testSuccessfulRegistration() {
        when(userRepository.exists("newuser")).thenReturn(false);

        boolean result = authService.register("newuser", "password");

        assertTrue(result);
        verify(userRepository).addUser(any(User.class));
    }

    @Test
    @DisplayName("Регистрация существующего пользователя")
    void testRegisterExistingUser() {
        when(userRepository.exists("existing")).thenReturn(true);

        boolean result = authService.register("existing", "password");

        assertFalse(result);
        verify(userRepository, never()).addUser(any(User.class));
    }

    @Test
    @DisplayName("Успешный вход с правильными учетными данными")
    void testSuccessfulLogin() {
        User user = new User("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        boolean result = authService.login("testuser", "password");

        assertTrue(result);
        assertTrue(authService.isLoggedIn());
        assertEquals("testuser", authService.getCurrentUsername());
        assertEquals(user, authService.getCurrentUser());
    }

    @Test
    @DisplayName("Неуспешный вход с неверным паролем")
    void testFailedLoginWithWrongPassword() {
        User user = new User("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        boolean result = authService.login("testuser", "wrongpassword");

        assertFalse(result);
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Неуспешный вход несуществующего пользователя")
    void testFailedLoginWithNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        boolean result = authService.login("nonexistent", "password");

        assertFalse(result);
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Выход из системы")
    void testLogout() {
        User user = new User("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        authService.login("testuser", "password");

        authService.logout();

        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUsername());
        assertNull(authService.getCurrentUser());
    }

    @Test
    @DisplayName("Проверка существования пользователя")
    void testUserExists() {
        when(userRepository.exists("existing")).thenReturn(true);
        when(userRepository.exists("nonexistent")).thenReturn(false);

        assertTrue(authService.userExists("existing"));
        assertFalse(authService.userExists("nonexistent"));
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(
                new User("user1", "pass1"),
                new User("user2", "pass2")
        ));

        var users = authService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }

    @Test
    @DisplayName("Регистрация с невалидным именем пользователя")
    void testRegistrationWithInvalidUsername() {
        boolean result = authService.register("", "password");

        assertFalse(result);
        verify(userRepository, never()).addUser(any(User.class));
    }

    @Test
    @DisplayName("Регистрация с невалидным паролем")
    void testRegistrationWithInvalidPassword() {
        boolean result = authService.register("user", "");

        assertFalse(result);
        verify(userRepository, never()).addUser(any(User.class));
    }
}