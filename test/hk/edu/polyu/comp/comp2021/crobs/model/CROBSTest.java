package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CROBSTest {

    @Test
    void testCROBSConstructor() {
        CROBS crobs = new CROBS();

        assertNotNull(crobs);
    }
}