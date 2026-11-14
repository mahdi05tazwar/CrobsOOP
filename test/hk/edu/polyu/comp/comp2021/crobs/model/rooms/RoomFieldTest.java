package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoomFieldTest {
    @Test
    void keywordsMatchSpecification() {
        assertEquals("roomType", RoomField.ROOM_TYPE.getKeyword());
        assertEquals("capacity", RoomField.CAPACITY.getKeyword());
        assertEquals("features", RoomField.FEATURES.getKeyword());
    }

    @Test
    void fromStringMatchesCaseInsensitively() {
        assertEquals(RoomField.ROOM_TYPE, RoomField.fromString("roomType"));
        assertEquals(RoomField.ROOM_TYPE, RoomField.fromString("ROOMTYPE"));
        assertEquals(RoomField.CAPACITY, RoomField.fromString("Capacity"));
        assertEquals(RoomField.FEATURES, RoomField.fromString("FEATURES"));
    }

    @Test
    void fromStringRejectsUnknownAndNullInputs() {
        assertThrows(InvalidInputException.class, () -> RoomField.fromString("id"));
        assertThrows(InvalidInputException.class, () -> RoomField.fromString(""));
        assertThrows(InvalidInputException.class, () -> RoomField.fromString(null));
    }
}