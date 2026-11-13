package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSlotTest {

    private static final String DAY = "2026-11-20";

    // ---------- creating a slot ----------

    @Test
    void parseReadsDateAndTimes() {
        TimeSlot slot = TimeSlot.parse(DAY, "09:00", "11:00");

        assertEquals(LocalDate.of(2026, 11, 20), slot.getDate());
        assertEquals(LocalTime.of(9, 0), slot.getStart());
        assertEquals(LocalTime.of(11, 0), slot.getEnd());
    }

    @Test
    void constructorKeepsItsValues() {
        TimeSlot slot = new TimeSlot(LocalDate.of(2026, 11, 20), LocalTime.of(14, 30), LocalTime.of(15, 0));

        assertEquals(LocalDate.of(2026, 11, 20), slot.getDate());
        assertEquals(LocalTime.of(14, 30), slot.getStart());
        assertEquals(LocalTime.of(15, 0), slot.getEnd());
    }

    @Test
    void startEqualToEndIsRejected() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> TimeSlot.parse(DAY, "09:00", "09:00"));

        assertEquals("Start time must be before end time", e.getMessage());
    }

    @Test
    void startAfterEndIsRejected() {
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse(DAY, "11:00", "09:00"));
    }

    @Test
    void wrongDateFormatIsRejected() {
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse("20/11/2026", "09:00", "11:00"));
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse("tomorrow", "09:00", "11:00"));
    }

    @Test
    void dateThatDoesNotExistIsRejected() {
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse("2026-02-30", "09:00", "11:00"));
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse("2026-13-01", "09:00", "11:00"));
    }

    @Test
    void wrongTimeFormatIsRejected() {
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse(DAY, "9:00", "11:00"));
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse(DAY, "09:00", "25:00"));
        assertThrows(InvalidInputException.class, () -> TimeSlot.parse(DAY, "9am", "11am"));
    }

    // ---------- parseDate ----------

    @Test
    void parseDateReadsAValidDate() {
        assertEquals(LocalDate.of(2026, 11, 16), TimeSlot.parseDate("2026-11-16"));
    }

    @Test
    void parseDateRejectsABadDate() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> TimeSlot.parseDate("2026-02-30"));

        assertEquals("Invalid date. Use yyyy-MM-dd", e.getMessage());
        assertThrows(InvalidInputException.class, () -> TimeSlot.parseDate("16-11-2026"));
    }

    // ---------- getStartDateTime ----------

    @Test
    void startDateTimeJoinsDateAndStart() {
        TimeSlot slot = TimeSlot.parse(DAY, "09:00", "11:00");

        assertEquals(LocalDateTime.of(2026, 11, 20, 9, 0), slot.getStartDateTime());
    }

    // ---------- overlaps ----------

    @Test
    void slotsThatShareTimeOverlap() {
        TimeSlot a = TimeSlot.parse(DAY, "09:00", "11:00");
        TimeSlot b = TimeSlot.parse(DAY, "10:00", "12:00");

        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    void slotInsideAnotherOverlaps() {
        TimeSlot outer = TimeSlot.parse(DAY, "09:00", "12:00");
        TimeSlot inner = TimeSlot.parse(DAY, "10:00", "11:00");

        assertTrue(outer.overlaps(inner));
        assertTrue(inner.overlaps(outer));
    }

    @Test
    void identicalSlotsOverlap() {
        TimeSlot a = TimeSlot.parse(DAY, "09:00", "11:00");
        TimeSlot b = TimeSlot.parse(DAY, "09:00", "11:00");

        assertTrue(a.overlaps(b));
    }

    @Test
    void backToBackSlotsDoNotOverlap() {
        TimeSlot first = TimeSlot.parse(DAY, "09:00", "10:00");
        TimeSlot second = TimeSlot.parse(DAY, "10:00", "11:00");

        assertFalse(first.overlaps(second));
        assertFalse(second.overlaps(first));
    }

    @Test
    void sameTimesOnDifferentDaysDoNotOverlap() {
        TimeSlot a = TimeSlot.parse("2026-11-20", "09:00", "11:00");
        TimeSlot b = TimeSlot.parse("2026-11-21", "09:00", "11:00");

        assertFalse(a.overlaps(b));
    }

    // ---------- isWithin ----------

    @Test
    void isWithinIncludesFirstDayAndExcludesEndDay() {
        LocalDate from = LocalDate.of(2026, 11, 16);
        LocalDate toExclusive = LocalDate.of(2026, 11, 23);

        assertTrue(TimeSlot.parse("2026-11-16", "09:00", "10:00").isWithin(from, toExclusive));
        assertTrue(TimeSlot.parse("2026-11-22", "09:00", "10:00").isWithin(from, toExclusive));
        assertFalse(TimeSlot.parse("2026-11-23", "09:00", "10:00").isWithin(from, toExclusive));
        assertFalse(TimeSlot.parse("2026-11-15", "09:00", "10:00").isWithin(from, toExclusive));
    }

    // ---------- compareTo ----------

    @Test
    void earlierDateComesFirst() {
        TimeSlot early = TimeSlot.parse("2026-11-19", "15:00", "16:00");
        TimeSlot late = TimeSlot.parse("2026-11-20", "09:00", "10:00");

        assertTrue(early.compareTo(late) < 0);
        assertTrue(late.compareTo(early) > 0);
    }

    @Test
    void onSameDateEarlierStartComesFirst() {
        TimeSlot morning = TimeSlot.parse(DAY, "09:00", "10:00");
        TimeSlot afternoon = TimeSlot.parse(DAY, "14:00", "15:00");

        assertTrue(morning.compareTo(afternoon) < 0);
        assertTrue(afternoon.compareTo(morning) > 0);
    }

    @Test
    void sameDateAndStartCompareAsEqual() {
        TimeSlot a = TimeSlot.parse(DAY, "09:00", "10:00");
        TimeSlot b = TimeSlot.parse(DAY, "09:00", "11:00");

        assertEquals(0, a.compareTo(b));
    }

    // ---------- equals and hashCode ----------

    @Test
    void slotsWithSameValuesAreEqual() {
        TimeSlot a = TimeSlot.parse(DAY, "09:00", "11:00");
        TimeSlot b = TimeSlot.parse(DAY, "09:00", "11:00");

        assertEquals(a, b);
        assertEquals(a, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void slotsWithAnyDifferentValueAreNotEqual() {
        TimeSlot base = TimeSlot.parse(DAY, "09:00", "11:00");

        assertNotEquals(base, TimeSlot.parse("2026-11-21", "09:00", "11:00"));
        assertNotEquals(base, TimeSlot.parse(DAY, "08:00", "11:00"));
        assertNotEquals(base, TimeSlot.parse(DAY, "09:00", "12:00"));
    }

    @Test
    void slotIsNotEqualToNullOrOtherTypes() {
        TimeSlot slot = TimeSlot.parse(DAY, "09:00", "11:00");

        assertFalse(slot.equals(null));
        assertFalse(slot.equals("2026-11-20 09:00-11:00"));
    }

    // ---------- toString ----------

    @Test
    void toStringShowsDateAndTimes() {
        assertEquals("2026-11-20 09:00-11:00", TimeSlot.parse(DAY, "09:00", "11:00").toString());
    }
}