package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.core.CrobsSystem;
import hk.edu.polyu.comp.comp2021.crobs.model.core.UndoableAction;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.util.CommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.view.CommandParser;
import hk.edu.polyu.comp.comp2021.crobs.view.ParsedCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For parsing and running user commands
 */
public class CommandController {
    private final CrobsSystem system;
    private final Map<String, CommandHandler> handlers;
    private final List<CommandLogger> loggers;
    private boolean running = true, alreadyShutDown = false;

    /**
     * Constructor to initialise the controller
     *
     * @param system the CrobsSystem object
     * @param loggers the list of command loggers
     */
    public CommandController(CrobsSystem system, List<CommandLogger> loggers) {
        this.system = system;
        this.loggers = loggers;
        handlers = new HashMap<>();

        register("undo", this::undo);
        register("redo", this::redo);
        register("quit", this::quit);
    }

    /**
     * Registers a command
     *
     * @param name name of command
     * @param handler handler for the command
     */
    public void register(String name, CommandHandler handler) {
        handlers.put(name, handler);
    }

    /**
     * @param line String to be parsed and executed
     * @return command result
     */
    public String execute(String line) {
        if (line == null || line.trim().isEmpty()) return "";
        try {
            ParsedCommand cmd = CommandParser.parse(line);
            CommandHandler handler = handlers.get(cmd.getName());
            if (handler == null) return  "Error! Couldn't recognise command '" + cmd.getName() + "'";
            String res = handler.handle(cmd);
            log(cmd.getRawLine());
            return res;
        }
        catch (CrobsException e) {
            return "Error! " + e.getMessage();
        }
    }

    /**
     * Checks whether the command loop is currently running
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Shuts down all command loggers
     */
    public void shutdown() {
        if (alreadyShutDown) return;
        for (CommandLogger logger : loggers) logger.close();
        alreadyShutDown = true;
    }

    // private methods
    private String quit(ParsedCommand cmd) {
        cmd.requireArgs(0, "quit");
        running = false;
        return "Goodbye.";
    }
    private String undo(ParsedCommand cmd) {
        cmd.requireArgs(0, "undo");
        UndoableAction action = system.undo();
        return "Undone: " + action.describe();
    }
    private String redo(ParsedCommand cmd) {
        cmd.requireArgs(0, "redo");
        UndoableAction action = system.redo();
        return "Redone: " + action.describe();
    }
    private void log(String command) {
        for (CommandLogger logger : loggers) {
            logger.log(command);
        }
    }
}
