package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {
    @Test
    void constructorValidatesAndCopiesDefensively() {
        Set<RoomFeature> input = new HashSet<>();
        input.add(new RoomFeature("projector"));
        Room room = new Room("R0001", RoomType.COMPUTER_LAB, 40, input);

        assertEquals("R0001", room.getId());
        assertEquals(RoomType.COMPUTER_LAB, room.getType());
        assertEquals(40, room.getCapacity());
        assertEquals(1, room.getFeatures().size());

        input.add(new RoomFeature("mic"));
        assertEquals(1, room.getFeatures().size());
        assertThrows(UnsupportedOperationException.class,
                () -> room.getFeatures().add(new RoomFeature("whiteboard")));
    }

    @Test
    void constructorAndSettersRejectInvalidValues() {
        Set<RoomFeature> valid = RoomFeature.parseList("projector");
        assertThrows(InvalidInputException.class,
                () -> new Room(null, RoomType.MEETING_ROOM, 10, valid));
        assertThrows(InvalidInputException.class,
                () -> new Room("   ", RoomType.MEETING_ROOM, 10, valid));
        assertThrows(InvalidInputException.class,
                () -> new Room("R0001", null, 10, valid));
        assertThrows(InvalidInputException.class,
                () -> new Room("R0001", RoomType.MEETING_ROOM, 0, valid));
        assertThrows(InvalidInputException.class,
                () -> new Room("R0001", RoomType.MEETING_ROOM, -3, valid));
        assertThrows(InvalidInputException.class,
                () -> new Room("R0001", RoomType.MEETING_ROOM, 10, null));

        Set<RoomFeature> withNull = new HashSet<>();
        withNull.add(null);
        assertThrows(InvalidInputException.class,
                () -> new Room("R0001", RoomType.MEETING_ROOM, 10, withNull));
    }

    @Test
    void settersUpdateRoomCorrectly() {
        Room room = new Room("R0001", RoomType.MEETING_ROOM, 10, Collections.emptySet());
        room.setType(RoomType.LECTURE_THEATRE);
        room.setCapacity(120);
        room.setFeatures(RoomFeature.parseList("projector,mic"));

        assertEquals(RoomType.LECTURE_THEATRE, room.getType());
        assertEquals(120, room.getCapacity());
        assertEquals("mic, projector", RoomFeature.format(room.getFeatures()));
    }

    @Test
    void hasAllFeaturesAndFitsEvaluateConstraints() {
        Room room = new Room("R0002", RoomType.SEMINAR_ROOM, 30,
                RoomFeature.parseList("projector,whiteboard"));

        assertTrue(room.hasAllFeatures(Collections.emptySet()));
        assertTrue(room.hasAllFeatures(RoomFeature.parseList("projector")));
        assertTrue(room.hasAllFeatures(RoomFeature.parseList("projector,whiteboard")));
        assertFalse(room.hasAllFeatures(RoomFeature.parseList("projector,mic")));
        assertThrows(InvalidInputException.class, () -> room.hasAllFeatures(null));

        assertTrue(room.fits(30, RoomFeature.parseList("projector")));
        assertTrue(room.fits(20, Collections.emptySet()));
        assertFalse(room.fits(31, RoomFeature.parseList("projector")));
        assertFalse(room.fits(20, RoomFeature.parseList("mic")));
        assertThrows(InvalidInputException.class, () -> room.fits(10, null));
    }
}