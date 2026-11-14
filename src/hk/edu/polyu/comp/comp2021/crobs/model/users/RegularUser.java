package hk.edu.polyu.comp.comp2021.crobs.model.users;

/**
 * Stores a regular user
 */
public class RegularUser extends User {
    /**
     * @param id id of regular user
     * @param name name of regular user
     */
    public RegularUser(String id, String name) {
        super(id, name);
    }

    @Override
    public Role getRole() {
        return Role.REGULAR;
    }
}