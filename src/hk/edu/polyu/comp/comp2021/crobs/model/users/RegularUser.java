package hk.edu.polyu.comp.comp2021.crobs.model.users;

public class RegularUser extends User {
    public RegularUser(String id, String name) {
        super(id, name);
    }

    @Override
    public Role getRole() {
        return Role.REGULAR;
    }
}