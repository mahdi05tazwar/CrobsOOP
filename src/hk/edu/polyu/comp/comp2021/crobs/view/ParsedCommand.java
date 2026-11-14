package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable representation of a parsed user command containing its name,
 * positional arguments, and trimmed raw command line.
 */
public final class ParsedCommand {
    private static final int MIN_ARG_INDEX = 0;

    private final String name;
    private final List<String> args;
    private final String rawLine;

    /**
     * Constructs a {@code ParsedCommand}.
     *
     * @param name    the command name token
     * @param args    positional argument list
     * @param rawLine raw input command line
     */
    public ParsedCommand(String name, List<String> args, String rawLine) {
        this.name = (name == null) ? "" : name;
        this.args = (args == null)
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(args));
        this.rawLine = (rawLine == null) ? "" : rawLine;
    }

    /**
     * Returns the command name.
     *
     * @return the command name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the raw command line.
     *
     * @return the raw command line
     */
    public String getRawLine() {
        return rawLine;
    }

    /**
     * Returns the number of command arguments.
     *
     * @return argument count
     */
    public int argCount() {
        return args.size();
    }

    /**
     * Returns the argument at index {@code i}.
     *
     * @param i zero-based argument index
     * @return the argument token at index {@code i}
     */
    public String arg(int i) {
        return args.get(i);
    }

    /**
     * Joins arguments from index {@code i} onward using single spaces.
     *
     * @param i starting zero-based index
     * @return joined arguments string, or empty string if {@code i} is at or past {@code argCount()}
     * @throws IndexOutOfBoundsException if {@code i} is negative
     */
    public String joinFrom(int i) {
        if (i < MIN_ARG_INDEX) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + i);
        }
        if (i >= args.size()) {
            return "";
        }
        return String.join(" ", args.subList(i, args.size()));
    }

    /**
     * Ensures that the argument count matches {@code exact}.
     *
     * @param exact expected exact argument count
     * @param usage usage string for error reporting
     * @throws InvalidInputException if {@code argCount()} does not equal {@code exact}
     */
    public void requireArgs(int exact, String usage) {
        if (args.size() != exact) {
            throw new InvalidInputException("Usage: " + usage);
        }
    }

    /**
     * Ensures that the argument count is at least {@code min}.
     *
     * @param min   minimum required argument count
     * @param usage usage string for error reporting
     * @throws InvalidInputException if {@code argCount()} is less than {@code min}
     */
    public void requireMinArgs(int min, String usage) {
        if (args.size() < min) {
            throw new InvalidInputException("Usage: " + usage);
        }
    }
}