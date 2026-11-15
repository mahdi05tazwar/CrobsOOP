package hk.edu.polyu.comp.comp2021.crobs.model.users;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserManagerTest {

    private UserManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    // ---------- createSuperAdmin ----------

    @Test
    void createSuperAdminGetsFirstId() {
        SuperAdmin sa = manager.createSuperAdmin();
        assertEquals("U0001", sa.getId());
        assertEquals("superadmin", sa.getName());
        assertSame(sa, manager.getUser("U0001"));
    }

    @Test
    void createSuperAdminTwiceThrows() {
        manager.createSuperAdmin();
        assertThrows(ConflictException.class, () -> manager.createSuperAdmin());
    }

    // ---------- addRegularUser ----------

    @Test
    void addRegularUserGivesSequentialIds() {
        manager.createSuperAdmin();
        RegularUser alice = manager.addRegularUser("alice");
        RegularUser bob = manager.addRegularUser("bob");
        assertEquals("U0002", alice.getId());
        assertEquals("U0003", bob.getId());
        assertEquals("alice", alice.getName());
        assertEquals(Role.REGULAR, alice.getRole());
    }

    @Test
    void addRegularUserAllowsDuplicateNames() {
        RegularUser a = manager.addRegularUser("alice");
        RegularUser b = manager.addRegularUser("alice");
        assertFalse(a.getId().equals(b.getId()));
    }

    @Test
    void addRegularUserRejectsNullName() {
        assertThrows(InvalidInputException.class, () -> manager.addRegularUser(null));
    }

    @Test
    void addRegularUserRejectsBlankName() {
        assertThrows(InvalidInputException.class, () -> manager.addRegularUser(""));
        assertThrows(InvalidInputException.class, () -> manager.addRegularUser("   "));
    }

    @Test
    void failedAddDoesNotUseUpAnId() {
        assertThrows(InvalidInputException.class, () -> manager.addRegularUser(""));
        assertEquals("U0001", manager.addRegularUser("alice").getId());
    }

    // ---------- getUser / hasUser ----------

    @Test
    void getUserFindsExistingUser() {
        RegularUser alice = manager.addRegularUser("alice");
        assertSame(alice, manager.getUser(alice.getId()));
    }

    @Test
    void getUnknownUserThrows() {
        assertThrows(NotFoundException.class, () -> manager.getUser("U9999"));
    }

    @Test
    void hasUserReportsExistence() {
        RegularUser alice = manager.addRegularUser("alice");
        assertTrue(manager.hasUser(alice.getId()));
        assertFalse(manager.hasUser("U9999"));
    }

    // ---------- removeUser ----------

    @Test
    void removeRegularUser() {
        RegularUser alice = manager.addRegularUser("alice");
        assertSame(alice, manager.removeUser(alice.getId()));
        assertFalse(manager.hasUser(alice.getId()));
    }

    @Test
    void removeAdminThrows() {
        String id = manager.addRegularUser("alice").getId();
        manager.changeRole(id, Role.ADMIN);
        assertThrows(ConflictException.class, () -> manager.removeUser(id));
        assertTrue(manager.hasUser(id));
    }

    @Test
    void removeSuperAdminThrows() {
        String id = manager.createSuperAdmin().getId();
        assertThrows(ConflictException.class, () -> manager.removeUser(id));
        assertTrue(manager.hasUser(id));
    }

    @Test
    void removeUnknownUserThrows() {
        assertThrows(NotFoundException.class, () -> manager.removeUser("U9999"));
    }

    @Test
    void removedIdIsNotReused() {
        String id = manager.addRegularUser("alice").getId();
        manager.removeUser(id);
        assertFalse(id.equals(manager.addRegularUser("bob").getId()));
    }

    // ---------- changeRole ----------

    @Test
    void promoteRegularToAdmin() {
        String id = manager.addRegularUser("alice").getId();
        User promoted = manager.changeRole(id, Role.ADMIN);

        assertInstanceOf(AdminUser.class, promoted);
        assertEquals(Role.ADMIN, promoted.getRole());
        assertEquals(id, promoted.getId());
        assertEquals("alice", promoted.getName());
        assertSame(promoted, manager.getUser(id));
    }

    @Test
    void demoteAdminToRegular() {
        String id = manager.addRegularUser("alice").getId();
        manager.changeRole(id, Role.ADMIN);
        User demoted = manager.changeRole(id, Role.REGULAR);

        assertEquals(Role.REGULAR, demoted.getRole());
        assertFalse(demoted.canManageRooms());
        assertSame(demoted, manager.getUser(id));
    }

    @Test
    void changeRoleToSuperAdminThrows() {
        String id = manager.addRegularUser("alice").getId();
        assertThrows(InvalidInputException.class, () -> manager.changeRole(id, Role.SUPER_ADMIN));
        assertEquals(Role.REGULAR, manager.getUser(id).getRole());
    }

    @Test
    void changeSuperAdminRoleThrows() {
        String id = manager.createSuperAdmin().getId();
        assertThrows(ConflictException.class, () -> manager.changeRole(id, Role.ADMIN));
        assertEquals(Role.SUPER_ADMIN, manager.getUser(id).getRole());
    }

    @Test
    void changeToSameRoleThrows() {
        String id = manager.addRegularUser("alice").getId();
        assertThrows(ConflictException.class, () -> manager.changeRole(id, Role.REGULAR));
    }

    @Test
    void changeRoleOfUnknownUserThrows() {
        assertThrows(NotFoundException.class, () -> manager.changeRole("U9999", Role.ADMIN));
    }

    // ---------- replace / restore (used by undo) ----------

    @Test
    void replacePutsOldObjectBack() {
        RegularUser original = manager.addRegularUser("alice");
        manager.changeRole(original.getId(), Role.ADMIN);

        manager.replace(original);
        assertSame(original, manager.getUser(original.getId()));
    }

    @Test
    void replaceUnknownUserThrows() {
        assertThrows(NotFoundException.class, () -> manager.replace(new RegularUser("U9999", "ghost")));
    }

    @Test
    void replaceNullThrows() {
        assertThrows(NotFoundException.class, () -> manager.replace(null));
    }

    @Test
    void restoreBringsRemovedUserBack() {
        RegularUser alice = manager.addRegularUser("alice");
        manager.removeUser(alice.getId());

        manager.restore(alice);
        assertSame(alice, manager.getUser(alice.getId()));
    }

    @Test
    void restoreNullDoesNothing() {
        manager.restore(null);
        assertFalse(manager.hasUser("U0001"));
    }

    // ---------- create ----------

    @Test
    void createBuildsTheRightClassForEachRole() {
        assertInstanceOf(RegularUser.class, UserManager.create("U0005", "eve", Role.REGULAR));
        assertEquals(Role.REGULAR, UserManager.create("U0005", "eve", Role.REGULAR).getRole());
        assertEquals(Role.ADMIN, UserManager.create("U0005", "eve", Role.ADMIN).getRole());
        assertEquals(Role.SUPER_ADMIN, UserManager.create("U0005", "eve", Role.SUPER_ADMIN).getRole());
    }

    @Test
    void createKeepsIdAndName() {
        User u = UserManager.create("U0005", "eve", Role.ADMIN);
        assertEquals("U0005", u.getId());
        assertEquals("eve", u.getName());
    }
}
