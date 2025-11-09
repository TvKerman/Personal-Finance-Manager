package com.finance.cli;

import com.finance.core.service.AuthService;
import com.finance.core.service.FinanceService;
import com.finance.infra.storage.DataStorage;
import com.finance.infra.storage.FileDataStorage;
import com.finance.infra.export.CsvExporter;

import java.util.*;

import com.finance.infra.export.JsonExporter;
import com.finance.core.model.Budget;

public class FinanceCLI {
    private final FinanceService financeService;
    private final AuthService authService;
    private final DataStorage dataStorage;
    private final Scanner scanner;
    private final CommandParser commandParser;

    public FinanceCLI(FinanceService financeService, AuthService authService, DataStorage dataStorage) {
        this.financeService = financeService;
        this.authService = authService;
        this.dataStorage = dataStorage;
        this.scanner = new Scanner(System.in);
        this.commandParser = new CommandParser();
    }

    public void run() {
        System.out.println("=== ПРИЛОЖЕНИЕ ДЛЯ УПРАВЛЕНИЯ ЛИЧНЫМИ ФИНАНСАМИ ===");
        printHelp();

        while (true) {
            try {
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("До свидания!");
                    break;
                }

                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                processCommand(input);

            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        dataStorage.saveData();
    }

    private void processCommand(String input) {
        CommandParser.ParsedCommand command = commandParser.parse(input);

        switch (command.command()) {
            case "login":
                handleLogin(command);
                break;
            case "register":
                handleRegister(command);
                break;
            case "logout":
                handleLogout();
                break;
            case "income":
                handleIncome(command);
                break;
            case "expense":
                handleExpense(command);
                break;
            case "budget":
                handleBudget(command);
                break;
            case "update-budget":
                handleUpdateBudget(command);
                break;
            case "remove-budget":
                handleRemoveBudget(command);
                break;
            case "budgets":
                handleBudgetList();
                break;
            case "stats":
                handleStatistics();
                break;
            case "transactions":
                handleTransactions();
                break;
            case "transfer":
                handleTransfer(command);
                break;
            case "export":
                handleExport(command);
                break;
            case "export-json":
                handleJsonExport(command);
                break;
            case "alerts":
                handleAlerts();
                break;
            case "calculate":
                handleCalculate(command);
                break;
            case "near-limit":
                handleNearLimit(command);
                break;
            default:
                System.out.println("Неизвестная команда. Введите 'help' для списка команд.");
        }
    }

    private void handleLogin(CommandParser.ParsedCommand command) {
        if (command.args().size() < 2) {
            System.out.println("Использование: login <username> <password>");
            return;
        }

        String username = command.args().get(0);
        String password = command.args().get(1);

        if (authService.login(username, password)) {
            financeService.setUserWallet(authService.getCurrentUser().getWallet());
            System.out.println("Успешный вход! Добро пожаловать, " + username);
        } else {
            System.out.println("Неверный логин или пароль");
        }
    }

    private void handleRegister(CommandParser.ParsedCommand command) {
        if (command.args().size() < 2) {
            System.out.println("Использование: register <username> <password>");
            return;
        }

        String username = command.args().get(0);
        String password = command.args().get(1);

        if (authService.register(username, password)) {
            System.out.println("Регистрация успешна! Теперь вы можете войти.");
        } else {
            System.out.println("Пользователь с таким логином уже существует");
        }
    }

    private void handleLogout() {
        authService.logout();
        financeService.setUserWallet(null);
        System.out.println("Вы вышли из системы");
    }

    private void handleIncome(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().size() < 2) {
            System.out.println("Использование: income <category> <amount> [description]");
            return;
        }

        String category = command.args().get(0);
        double amount = Double.parseDouble(command.args().get(1));
        String description = command.args().size() > 2 ? command.args().get(2) : "";

        financeService.addIncome(category, amount, description);
        authService.saveChanges();
        System.out.println("Доход добавлен!");
    }

    private void handleExpense(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().size() < 2) {
            System.out.println("Использование: expense <category> <amount> [description]");
            return;
        }

        String category = command.args().get(0);
        double amount = Double.parseDouble(command.args().get(1));
        String description = command.args().size() > 2 ? command.args().get(2) : "";

        financeService.addExpense(category, amount, description);

        authService.saveChanges();
        System.out.println("Расход добавлен!");
        expenseAlert(category);
    }

    private void expenseAlert(String category) {
        if (!financeService.getCurrentWallet().isExistsBudget(category)) {
            System.out.println("Внимание! Бюджет для данной категории отсутствует");
            return;
        }
        String alert = financeService.checkAlerts().stream()
                .filter(s -> s.contains(category))
                .findFirst()
                .orElse(null);
        if (alert != null) {
            System.out.println("=== ПРЕДУПРЕЖДЕНИЕ ===");
            System.out.println("- " + alert);
        }
    }

    private void handleBudget(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().size() < 2) {
            System.out.println("Использование: budget <category> <limit>");
            return;
        }

        String category = command.args().get(0);
        double limit = Double.parseDouble(command.args().get(1));

        financeService.setBudget(category, limit);
        authService.saveChanges();
        System.out.println("Бюджет установлен!");
    }

    private void handleUpdateBudget(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().size() < 2) {
            System.out.println("Использование: update-budget <category> <new-limit>");
            return;
        }

        String category = command.args().get(0);
        double newLimit = Double.parseDouble(command.args().get(1));

        financeService.updateBudget(category, newLimit);
        authService.saveChanges();
        System.out.println("Бюджет обновлен!");
    }

    private void handleRemoveBudget(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().isEmpty()) {
            System.out.println("Использование: remove-budget <category>");
            return;
        }

        String category = command.args().getFirst();
        financeService.removeBudget(category);
        authService.saveChanges();
        System.out.println("Бюджет удален!");
    }

    private void handleBudgetList() {
        checkAuthentication();

        List<Budget> budgets = financeService.getAllBudgets();
        if (budgets.isEmpty()) {
            System.out.println("Бюджеты не установлены");
            return;
        }

        System.out.println("\n=== ВАШИ БЮДЖЕТЫ ===");
        budgets.forEach(budget -> {
            String status = budget.isExceeded() ? "ПРЕВЫШЕН" :
                    budget.isWarningThresholdReached() ? "БЛИЗКО К ЛИМИТУ" : "В НОРМЕ";
            System.out.printf("%s: Лимит %,.2f, Потрачено %,.2f, Осталось %,.2f [%s]%n",
                    budget.getCategory(), budget.getLimit(), budget.getSpent(),
                    budget.getRemaining(), status);
        });
    }

    private void handleStatistics() {
        checkAuthentication();

        var report = financeService.generateReport();
        Formatter.printReport(report);
    }

    private void handleTransactions() {
        checkAuthentication();

        var transactions = financeService.getCurrentWallet().getTransactions();
        if (transactions.isEmpty()) {
            System.out.println("Нет транзакций");
            return;
        }

        Formatter.printTransactionTable(transactions);
    }

    private void handleTransfer(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().size() < 2) {
            System.out.println("Использование: transfer <username> <amount> [description]");
            return;
        }

        String toUsername = command.args().get(0);
        double amount = Double.parseDouble(command.args().get(1));
        String description = command.args().size() > 2 ? command.args().get(2) : "";
        if (!authService.userExists(toUsername)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        financeService.transfer(authService.getUserByUsername(toUsername).getWallet(), amount, description);
        authService.saveChanges();
        System.out.println("Перевод выполнен успешно!");
    }

    private void handleExport(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().isEmpty()) {
            System.out.println("Использование: export <filename> [type]");
            System.out.println("Типы: transactions (по умолчанию), budgets, full");
            return;
        }

        String filename = command.args().get(0);
        String type = command.args().size() > 1 ? command.args().get(1) : "transactions";

        if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }

        try {
            CsvExporter exporter = new CsvExporter();

            switch (type.toLowerCase()) {
                case "transactions":
                    exporter.exportTransactions(financeService.getCurrentWallet(), filename);
                    break;
                case "budgets":
                    exporter.exportBudgets(financeService.getCurrentWallet(), filename);
                    break;
                case "full":
                    exporter.export(financeService.getCurrentWallet(), filename);
                    break;
                default:
                    System.out.println("Неизвестный тип экспорта: " + type);
                    return;
            }

            System.out.println("Данные экспортированы в файл: " + filename);

        } catch (Exception e) {
            System.out.println("Ошибка при экспорте: " + e.getMessage());
        }
    }

    private void handleJsonExport(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().isEmpty()) {
            System.out.println("Использование: export-json <filename>");
            return;
        }

        String filename = command.args().getFirst();
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        try {
            JsonExporter exporter = new JsonExporter();
            exporter.exportWallet(financeService.getCurrentWallet(), filename);
            System.out.println("Данные экспортированы в JSON файл: " + filename);
        } catch (Exception e) {
            System.out.println("Ошибка при экспорте JSON: " + e.getMessage());
        }
    }

    private void handleAlerts() {
        checkAuthentication();

        List<String> alerts = financeService.checkAlerts();
        if (alerts.isEmpty()) {
            System.out.println("Предупреждений нет");
        } else {
            System.out.println("=== ПРЕДУПРЕЖДЕНИЯ ===");
            alerts.forEach(alert -> System.out.println("- " + alert));
        }
    }

    private void handleCalculate(CommandParser.ParsedCommand command) {
        checkAuthentication();

        if (command.args().isEmpty()) {
            System.out.println("Использование: calculate <category1> [category2] ...");
            return;
        }

        Set<String> categories = new HashSet<>(command.args());
        double total = financeService.calculateExpensesForCategories(categories);
        System.out.printf("Общие расходы по выбранным категориям: %.2f%n", total);
    }

    private void handleNearLimit(CommandParser.ParsedCommand command) {
        checkAuthentication();

        double threshold = 80.0;
        if (!command.args().isEmpty()) {
            try {
                threshold = Double.parseDouble(command.args().getFirst());
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат порога. Используется значение по умолчанию 80%");
            }
        }

        List<Budget> nearLimitBudgets = financeService.getBudgetsNearLimit(threshold);
        if (nearLimitBudgets.isEmpty()) {
            System.out.println("Нет бюджетов, близких к лимиту");
        } else {
            System.out.println("=== БЮДЖЕТЫ БЛИЗКИЕ К ЛИМИТУ ===");
            nearLimitBudgets.forEach(budget ->
                    System.out.printf("%s: израсходовано %.1f%% (%,.2f из %,.2f)%n",
                            budget.getCategory(), budget.getUsagePercentage(),
                            budget.getSpent(), budget.getLimit())
            );
        }
    }

    private void checkAuthentication() {
        if (!authService.isLoggedIn()) {
            throw new IllegalStateException("Для выполнения этой команды необходимо войти в систему");
        }
    }

    private void printHelp() {
        System.out.println("""
            Доступные команды:
            
            Аутентификация:
              login <username> <password>    - Войти в систему
              register <username> <password> - Зарегистрироваться
              logout                         - Выйти из системы
            
            Управление финансами:
              income <category> <amount> [description]    - Добавить доход
              expense <category> <amount> [description]   - Добавить расход
              budget <category> <limit>                   - Установить бюджет
              update-budget <category> <new-limit>        - Обновить бюджет
              remove-budget <category>                    - Удалить бюджет
              transfer <username> <amount> [description]  - Перевод пользователю
            
            Отчеты и аналитика:
              stats                         - Показать статистику
              transactions                  - Показать все транзакции
              alerts                        - Показать предупреждения
              calculate <category1> ...     - Посчитать расходы по категориям
              budgets                       - Список всех бюджетов
              near-limit [threshold]        - Бюджеты близкие к лимиту
              export <filename> [type]      - Экспорт в CSV. Типы: transactions (по умолчанию), budgets, full
              export-json <filename>        - Экспорт в JSON
            
            Системные:
              help                          - Показать эту справку
              exit                          - Выйти из приложения
            """);
    }

    public static void main(String[] args) {
        FileDataStorage fileDataStorage = new FileDataStorage("finance_data.ser");
        FinanceService financeService = new FinanceService();
        AuthService authService = new AuthService(fileDataStorage);
        FinanceCLI cli = new FinanceCLI(financeService, authService, fileDataStorage);
        cli.run();
    }
}