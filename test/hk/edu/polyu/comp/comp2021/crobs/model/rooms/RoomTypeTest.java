package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTypeTest {
    @Test
    void keywordsMatchSpecification() {
        assertEquals("lectureTheatre", RoomType.LECTURE_THEATRE.getKeyword());
        assertEquals("computerLab", RoomType.COMPUTER_LAB.getKeyword());
        assertEquals("seminarRoom", RoomType.SEMINAR_ROOM.getKeyword());
        assertEquals("meetingRoom", RoomType.MEETING_ROOM.getKeyword());
    }

    @Test
    void fromStringMatchesCaseInsensitively() {
        assertEquals(RoomType.LECTURE_THEATRE, RoomType.fromString("lectureTheatre"));
        assertEquals(RoomType.LECTURE_THEATRE, RoomType.fromString("LECTURETHEATRE"));
        assertEquals(RoomType.COMPUTER_LAB, RoomType.fromString("ComputerLab"));
        assertEquals(RoomType.SEMINAR_ROOM, RoomType.fromString("seminarroom"));
        assertEquals(RoomType.MEETING_ROOM, RoomType.fromString("MEETINGROOM"));
    }

    @Test
    void fromStringRejectsInvalidAndNullInputs() {
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> RoomType.fromString("unknownRoom"));
        assertTrue(ex.getMessage().contains("lectureTheatre"));

        assertThrows(InvalidInputException.class, () -> RoomType.fromString(""));
        assertThrows(InvalidInputException.class, () -> RoomType.fromString("seminar Room"));
        assertThrows(InvalidInputException.class, () -> RoomType.fromString(null));
    }
}