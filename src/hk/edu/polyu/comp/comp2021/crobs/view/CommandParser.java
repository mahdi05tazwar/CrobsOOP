package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing raw command strings into {@link ParsedCommand} instances.
 */
public final class CommandParser {
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final int COMMAND_NAME_INDEX = 0;
    private static final int FIRST_ARG_INDEX = 1;

    private CommandParser() {
    }

    /**
     * Trims and splits {@code line} on whitespace, preserving command name case.
     *
     * @param line raw command string
     * @return the parsed {@link ParsedCommand}
     * @throws InvalidInputException if {@code line} is null or blank
     */
    public static ParsedCommand parse(String line) {
        if (line == null) {
            throw new InvalidInputException("Command cannot be blank");
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidInputException("Command cannot be blank");
        }
        String[] tokens = trimmed.split(WHITESPACE_REGEX);
        String name = tokens[COMMAND_NAME_INDEX];
        List<String> args = new ArrayList<>();
        for (int i = FIRST_ARG_INDEX; i < tokens.length; i++) {
            args.add(tokens[i]);
        }
        return new ParsedCommand(name, args, trimmed);
    }
}