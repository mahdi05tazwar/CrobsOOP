package hk.edu.polyu.comp.comp2021.crobs.model.users;

public class AdminUser extends User{
    public AdminUser(String id, String name) {
        super(id, name);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public boolean canManageRooms() {
        return true;
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }

    @Override
    public boolean canSeeAllBookingOwners() {
        return true;
    }
}
