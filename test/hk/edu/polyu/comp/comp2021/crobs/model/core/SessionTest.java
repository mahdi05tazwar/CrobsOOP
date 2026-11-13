package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.PermissionDeniedException;
import hk.edu.polyu.comp.comp2021.crobs.model.users.AdminUser;
import hk.edu.polyu.comp.comp2021.crobs.model.users.RegularUser;
import hk.edu.polyu.comp.comp2021.crobs.model.users.SuperAdmin;
import hk.edu.polyu.comp.comp2021.crobs.model.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTest {

    private Session session;
    private User regular;
    private User admin;
    private User superAdmin;

    @BeforeEach
    void setUp() {
        session = new Session();
        regular = new RegularUser("U0002", "alice");
        admin = new AdminUser("U0003", "bob");
        superAdmin = new SuperAdmin("U0001", "superadmin");
    }

    @Test
    void newSessionHasNoUser() {
        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginStoresUser() {
        session.login(regular);
        assertTrue(session.isLoggedIn());
        assertSame(regular, session.requireUser());
    }

    @Test
    void loginWhileLoggedInThrows() {
        session.login(regular);
        assertThrows(ConflictException.class, () -> session.login(admin));
        assertSame(regular, session.requireUser());
    }

    @Test
    void logoutReturnsUserAndClearsSession() {
        session.login(regular);
        assertSame(regular, session.logout());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void logoutWithoutLoginThrows() {
        assertThrows(PermissionDeniedException.class, () -> session.logout());
    }

    @Test
    void canLogInAgainAfterLogout() {
        session.login(regular);
        session.logout();
        session.login(admin);
        assertSame(admin, session.requireUser());
    }

    @Test
    void requireUserWithoutLoginThrows() {
        assertThrows(PermissionDeniedException.class, () -> session.requireUser());
    }

    @Test
    void requireAdminWithoutLoginThrows() {
        assertThrows(PermissionDeniedException.class, () -> session.requireAdmin());
    }

    @Test
    void requireAdminRejectsRegularUser() {
        session.login(regular);
        assertThrows(PermissionDeniedException.class, () -> session.requireAdmin());
    }

    @Test
    void requireAdminAcceptsAdmin() {
        session.login(admin);
        assertSame(admin, session.requireAdmin());
    }

    @Test
    void requireAdminAcceptsSuperAdmin() {
        session.login(superAdmin);
        assertSame(superAdmin, session.requireAdmin());
    }

    @Test
    void requireSuperAdminWithoutLoginThrows() {
        assertThrows(PermissionDeniedException.class, () -> session.requireSuperAdmin());
    }

    @Test
    void requireSuperAdminRejectsAdmin() {
        session.login(admin);
        assertThrows(PermissionDeniedException.class, () -> session.requireSuperAdmin());
    }

    @Test
    void requireSuperAdminRejectsRegularUser() {
        session.login(regular);
        assertThrows(PermissionDeniedException.class, () -> session.requireSuperAdmin());
    }

    @Test
    void requireSuperAdminAcceptsSuperAdmin() {
        session.login(superAdmin);
        assertSame(superAdmin, session.requireSuperAdmin());
    }

    @Test
    void refreshReplacesCurrentUserWithSameId() {
        session.login(regular);
        User promoted = new AdminUser("U0002", "alice");
        session.refresh(promoted);
        assertSame(promoted, session.requireUser());
        assertSame(promoted, session.requireAdmin());
    }

    @Test
    void refreshIgnoresDifferentId() {
        session.login(regular);
        session.refresh(admin);
        assertSame(regular, session.requireUser());
    }

    @Test
    void refreshWhileLoggedOutDoesNothing() {
        session.refresh(regular);
        assertFalse(session.isLoggedIn());
    }
}
