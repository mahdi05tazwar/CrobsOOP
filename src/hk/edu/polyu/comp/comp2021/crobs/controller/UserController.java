package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.core.CrobsSystem;
import hk.edu.polyu.comp.comp2021.crobs.model.users.Role;
import hk.edu.polyu.comp.comp2021.crobs.model.users.User;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import hk.edu.polyu.comp.comp2021.crobs.view.ParsedCommand;

import java.util.List;

/**
 * Controller handling user-related commands (login, logout, addUser, removeUser, changeRole).
 */
public class UserController {
    private final CrobsSystem system;

    public UserController(CrobsSystem system, CommandController registry) {
        this.system = system;
        registry.register("login", this::login);
        registry.register("logout", this::logout);
        registry.register("addUser", this::addUser);
        registry.register("removeUser", this::removeUser);
        registry.register("changeRole", this::changeRole);
    }

    private String login(ParsedCommand cmd) {
        cmd.requireArgs(1, "login <userId>");
        User user = system.login(cmd.arg(0));
        return "Logged in as " + OutputFormatter.formatUser(user) + ".";
    }

    private String logout(ParsedCommand cmd) {
        cmd.requireArgs(0, "logout");
        User user = system.logout();
        return "Logged out " + user.getId() + ".";
    }

    private String addUser(ParsedCommand cmd) {
        cmd.requireArgs(1, "addUser <name>");
        User user = system.addUser(cmd.arg(0));
        return "Created user " + user.getId() + " (" + user.getName() + ").";
    }

    private String removeUser(ParsedCommand cmd) {
        cmd.requireArgs(1, "removeUser <userId>");
        List<?> cancelled = system.removeUser(cmd.arg(0));
        return "Removed user " + cmd.arg(0) + "; cancelled " + cancelled.size() + " booking(s).";
    }

    private String changeRole(ParsedCommand cmd) {
        cmd.requireArgs(2, "changeRole <userId> <newRole>");
        String userId = cmd.arg(0);
        Role newRole = Role.fromString(cmd.arg(1));
        system.changeRole(userId, newRole.getKeyword());
        return userId + " is now " + newRole.getKeyword() + ".";
    }
}