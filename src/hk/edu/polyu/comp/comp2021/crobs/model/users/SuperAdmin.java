package hk.edu.polyu.comp.comp2021.crobs.model.users;

public class SuperAdmin extends User {
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