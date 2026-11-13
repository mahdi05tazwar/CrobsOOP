
package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Booking, and for ScheduleEntry (which wraps a Booking).
 */
class BookingTest {

    private TimeSlot morning;
    private TimeSlot afternoon;
    private Booking booking;

    @BeforeEach
    void setUp() {
        morning = TimeSlot.parse("2026-11-20", "09:00", "11:00");
        afternoon = TimeSlot.parse("2026-11-20", "14:00", "16:00");
        booking = new Booking("B0001", "U0002", "R0001", morning, "COMP2021 meeting");
    }

    // ---------- creating a booking ----------

    @Test
    void newBookingKeepsItsValuesAndIsActive() {
        assertEquals("B0001", booking.getId());
        assertEquals("U0002", booking.getOwnerId());
        assertEquals("R0001", booking.getRoomId());
        assertEquals(morning, booking.getSlot());
        assertEquals("COMP2021 meeting", booking.getPurpose());
        assertEquals(BookingStatus.ACTIVE, booking.getStatus());
        assertTrue(booking.isActive());
    }

    @Test
    void emptyPurposeIsRejected() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> new Booking("B0001", "U0002", "R0001", morning, ""));

        assertEquals("Purpose cannot be empty", e.getMessage());
    }

    @Test
    void blankOrMissingPurposeIsRejected() {
        assertThrows(InvalidInputException.class,
                () -> new Booking("B0001", "U0002", "R0001", morning, "   "));
        assertThrows(InvalidInputException.class,
                () -> new Booking("B0001", "U0002", "R0001", morning, null));
    }

    // ---------- ownership ----------

    @Test
    void isOwnedByMatchesOnlyTheOwner() {
        assertTrue(booking.isOwnedBy("U0002"));
        assertFalse(booking.isOwnedBy("U0003"));
    }

    // ---------- update ----------

    @Test
    void updateChangesRoomSlotAndPurposeButNotIdOrOwner() {
        booking.update("R0005", afternoon, "Project demo");

        assertEquals("R0005", booking.getRoomId());
        assertEquals(afternoon, booking.getSlot());
        assertEquals("Project demo", booking.getPurpose());
        assertEquals("B0001", booking.getId());
        assertEquals("U0002", booking.getOwnerId());
        assertTrue(booking.isActive());
    }

    @Test
    void updateWithEmptyPurposeIsRejectedAndChangesNothing() {
        assertThrows(InvalidInputException.class, () -> booking.update("R0005", afternoon, " "));

        assertEquals("R0001", booking.getRoomId());
        assertEquals(morning, booking.getSlot());
        assertEquals("COMP2021 meeting", booking.getPurpose());
    }

    // ---------- cancel and reactivate ----------

    @Test
    void cancelMakesTheBookingInactive() {
        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(booking.isActive());
    }

    @Test
    void cancellingTwiceIsRejected() {
        booking.cancel();

        ConflictException e = assertThrows(ConflictException.class, () -> booking.cancel());
        assertEquals("Booking is already cancelled", e.getMessage());
    }

    @Test
    void reactivateMakesACancelledBookingActiveAgain() {
        booking.cancel();
        booking.reactivate();

        assertEquals(BookingStatus.ACTIVE, booking.getStatus());
        assertTrue(booking.isActive());
    }

    // ---------- conflictsWith ----------

    @Test
    void sameRoomAndOverlappingTimeConflicts() {
        TimeSlot overlapping = TimeSlot.parse("2026-11-20", "10:00", "12:00");

        assertTrue(booking.conflictsWith("R0001", overlapping));
    }

    @Test
    void differentRoomDoesNotConflict() {
        assertFalse(booking.conflictsWith("R0002", morning));
    }

    @Test
    void sameRoomAtAnotherTimeDoesNotConflict() {
        assertFalse(booking.conflictsWith("R0001", afternoon));
    }

    @Test
    void backToBackInSameRoomDoesNotConflict() {
        TimeSlot rightAfter = TimeSlot.parse("2026-11-20", "11:00", "12:00");

        assertFalse(booking.conflictsWith("R0001", rightAfter));
    }

    @Test
    void cancelledBookingNeverConflicts() {
        booking.cancel();

        assertFalse(booking.conflictsWith("R0001", morning));
    }

    // ---------- byTime ----------

    @Test
    void byTimeSortsByDateThenStartThenId() {
        Booking nextDay = new Booking("B0001", "U0002", "R0001",
                TimeSlot.parse("2026-11-21", "08:00", "09:00"), "later day");
        Booking sameSlotHigherId = new Booking("B0004", "U0002", "R0002", morning, "same slot");
        Booking sameSlotLowerId = new Booking("B0002", "U0002", "R0001", morning, "same slot");
        Booking afternoonSameDay = new Booking("B0003", "U0002", "R0001", afternoon, "afternoon");

        List<Booking> list = new ArrayList<Booking>();
        list.add(nextDay);
        list.add(sameSlotHigherId);
        list.add(afternoonSameDay);
        list.add(sameSlotLowerId);
        list.sort(Booking.byTime());

        assertSame(sameSlotLowerId, list.get(0));
        assertSame(sameSlotHigherId, list.get(1));
        assertSame(afternoonSameDay, list.get(2));
        assertSame(nextDay, list.get(3));
    }

    // ---------- ScheduleEntry ----------

    @Test
    void scheduleEntryShowsOwnerWhenVisible() {
        ScheduleEntry entry = new ScheduleEntry(booking, true);

        assertSame(booking, entry.getBooking());
        assertTrue(entry.isOwnerVisible());
        assertEquals("U0002", entry.getOwnerDisplay());
    }

    @Test
    void scheduleEntryHidesOwnerWhenNotVisible() {
        ScheduleEntry entry = new ScheduleEntry(booking, false);

        assertSame(booking, entry.getBooking());
        assertFalse(entry.isOwnerVisible());
        assertEquals("(hidden)", entry.getOwnerDisplay());
    }
}