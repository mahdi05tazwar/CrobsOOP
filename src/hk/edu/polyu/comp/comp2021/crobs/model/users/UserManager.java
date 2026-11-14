package hk.edu.polyu.comp.comp2021.crobs.model.users;

import hk.edu.polyu.comp.comp2021.crobs.model.core.IdGenerator;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages user creation, lookup, role modification, and deletion.
 */
public class UserManager {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final IdGenerator ids = new IdGenerator("U");
    private boolean superAdminCreated = false;

    /**
     * Creates a superadmin user
     * @return the created superadmin
     */
    public SuperAdmin createSuperAdmin() {
        if (superAdminCreated) {
            throw new ConflictException("Superadmin already created");
        }
        String id = ids.next();
        SuperAdmin sa = new SuperAdmin(id, "superadmin");
        users.put(id, sa);
        superAdminCreated = true;
        return sa;
    }

    /**
     * Adds a regular user
     * @param name name of the new user
     * @return the created user
     */
    public RegularUser addRegularUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Regular user name cannot be empty");
        }
        String id = ids.next();
        RegularUser ra = new RegularUser(id, name);
        users.put(id, ra);
        return ra;
    }

    /**
     * Returns a user
     * @param id id of user
     * @return the user the id points to
     */
    public User getUser(String id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("User not found: " + id);
        }
        return user;
    }

    /**
     * checks if a user exists
     * @param id id of user
     * @return if user with this id exists
     */
    public boolean hasUser (String id) {
        return users.containsKey(id);
    }

    /**
     * removes a user
     * @param id id of user needed to be removed
     * @return the removed user
     */
    public User removeUser(String id) {
        User user = getUser(id);
        if (user.getRole() != Role.REGULAR) {
            throw new ConflictException("Only regular users can be removed.");
        }
        return users.remove(id);
    }

    /**
     * Changes role of user
     * @param id id of user whose role is to be changed
     * @param newRole new role to be assigned
     * @return affected user
     */
    public User changeRole(String id, Role newRole) {
        if (newRole == Role.SUPER_ADMIN) {
            throw new InvalidInputException("Cannot change to superadmin role.");
        }
        User user = getUser(id);
        if (user instanceof SuperAdmin) {
            throw new ConflictException("Cannot change superadmin role.");
        }
        if (user.getRole() == newRole) {
            throw new ConflictException("User already has role " + newRole.getKeyword());
        }
        User newUser = create(user.getId(), user.getName(), newRole);
        users.put(id, newUser);
        return newUser;
    }

    /**
     * Replaces one user with another
     * @param user replacement user
     */
    public void replace(User user) {
        if (user == null || !users.containsKey(user.getId())) {
            throw new NotFoundException("User not found for replacement");
        }
        users.put(user.getId(), user);
    }

    /**
     * Restores a deleted user
     * @param user user to be restored
     */
    public void restore (User user) {
        if (user == null) return;
        users.put(user.getId(), user);
    }

    /**
     * Creates a user
     * @param id id of new user
     * @param name name of new user
     * @param role role of new user
     * @return created user
     */
    public static User create(String id, String name, Role role) {
        switch (role) {
            case REGULAR:
                return new RegularUser(id, name);
            case ADMIN:
                return new AdminUser(id, name);
            case SUPER_ADMIN:
                return new SuperAdmin(id, name);
            default:
                throw new InvalidInputException("Unknown role type");
        }
    }
}