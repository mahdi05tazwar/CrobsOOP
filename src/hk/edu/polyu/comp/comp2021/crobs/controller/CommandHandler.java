package hk.edu.polyu.comp.comp2021.crobs.controller;

/**
 * Interface to handle a parsed command
 */
@FunctionalInterface
public interface CommandHandler {
    /**
     * The method that will handle it (the only method)
     * @param cmd the parsed command
     * @return the command output
     */
    String handle(ParsedCommand cmd);
}
