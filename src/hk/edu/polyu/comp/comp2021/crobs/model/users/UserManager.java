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

    public RegularUser addRegularUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Regular user name cannot be empty");
        }
        String id = ids.next();
        RegularUser ra = new RegularUser(id, name);
        users.put(id, ra);
        return ra;
    }

    public User getUser(String id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("User not found: " + id);
        }
        return user;
    }

    public boolean hasUser (String id) {
        return users.containsKey(id);
    }

    public User removeUser(String id) {
        User user = getUser(id);
        if (user.getRole() != Role.REGULAR) {
            throw new ConflictException("Only regular users can be removed.");
        }
        return users.remove(id);
    }

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

    public void replace(User user) {
        if (user == null || !users.containsKey(user.getId())) {
            throw new NotFoundException("User not found for replacement");
        }
        users.put(user.getId(), user);
    }

    public void restore (User user) {
        if (user == null) return;
        users.put(user.getId(), user);
    }

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