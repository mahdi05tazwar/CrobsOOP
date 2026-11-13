package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

import hk.edu.polyu.comp.comp2021.crobs.model.core.IdGenerator;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores every booking and enforces the rule that two active bookings
 * can never overlap in the same room.
 */
public class BookingManager {

    private static final String ID_PREFIX = "B";

    private final Map<String, Booking> bookings = new LinkedHashMap<String, Booking>();
    private final IdGenerator ids = new IdGenerator(ID_PREFIX);

    /**
     * Creates a booking with a new unique ID and stores it.
     *
     * @param ownerId the ID of the user making the booking
     * @param roomId  the ID of the room to book
     * @param slot    the date and time to book
     * @param purpose why the room is booked
     * @return the new booking
     * @throws ConflictException if the room is already booked at that time
     */
    public Booking createBooking(String ownerId, String roomId, TimeSlot slot, String purpose) {
        checkNoConflict(roomId, slot, null);
        Booking booking = new Booking(ids.next(), ownerId, roomId, slot, purpose);
        bookings.put(booking.getId(), booking);
        return booking;
    }

    /**
     * Finds a booking by its ID.
     *
     * @param id the booking ID
     * @return the booking
     * @throws NotFoundException if no booking has that ID
     */
    public Booking getBooking(String id) {
        Booking booking = bookings.get(id);
        if (booking == null) {
            throw new NotFoundException("Booking not found: " + id);
        }
        return booking;
    }

    /**
     * Finds the first active booking that clashes with a room and time.
     *
     * @param roomId          the room to check
     * @param slot            the time to check
     * @param ignoreBookingId a booking ID to skip, or null to skip none
     * @return the clashing booking, or null if there is none
     */
    public Booking findConflict(String roomId, TimeSlot slot, String ignoreBookingId) {
        for (Booking booking : bookings.values()) {
            if (!booking.getId().equals(ignoreBookingId) && booking.conflictsWith(roomId, slot)) {
                return booking;
            }
        }
        return null;
    }

    /**
     * Checks whether a room is free for a whole time slot.
     *
     * @param roomId the room to check
     * @param slot   the time to check
     * @return true if no active booking clashes
     */
    public boolean isRoomAvailable(String roomId, TimeSlot slot) {
        return findConflict(roomId, slot, null) == null;
    }

    /**
     * Changes the room, time and purpose of a booking, after checking
     * the new values do not clash with any other booking.
     *
     * @param booking the booking to change
     * @param roomId  the new room ID
     * @param slot    the new date and time
     * @param purpose the new purpose
     * @throws ConflictException if another booking already uses that room and time
     */
    public void modifyBooking(Booking booking, String roomId, TimeSlot slot, String purpose) {
        checkNoConflict(roomId, slot, booking.getId());
        booking.update(roomId, slot, purpose);
    }

    /**
     * Cancels every active booking made by one user.
     *
     * @param userId the user whose bookings are cancelled
     * @return the bookings that were cancelled
     */
    public List<Booking> cancelAllByUser(String userId) {
        List<Booking> cancelled = new ArrayList<Booking>();
        for (Booking booking : bookings.values()) {
            if (booking.isActive() && booking.isOwnedBy(userId)) {
                booking.cancel();
                cancelled.add(booking);
            }
        }
        return cancelled;
    }

    /**
     * Cancels every active booking of one room that has not started yet.
     *
     * @param roomId the room whose bookings are cancelled
     * @param now    the current date and time
     * @return the bookings that were cancelled
     */
    public List<Booking> cancelFutureByRoom(String roomId, LocalDateTime now) {
        List<Booking> cancelled = new ArrayList<Booking>();
        for (Booking booking : bookings.values()) {
            if (booking.isActive()
                    && booking.getRoomId().equals(roomId)
                    && booking.getSlot().getStartDateTime().isAfter(now)) {
                booking.cancel();
                cancelled.add(booking);
            }
        }
        return cancelled;
    }

    /**
     * Lists every booking a user has made, active or cancelled.
     *
     * @param userId the user whose bookings are listed
     * @return the bookings, sorted by date and start time
     */
    public List<Booking> getBookingsByUser(String userId) {
        List<Booking> result = new ArrayList<Booking>();
        for (Booking booking : bookings.values()) {
            if (booking.isOwnedBy(userId)) {
                result.add(booking);
            }
        }
        result.sort(Booking.byTime());
        return result;
    }

    /**
     * Lists the active bookings of one room inside a date range.
     *
     * @param roomId      the room whose bookings are listed
     * @param from        the first day of the range (included)
     * @param toExclusive the day after the last day of the range (not included)
     * @return the bookings, sorted by date and start time
     */
    public List<Booking> getActiveBookingsForRoom(String roomId, LocalDate from, LocalDate toExclusive) {
        List<Booking> result = new ArrayList<Booking>();
        for (Booking booking : bookings.values()) {
            if (booking.isActive()
                    && booking.getRoomId().equals(roomId)
                    && booking.getSlot().isWithin(from, toExclusive)) {
                result.add(booking);
            }
        }
        result.sort(Booking.byTime());
        return result;
    }

    /**
     * Takes a booking out of storage completely. Used to undo a "book" command.
     *
     * @param id the ID of the booking to remove
     * @throws NotFoundException if no booking has that ID
     */
    public void remove(String id) {
        bookings.remove(getBooking(id).getId());
    }

    /**
     * Puts a removed booking back into storage. Used to redo a "book" command.
     *
     * @param booking the booking to put back
     */
    public void restore(Booking booking) {
        bookings.put(booking.getId(), booking);
    }

    private void checkNoConflict(String roomId, TimeSlot slot, String ignoreBookingId) {
        Booking clash = findConflict(roomId, slot, ignoreBookingId);
        if (clash != null) {
            throw new ConflictException(
                    "Room " + roomId + " is already booked by " + clash.getId() + " at that time");
        }
    }
}