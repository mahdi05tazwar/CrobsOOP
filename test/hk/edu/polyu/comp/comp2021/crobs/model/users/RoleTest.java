package hk.edu.polyu.comp.comp2021.crobs.model.users;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleTest {

    @Test
    void keywordsMatchCommandSyntax() {
        assertEquals("regular", Role.REGULAR.getKeyword());
        assertEquals("admin", Role.ADMIN.getKeyword());
        assertEquals("superadmin", Role.SUPER_ADMIN.getKeyword());
    }

    @Test
    void fromStringFindsEachRole() {
        assertSame(Role.REGULAR, Role.fromString("regular"));
        assertSame(Role.ADMIN, Role.fromString("admin"));
        assertSame(Role.SUPER_ADMIN, Role.fromString("superadmin"));
    }

    @Test
    void fromStringIgnoresCase() {
        assertSame(Role.ADMIN, Role.fromString("ADMIN"));
        assertSame(Role.REGULAR, Role.fromString("Regular"));
    }

    @Test
    void fromStringRejectsUnknownRole() {
        assertThrows(InvalidInputException.class, () -> Role.fromString("king"));
        assertThrows(InvalidInputException.class, () -> Role.fromString(""));
    }

    @Test
    void fromStringRejectsNull() {
        assertThrows(InvalidInputException.class, () -> Role.fromString(null));
    }
}
