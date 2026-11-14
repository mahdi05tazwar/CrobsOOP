package hk.edu.polyu.comp.comp2021.crobs.model.users;

/**
 * Represents a base user
 */
public abstract class User {
    final String id, name;

    /**
     * @param id id of user
     * @param name name of user
     */
    protected User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the user's ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the user's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return user's role
     */
    public abstract Role getRole();

    /**
     * @return true if it can manage users
     */
    public boolean canManageUsers() {
        return false;
    }

    /**
     * @return true if it can manage rooms
     */
    public boolean canManageRooms() {
        return false;
    }

    /**
     * @return true if the user can see all booking owners
     */
    public boolean canSeeAllBookingOwners() {
        return false;
    }

    /**
     * @return true if the user can change roles
     */
    public boolean canChangeRoles() {
        return false;
    }

    @Override
    public String toString() {
        return id + " (" + name + ", " + getRole().getKeyword() + ")";
    }
}