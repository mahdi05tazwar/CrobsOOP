package hk.edu.polyu.comp.comp2021.crobs.model.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void storesIdAndName() {
        User u = new RegularUser("U0002", "alice");
        assertEquals("U0002", u.getId());
        assertEquals("alice", u.getName());
    }

    @Test
    void regularUserHasNoPrivileges() {
        User u = new RegularUser("U0002", "alice");
        assertEquals(Role.REGULAR, u.getRole());
        assertFalse(u.canManageUsers());
        assertFalse(u.canManageRooms());
        assertFalse(u.canSeeAllBookingOwners());
        assertFalse(u.canChangeRoles());
    }

    @Test
    void adminCanManageButNotChangeRoles() {
        User u = new AdminUser("U0003", "bob");
        assertEquals(Role.ADMIN, u.getRole());
        assertTrue(u.canManageUsers());
        assertTrue(u.canManageRooms());
        assertTrue(u.canSeeAllBookingOwners());
        assertFalse(u.canChangeRoles());
    }

    @Test
    void superAdminHasEveryPrivilege() {
        User u = new SuperAdmin("U0001", "superadmin");
        assertEquals(Role.SUPER_ADMIN, u.getRole());
        assertTrue(u.canManageUsers());
        assertTrue(u.canManageRooms());
        assertTrue(u.canSeeAllBookingOwners());
        assertTrue(u.canChangeRoles());
    }

    @Test
    void toStringShowsIdNameAndRole() {
        assertEquals("U0002 (alice, regular)", new RegularUser("U0002", "alice").toString());
        assertEquals("U0003 (bob, admin)", new AdminUser("U0003", "bob").toString());
        assertEquals("U0001 (superadmin, superadmin)", new SuperAdmin("U0001", "superadmin").toString());
    }
}
