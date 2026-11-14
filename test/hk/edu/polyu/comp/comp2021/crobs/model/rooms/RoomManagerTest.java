package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomManagerTest {
    private RoomManager manager;

    @BeforeEach
    void setUp() {
        manager = new RoomManager();
    }

    @Test
    void addRoomGeneratesSequentialIdsWithoutWastingIdsOnErrors() {
        Set<RoomFeature> nullElementSet = new HashSet<>();
        nullElementSet.add(null);

        assertThrows(InvalidInputException.class,
                () -> manager.addRoom(null, 20, Collections.emptySet()));
        assertThrows(InvalidInputException.class,
                () -> manager.addRoom(RoomType.COMPUTER_LAB, 0, Collections.emptySet()));
        assertThrows(InvalidInputException.class,
                () -> manager.addRoom(RoomType.COMPUTER_LAB, 20, null));
        assertThrows(InvalidInputException.class,
                () -> manager.addRoom(RoomType.COMPUTER_LAB, 20, nullElementSet));

        Room r1 = manager.addRoom(RoomType.COMPUTER_LAB, 40, RoomFeature.parseList("projector"));
        Room r2 = manager.addRoom(RoomType.MEETING_ROOM, 12, RoomFeature.parseList("none"));

        assertEquals("R0001", r1.getId());
        assertEquals("R0002", r2.getId());
        assertTrue(manager.hasRoom("R0001"));
        assertFalse(manager.hasRoom("R9999"));
        assertFalse(manager.hasRoom(null));
    }

    @Test
    void getRemoveAndRestoreRespectIdNonReuse() {
        Room r1 = manager.addRoom(RoomType.LECTURE_THEATRE, 100, RoomFeature.parseList("mic"));
        assertEquals(r1, manager.getRoom("R0001"));
        assertEquals(r1, manager.removeRoom("R0001"));
        assertFalse(manager.hasRoom("R0001"));

        Room r2 = manager.addRoom(RoomType.SEMINAR_ROOM, 25, RoomFeature.parseList("whiteboard"));
        assertEquals("R0002", r2.getId());

        manager.restore(r1);
        assertEquals(r1, manager.getRoom("R0001"));
        assertEquals(2, manager.getAllRooms().size());

        assertThrows(NotFoundException.class, () -> manager.getRoom("R9999"));
        assertThrows(NotFoundException.class, () -> manager.getRoom(null));
        assertThrows(NotFoundException.class, () -> manager.removeRoom("R9999"));
        assertThrows(NotFoundException.class, () -> manager.removeRoom(null));
        assertThrows(InvalidInputException.class, () -> manager.restore(null));
    }

    @Test
    void modifyFieldRoundTripsAllFieldsForUndoAndRejectsInvalidInput() {
        Room room = manager.addRoom(RoomType.COMPUTER_LAB, 40,
                RoomFeature.parseList("projector,whiteboard"));

        String oldType = manager.modifyField(room, RoomField.ROOM_TYPE, "lectureTheatre");
        assertEquals("computerLab", oldType);
        assertEquals(RoomType.LECTURE_THEATRE, room.getType());
        manager.modifyField(room, RoomField.ROOM_TYPE, oldType);
        assertEquals(RoomType.COMPUTER_LAB, room.getType());

        String oldCap = manager.modifyField(room, RoomField.CAPACITY, "60");
        assertEquals("40", oldCap);
        assertEquals(60, room.getCapacity());
        manager.modifyField(room, RoomField.CAPACITY, oldCap);
        assertEquals(40, room.getCapacity());

        String oldFeatures = manager.modifyField(room, RoomField.FEATURES, "none");
        assertEquals("projector,whiteboard", oldFeatures);
        assertTrue(room.getFeatures().isEmpty());
        String emptyUndo = manager.modifyField(room, RoomField.FEATURES, oldFeatures);
        assertEquals("none", emptyUndo);
        assertEquals("projector, whiteboard", RoomFeature.format(room.getFeatures()));

        assertThrows(InvalidInputException.class,
                () -> manager.modifyField(null, RoomField.CAPACITY, "20"));
        assertThrows(InvalidInputException.class,
                () -> manager.modifyField(room, null, "20"));
        assertThrows(InvalidInputException.class,
                () -> manager.modifyField(room, RoomField.CAPACITY, null));
        assertThrows(InvalidInputException.class,
                () -> manager.modifyField(room, RoomField.CAPACITY, "abc"));
        assertThrows(InvalidInputException.class,
                () -> manager.modifyField(room, RoomField.CAPACITY, "0"));
        assertThrows(InvalidInputException.class,
                () -> manager.modifyField(room, RoomField.FEATURES, "a,,b"));
        assertEquals(40, room.getCapacity());
    }

    @Test
    void getAllRoomsAndFindRoomsReturnSortedUnmodifiableLists() {
        for (int i = 0; i < 10; i++) {
            manager.addRoom(RoomType.MEETING_ROOM, 10 + i, RoomFeature.parseList("none"));
        }
        Room r11 = manager.addRoom(RoomType.COMPUTER_LAB, 50,
                RoomFeature.parseList("projector,whiteboard"));

        List<Room> all = manager.getAllRooms();
        assertEquals(11, all.size());
        assertEquals("R0001", all.get(0).getId());
        assertEquals("R0011", all.get(10).getId());
        assertThrows(UnsupportedOperationException.class, () -> all.add(r11));

        List<Room> found = manager.findRooms(18, RoomFeature.parseList("projector"));
        assertEquals(1, found.size());
        assertEquals("R0011", found.get(0).getId());
        assertThrows(UnsupportedOperationException.class, () -> found.remove(0));

        assertEquals(11, manager.findRooms(0, Collections.emptySet()).size());
        assertThrows(InvalidInputException.class,
                () -> manager.findRooms(-1, Collections.emptySet()));
        assertThrows(InvalidInputException.class,
                () -> manager.findRooms(10, null));
    }
}