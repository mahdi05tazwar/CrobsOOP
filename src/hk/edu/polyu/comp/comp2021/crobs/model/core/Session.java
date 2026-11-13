package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.users.User;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.PermissionDeniedException;

import java.util.Optional;

/**
 * This class holds the current user's session.
 */
public class Session {
    private Optional<User> currentUser = Optional.empty();

    /**
     * @return true if a user is logged in
     */
    boolean isLoggedIn() {
        return currentUser.isPresent();
    }

    /**
     * Log in a user
     * @param user the user to be logged in
     */
    void login(User user) {
        if (isLoggedIn()) throw new ConflictException("A user is already logged in.");
        currentUser = Optional.of(user);
    }

    /**
     * Log out the user
     * @return the user that got logged out
     */
    User logout() {
        if (! isLoggedIn()) throw new PermissionDeniedException("No user is currently logged in.");
        User loggedOut = currentUser.get();
        currentUser = Optional.empty();
        return loggedOut;
    }

    /**
     * @return the current user if logged in
     */
    User requireUser() {
        if (! isLoggedIn()) throw new PermissionDeniedException("Please log in first.");
        return currentUser.get();
    }

    /**
     * @return the current user if logged in and is an admin
     */
    User requireAdmin() {
        requireUser();
        if (! currentUser.get().canManageRooms()) throw new PermissionDeniedException("Current user is not an admin.");
        return currentUser.get();
    }

    /**
     * @return the current user if logged in and is a superadmin
     */
    User requireSuperAdmin() {
        requireUser();
        if (! currentUser.get().canChangeRoles()) throw new PermissionDeniedException("Current user is not a superadmin.");
        return currentUser.get();
    }

    /**
     * @param newUserObj to replace the existing user object (if any) as to refresh/update it
     */
    void refresh(User newUserObj) {
        if (isLoggedIn() && currentUser.get().getId().equals(newUserObj.getId())) currentUser = Optional.of(newUserObj);
    }
}
