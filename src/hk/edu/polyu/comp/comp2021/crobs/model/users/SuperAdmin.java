package hk.edu.polyu.comp.comp2021.crobs.model.users;

/**
 * Stores a superadmin user
 */
public class SuperAdmin extends AdminUser {
    /**
     * @param id id of superadmin
     * @param name name of superadmin
     */
    public SuperAdmin(String id, String name) {
        super(id, name);
    }

    @Override
    public Role getRole() {
        return Role.SUPER_ADMIN;
    }

    @Override
    public boolean canChangeRoles() {
        return true;
    }
}