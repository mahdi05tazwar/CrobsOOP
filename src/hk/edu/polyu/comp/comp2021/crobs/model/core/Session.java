package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.core.users.User;
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
    public boolean isLoggedIn() {
        return currentUser.isPresent();
    }

    /**
     * Log in a user
     * @param user the user to be logged in
     */
    public void login(User user) {
        if (isLoggedIn()) throw new ConflictException("A user is already logged in.");
        currentUser = Optional.of(user);
    }

    /**
     * Log out the user
     * @return the user that got logged out
     */
    public User logout() {
        if (! isLoggedIn()) throw new PermissionDeniedException("No user is currently logged in.");
        User loggedOut = currentUser.get();
        currentUser = Optional.empty();
        return loggedOut;
    }

    /**
     * @return the current user if logged in
     */
    public User requireUser() {
        if (! isLoggedIn()) throw new PermissionDeniedException("Please log in first.");
        return currentUser.get();
    }

    /**
     * @return the current user if logged in and is an admin
     */
    public User requireAdmin() {
        requireUser();
        if (! currentUser.get().canManageRooms()) throw new PermissionDeniedException("Current user is not an admin.");
        return currentUser.get();
    }

    /**
     * @return the current user if logged in and is a superadmin
     */
    public User requireSuperAdmin() {
        requireUser();
        if (! currentUser.get().canChangeRoles()) throw new PermissionDeniedException("Current user is no a superadmin.");
    }

    /**
     * @param newUserObj to replace the existing user object (if any) as to refresh/update it
     */
    public void refresh(User newUserObj) {
        if (isLoggedIn() && currentUser.get().getId().equals(newUserObj.getId())) currentUser = Optional.of(newUserObj);
    }
}
