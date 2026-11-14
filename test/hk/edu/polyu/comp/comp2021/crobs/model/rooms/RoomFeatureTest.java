package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomFeatureTest {
    @Test
    void constructorNormalizesToLowerCase() {
        RoomFeature feature = new RoomFeature("Projector");
        assertEquals("projector", feature.getName());
        assertEquals("projector", feature.toString());
    }

    @Test
    void constructorRejectsNullEmptyAndWhitespace() {
        assertThrows(InvalidInputException.class, () -> new RoomFeature(null));
        assertThrows(InvalidInputException.class, () -> new RoomFeature(""));
        assertThrows(InvalidInputException.class, () -> new RoomFeature(" "));
        assertThrows(InvalidInputException.class, () -> new RoomFeature("smart board"));
        assertThrows(InvalidInputException.class, () -> new RoomFeature("mic\t"));
    }

    @Test
    void equalsHashCodeAndCompareToWorkByName() {
        RoomFeature a = new RoomFeature("Audio");
        RoomFeature b = new RoomFeature("audio");
        RoomFeature c = new RoomFeature("Whiteboard");

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, "audio");
        assertNotEquals(a, null);

        assertTrue(a.compareTo(c) < 0);
        assertEquals(0, a.compareTo(b));
        assertTrue(c.compareTo(a) > 0);
        assertThrows(NullPointerException.class, () -> a.compareTo(null));
    }

    @Test
    void parseListHandlesNoneAndSortedDeduplicatedCsv() {
        assertTrue(RoomFeature.parseList("none").isEmpty());
        assertTrue(RoomFeature.parseList("NONE").isEmpty());

        Set<RoomFeature> parsed = RoomFeature.parseList("whiteboard,Projector,whiteboard");
        assertTrue(parsed instanceof TreeSet);
        assertEquals(2, parsed.size());
        assertEquals(
                Arrays.asList(new RoomFeature("projector"), new RoomFeature("whiteboard")),
                Arrays.asList(parsed.toArray(new RoomFeature[0])));
    }

    @Test
    void parseListRejectsMalformedCsv() {
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList(null));
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList(""));
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList("a,,b"));
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList("a,"));
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList(",a"));
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList(","));
        assertThrows(InvalidInputException.class, () -> RoomFeature.parseList("a, b"));
    }

    @Test
    void formatFormatsSortedCommaSpaceOrNone() {
        assertEquals("none", RoomFeature.format(null));
        assertEquals("none", RoomFeature.format(Collections.emptySet()));

        Set<RoomFeature> unsorted = new HashSet<>();
        unsorted.add(new RoomFeature("whiteboard"));
        unsorted.add(new RoomFeature("audio"));
        unsorted.add(new RoomFeature("projector"));
        assertEquals("audio, projector, whiteboard", RoomFeature.format(unsorted));
    }
}