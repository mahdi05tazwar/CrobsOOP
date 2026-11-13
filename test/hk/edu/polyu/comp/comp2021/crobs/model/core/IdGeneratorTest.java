package hk.edu.polyu.comp.comp2021.crobs.model.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdGeneratorTest {

    @Test
    void firstIdIsOneWithFourDigitPadding() {
        IdGenerator ids = new IdGenerator("R");
        assertEquals("R0001", ids.next());
    }

    @Test
    void consecutiveIdsIncrease() {
        IdGenerator ids = new IdGenerator("B");
        assertEquals("B0001", ids.next());
        assertEquals("B0002", ids.next());
        assertEquals("B0003", ids.next());
    }

    @Test
    void consecutiveIdsAreUnique() {
        IdGenerator ids = new IdGenerator("U");
        assertNotEquals(ids.next(), ids.next());
    }

    @Test
    void separateGeneratorsCountIndependently() {
        IdGenerator rooms = new IdGenerator("R");
        IdGenerator users = new IdGenerator("U");
        rooms.next();
        rooms.next();
        assertEquals("U0001", users.next());
        assertEquals("R0003", rooms.next());
    }

    @Test
    void idsKeepGrowingPastFourDigits() {
        IdGenerator ids = new IdGenerator("R");
        String last = "";
        for (int i = 0; i < 10000; i++) {
            last = ids.next();
        }
        assertEquals("R10000", last);
    }

    @Test
    void idOrderComparesNumericallyForEqualLength() {
        Comparator<String> order = IdGenerator.idOrder();
        assertTrue(order.compare("R0002", "R0010") < 0);
        assertTrue(order.compare("R0010", "R0002") > 0);
        assertEquals(0, order.compare("R0005", "R0005"));
    }

    @Test
    void idOrderPutsShorterIdsFirst() {
        Comparator<String> order = IdGenerator.idOrder();
        assertTrue(order.compare("R9999", "R10000") < 0);
        assertTrue(order.compare("R10000", "R9999") > 0);
    }

    @Test
    void idOrderSortsAList() {
        List<String> list = new ArrayList<>(Arrays.asList("R10000", "R0010", "R0002", "R0001"));
        Collections.sort(list, IdGenerator.idOrder());
        assertEquals(Arrays.asList("R0001", "R0002", "R0010", "R10000"), list);
    }
}
