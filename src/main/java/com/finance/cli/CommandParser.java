package com.finance.cli;

import java.util.*;

public class CommandParser {

    public ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Команда не может быть пустой");
        }

        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("\"") && !inQuotes) {
                inQuotes = true;
                currentArg.append(part.substring(1));
            } else if (part.endsWith("\"") && inQuotes) {
                inQuotes = false;
                currentArg.append(" ").append(part, 0, part.length() - 1);
                args.add(currentArg.toString());
                currentArg = new StringBuilder();
            } else if (inQuotes) {
                currentArg.append(" ").append(part);
            } else {
                args.add(part);
            }
        }

        if (inQuotes && !currentArg.isEmpty()) {
            args.add(currentArg.toString());
        }

        return new ParsedCommand(command, args);
    }

    public record ParsedCommand(String command, List<String> args) {
            public ParsedCommand(String command, List<String> args) {
                this.command = command;
                this.args = Collections.unmodifiableList(args);
            }
        }
}