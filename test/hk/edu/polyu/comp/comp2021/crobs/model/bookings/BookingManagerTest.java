package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingManagerTest {

    private static final String ALICE = "U0002";
    private static final String BOB = "U0003";
    private static final String ROOM_1 = "R0001";
    private static final String ROOM_2 = "R0002";

    private BookingManager manager;
    private TimeSlot morning;
    private TimeSlot afternoon;

    @BeforeEach
    void setUp() {
        manager = new BookingManager();
        morning = TimeSlot.parse("2026-11-20", "09:00", "11:00");
        afternoon = TimeSlot.parse("2026-11-20", "14:00", "16:00");
    }

    private static TimeSlot slot(String date, String start, String end) {
        return TimeSlot.parse(date, start, end);
    }

    // ---------- createBooking ----------

    @Test
    void createBookingStoresAnActiveBooking() {
        Booking booking = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        assertEquals(ALICE, booking.getOwnerId());
        assertEquals(ROOM_1, booking.getRoomId());
        assertEquals(morning, booking.getSlot());
        assertEquals("Meeting", booking.getPurpose());
        assertTrue(booking.isActive());
        assertSame(booking, manager.getBooking(booking.getId()));
    }

    @Test
    void everyBookingGetsItsOwnId() {
        Booking first = manager.createBooking(ALICE, ROOM_1, morning, "First");
        Booking second = manager.createBooking(ALICE, ROOM_1, afternoon, "Second");
        Booking third = manager.createBooking(BOB, ROOM_2, morning, "Third");

        assertTrue(first.getId().startsWith("B"));
        assertNotEquals(first.getId(), second.getId());
        assertNotEquals(first.getId(), third.getId());
        assertNotEquals(second.getId(), third.getId());
    }

    @Test
    void overlappingBookingInSameRoomIsRejected() {
        Booking existing = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");
        TimeSlot overlapping = slot("2026-11-20", "10:00", "12:00");

        ConflictException e = assertThrows(ConflictException.class,
                () -> manager.createBooking(BOB, ROOM_1, overlapping, "Clash"));

        assertEquals("Room R0001 is already booked by " + existing.getId() + " at that time", e.getMessage());
        assertTrue(manager.getBookingsByUser(BOB).isEmpty());
    }

    @Test
    void backToBackBookingInSameRoomIsAllowed() {
        manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        Booking next = manager.createBooking(BOB, ROOM_1, slot("2026-11-20", "11:00", "12:00"), "Right after");

        assertTrue(next.isActive());
    }

    @Test
    void sameTimeInAnotherRoomIsAllowed() {
        manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        Booking other = manager.createBooking(BOB, ROOM_2, morning, "Other room");

        assertTrue(other.isActive());
    }

    @Test
    void createBookingWithEmptyPurposeIsRejected() {
        assertThrows(InvalidInputException.class, () -> manager.createBooking(ALICE, ROOM_1, morning, ""));
        assertTrue(manager.isRoomAvailable(ROOM_1, morning));
    }

    // ---------- getBooking ----------

    @Test
    void getBookingWithUnknownIdIsRejected() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> manager.getBooking("B9999"));

        assertEquals("Booking not found: B9999", e.getMessage());
    }

    // ---------- findConflict and isRoomAvailable ----------

    @Test
    void findConflictReturnsTheClashingBooking() {
        Booking existing = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        assertSame(existing, manager.findConflict(ROOM_1, morning, null));
    }

    @Test
    void findConflictReturnsNullWhenRoomIsFree() {
        manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        assertNull(manager.findConflict(ROOM_1, afternoon, null));
        assertNull(manager.findConflict(ROOM_2, morning, null));
    }

    @Test
    void findConflictSkipsTheIgnoredBooking() {
        Booking existing = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        assertNull(manager.findConflict(ROOM_1, morning, existing.getId()));
    }

    @Test
    void isRoomAvailableReflectsActiveBookings() {
        assertTrue(manager.isRoomAvailable(ROOM_1, morning));

        Booking booking = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");

        assertFalse(manager.isRoomAvailable(ROOM_1, morning));
        assertTrue(manager.isRoomAvailable(ROOM_1, afternoon));
        assertTrue(manager.isRoomAvailable(ROOM_2, morning));

        booking.cancel();

        assertTrue(manager.isRoomAvailable(ROOM_1, morning));
    }

    // ---------- modifyBooking ----------

    @Test
    void modifyBookingChangesRoomSlotAndPurpose() {
        Booking booking = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");
        String id = booking.getId();

        manager.modifyBooking(booking, ROOM_2, afternoon, "Moved");

        assertEquals(ROOM_2, booking.getRoomId());
        assertEquals(afternoon, booking.getSlot());
        assertEquals("Moved", booking.getPurpose());
        assertEquals(id, booking.getId());
        assertEquals(ALICE, booking.getOwnerId());
    }

    @Test
    void modifyBookingMayOverlapItsOwnOldSlot() {
        Booking booking = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");
        TimeSlot shifted = slot("2026-11-20", "09:30", "11:30");

        manager.modifyBooking(booking, ROOM_1, shifted, "Shifted");

        assertEquals(shifted, booking.getSlot());
    }

    @Test
    void modifyBookingIntoAnotherBookingIsRejectedAndChangesNothing() {
        manager.createBooking(ALICE, ROOM_1, morning, "Meeting");
        Booking mine = manager.createBooking(BOB, ROOM_1, afternoon, "Mine");

        assertThrows(ConflictException.class,
                () -> manager.modifyBooking(mine, ROOM_1, morning, "Clash"));

        assertEquals(afternoon, mine.getSlot());
        assertEquals("Mine", mine.getPurpose());
    }

    // ---------- cancelAllByUser ----------

    @Test
    void cancelAllByUserCancelsOnlyThatUsersActiveBookings() {
        Booking a1 = manager.createBooking(ALICE, ROOM_1, morning, "A1");
        Booking a2 = manager.createBooking(ALICE, ROOM_1, afternoon, "A2");
        Booking alreadyCancelled = manager.createBooking(ALICE, ROOM_2, afternoon, "A3");
        Booking bobs = manager.createBooking(BOB, ROOM_2, morning, "B1");
        alreadyCancelled.cancel();

        List<Booking> cancelled = manager.cancelAllByUser(ALICE);

        assertEquals(2, cancelled.size());
        assertTrue(cancelled.contains(a1));
        assertTrue(cancelled.contains(a2));
        assertFalse(a1.isActive());
        assertFalse(a2.isActive());
        assertTrue(bobs.isActive());
    }

    @Test
    void cancelAllByUserWithNoBookingsReturnsEmptyList() {
        assertTrue(manager.cancelAllByUser(ALICE).isEmpty());
    }

    // ---------- cancelFutureByRoom ----------

    @Test
    void cancelFutureByRoomCancelsOnlyFutureActiveBookingsOfThatRoom() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 20, 12, 0);
        Booking past = manager.createBooking(ALICE, ROOM_1, morning, "Already started");
        Booking future = manager.createBooking(ALICE, ROOM_1, afternoon, "Later today");
        Booking nextDay = manager.createBooking(BOB, ROOM_1, slot("2026-11-21", "09:00", "10:00"), "Tomorrow");
        Booking futureCancelled = manager.createBooking(BOB, ROOM_1, slot("2026-11-22", "09:00", "10:00"), "Gone");
        Booking otherRoom = manager.createBooking(BOB, ROOM_2, afternoon, "Other room");
        futureCancelled.cancel();

        List<Booking> cancelled = manager.cancelFutureByRoom(ROOM_1, now);

        assertEquals(2, cancelled.size());
        assertTrue(cancelled.contains(future));
        assertTrue(cancelled.contains(nextDay));
        assertTrue(past.isActive());
        assertFalse(future.isActive());
        assertFalse(nextDay.isActive());
        assertTrue(otherRoom.isActive());
    }

    @Test
    void bookingStartingExactlyNowIsNotCancelled() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 20, 9, 0);
        Booking startingNow = manager.createBooking(ALICE, ROOM_1, morning, "Starting now");

        assertTrue(manager.cancelFutureByRoom(ROOM_1, now).isEmpty());
        assertTrue(startingNow.isActive());
    }

    // ---------- getBookingsByUser ----------

    @Test
    void getBookingsByUserIsSortedAndIncludesCancelled() {
        Booking late = manager.createBooking(ALICE, ROOM_1, slot("2026-11-25", "09:00", "10:00"), "Late");
        Booking early = manager.createBooking(ALICE, ROOM_1, slot("2026-11-18", "15:00", "16:00"), "Early");
        Booking middle = manager.createBooking(ALICE, ROOM_1, afternoon, "Middle");
        Booking middleMorning = manager.createBooking(ALICE, ROOM_1, morning, "Middle morning");
        manager.createBooking(BOB, ROOM_2, morning, "Not Alice");
        middle.cancel();

        List<Booking> result = manager.getBookingsByUser(ALICE);

        assertEquals(4, result.size());
        assertSame(early, result.get(0));
        assertSame(middleMorning, result.get(1));
        assertSame(middle, result.get(2));
        assertSame(late, result.get(3));
    }

    @Test
    void getBookingsByUserWithNoBookingsReturnsEmptyList() {
        assertTrue(manager.getBookingsByUser(ALICE).isEmpty());
    }

    // ---------- getActiveBookingsForRoom ----------

    @Test
    void getActiveBookingsForRoomKeepsOnlyActiveBookingsInTheWeek() {
        LocalDate from = LocalDate.of(2026, 11, 16);
        LocalDate toExclusive = LocalDate.of(2026, 11, 23);
        Booking lastDay = manager.createBooking(ALICE, ROOM_1, slot("2026-11-22", "09:00", "10:00"), "Last day");
        Booking firstDay = manager.createBooking(BOB, ROOM_1, slot("2026-11-16", "09:00", "10:00"), "First day");
        Booking midAfternoon = manager.createBooking(ALICE, ROOM_1, afternoon, "Afternoon");
        Booking midMorning = manager.createBooking(BOB, ROOM_1, morning, "Morning");
        Booking cancelled = manager.createBooking(ALICE, ROOM_1, slot("2026-11-18", "09:00", "10:00"), "Cancelled");
        manager.createBooking(ALICE, ROOM_1, slot("2026-11-15", "09:00", "10:00"), "Day before");
        manager.createBooking(ALICE, ROOM_1, slot("2026-11-23", "09:00", "10:00"), "Day after");
        manager.createBooking(ALICE, ROOM_2, slot("2026-11-17", "09:00", "10:00"), "Other room");
        cancelled.cancel();

        List<Booking> result = manager.getActiveBookingsForRoom(ROOM_1, from, toExclusive);

        assertEquals(4, result.size());
        assertSame(firstDay, result.get(0));
        assertSame(midMorning, result.get(1));
        assertSame(midAfternoon, result.get(2));
        assertSame(lastDay, result.get(3));
    }

    // ---------- remove and restore ----------

    @Test
    void removeTakesTheBookingOutOfStorage() {
        Booking booking = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");
        String id = booking.getId();

        manager.remove(id);

        assertThrows(NotFoundException.class, () -> manager.getBooking(id));
        assertTrue(manager.isRoomAvailable(ROOM_1, morning));
        assertTrue(manager.getBookingsByUser(ALICE).isEmpty());
    }

    @Test
    void removeWithUnknownIdIsRejected() {
        assertThrows(NotFoundException.class, () -> manager.remove("B9999"));
    }

    @Test
    void restorePutsARemovedBookingBack() {
        Booking booking = manager.createBooking(ALICE, ROOM_1, morning, "Meeting");
        manager.remove(booking.getId());

        manager.restore(booking);

        assertSame(booking, manager.getBooking(booking.getId()));
        assertFalse(manager.isRoomAvailable(ROOM_1, morning));
    }

    @Test
    void idsAreNotReusedAfterARemove() {
        Booking first = manager.createBooking(ALICE, ROOM_1, morning, "First");
        manager.remove(first.getId());

        Booking second = manager.createBooking(ALICE, ROOM_1, morning, "Second");

        assertNotEquals(first.getId(), second.getId());
    }
}