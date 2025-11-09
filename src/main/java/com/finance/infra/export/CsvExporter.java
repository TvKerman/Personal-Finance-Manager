package com.finance.infra.export;

import com.finance.core.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class CsvExporter implements ReportExporter {
    private static final String DELIMITER = ";";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void export(Wallet wallet, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writeHeader(wallet, writer);
            writeTransactions(wallet, writer);
            writeSummary(wallet, writer);
        }
    }

    private void writeHeader(Wallet wallet, FileWriter writer) throws IOException {
        writer.write("Financial Data Export" + LINE_SEPARATOR);
        writer.write("User: " + escapeCsvField(wallet.getUsername()) + LINE_SEPARATOR);
        writer.write("Export Date: " + java.time.LocalDate.now() + LINE_SEPARATOR);
        writer.write(LINE_SEPARATOR);

        writer.write("Date" + DELIMITER + "Type" + DELIMITER + "Category" +
                DELIMITER + "Amount" + DELIMITER + "Description" + LINE_SEPARATOR);
    }

    private void writeTransactions(Wallet wallet, FileWriter writer) throws IOException {
        for (Transaction transaction : wallet.getTransactions()) {
            writer.write(String.format("%s%s%s%s%s%s%.2f%s%s%s",
                    transaction.getDate().format(DATE_FORMATTER),
                    DELIMITER,
                    transaction.getType(),
                    DELIMITER,
                    escapeCsvField(transaction.getCategory()),
                    DELIMITER,
                    transaction.getAmount(),
                    DELIMITER,
                    escapeCsvField(transaction.getDescription()),
                    LINE_SEPARATOR
            ));
        }
    }

    private void writeSummary(Wallet wallet, FileWriter writer) throws IOException {
        writer.write(LINE_SEPARATOR);
        writer.write("SUMMARY" + LINE_SEPARATOR);
        writer.write("Total Income" + DELIMITER + String.format("%.2f", wallet.getTotalIncome()) + LINE_SEPARATOR);
        writer.write("Total Expenses" + DELIMITER + String.format("%.2f", wallet.getTotalExpenses()) + LINE_SEPARATOR);
        writer.write("Balance" + DELIMITER + String.format("%.2f", wallet.getBalance()) + LINE_SEPARATOR);
    }

    private String escapeCsvField(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }

        if (field.contains(DELIMITER) || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }

    public void exportTransactions(Wallet wallet, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writeHeader(wallet, writer);
            writeTransactions(wallet, writer);
        }
    }

    public void exportBudgets(Wallet wallet, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Budget Report" + LINE_SEPARATOR);
            writer.write("User: " + escapeCsvField(wallet.getUsername()) + LINE_SEPARATOR);
            writer.write(LINE_SEPARATOR);

            writer.write("Category" + DELIMITER + "Limit" + DELIMITER +
                    "Spent" + DELIMITER + "Remaining" + DELIMITER + "Status" + LINE_SEPARATOR);

            for (Budget budget : wallet.getBudgets().values()) {
                String status = budget.isExceeded() ? "EXCEEDED" :
                        budget.isWarningThresholdReached() ? "WARNING" : "OK";

                writer.write(String.format("%s%s%.2f%s%.2f%s%.2f%s%s%s",
                        escapeCsvField(budget.getCategory()),
                        DELIMITER,
                        budget.getLimit(),
                        DELIMITER,
                        budget.getSpent(),
                        DELIMITER,
                        budget.getRemaining(),
                        DELIMITER,
                        status,
                        LINE_SEPARATOR
                ));
            }
        }
    }
}