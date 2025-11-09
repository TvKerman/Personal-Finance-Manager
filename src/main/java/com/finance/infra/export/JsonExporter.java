package com.finance.infra.export;

import com.finance.core.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JsonExporter {
    private final ObjectMapper objectMapper;

    public JsonExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void exportWallet(Wallet wallet, String filename) throws IOException {
        Map<String, Object> walletData = new HashMap<>();

        walletData.put("username", wallet.getUsername());
        walletData.put("balance", wallet.getBalance());
        walletData.put("totalIncome", wallet.getTotalIncome());
        walletData.put("totalExpenses", wallet.getTotalExpenses());

        walletData.put("transactions", wallet.getTransactions().stream()
                .map(this::transactionToMap)
                .toArray());

        walletData.put("budgets", wallet.getBudgets().values().stream()
                .map(this::budgetToMap)
                .toArray());

        walletData.put("categories", wallet.getCategories());

        objectMapper.writeValue(new FileWriter(filename), walletData);
    }

    private Map<String, Object> transactionToMap(Transaction transaction) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", transaction.getId());
        map.put("type", transaction.getType().toString());
        map.put("category", transaction.getCategory());
        map.put("amount", transaction.getAmount());
        map.put("date", transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("description", transaction.getDescription());
        return map;
    }

    private Map<String, Object> budgetToMap(Budget budget) {
        Map<String, Object> map = new HashMap<>();
        map.put("category", budget.getCategory());
        map.put("limit", budget.getLimit());
        map.put("spent", budget.getSpent());
        map.put("remaining", budget.getRemaining());
        map.put("warningThreshold", budget.getWarningThreshold());
        map.put("exceeded", budget.isExceeded());
        return map;
    }
}