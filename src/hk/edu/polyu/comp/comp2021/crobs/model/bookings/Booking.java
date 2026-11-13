package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.util.Comparator;

/**
 * One room booking made by one user.
 * The booking ID and the owner never change; room, time slot and purpose can.
 */
public class Booking {

    private static final Comparator<Booking> BY_TIME =
            Comparator.comparing(Booking::getSlot).thenComparing(Booking::getId);

    private final String id;
    private final String ownerId;
    private String roomId;
    private TimeSlot slot;
    private String purpose;
    private BookingStatus status;

    /**
     * Creates an active booking.
     *
     * @param id      the unique booking ID
     * @param ownerId the ID of the user who made the booking
     * @param roomId  the ID of the booked room
     * @param slot    the booked date and time
     * @param purpose why the room is booked
     * @throws InvalidInputException if the purpose is empty
     */
    public Booking(String id, String ownerId, String roomId, TimeSlot slot, String purpose) {
        checkPurpose(purpose);
        this.id = id;
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.slot = slot;
        this.purpose = purpose;
        this.status = BookingStatus.ACTIVE;
    }

    /**
     * Returns the booking ID.
     *
     * @return the booking ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the ID of the user who owns this booking.
     *
     * @return the owner's user ID
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Returns the ID of the booked room.
     *
     * @return the room ID
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * Returns the booked date and time.
     *
     * @return the time slot
     */
    public TimeSlot getSlot() {
        return slot;
    }

    /**
     * Returns why the room is booked.
     *
     * @return the purpose
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * Returns whether the booking is active or cancelled.
     *
     * @return the status
     */
    public BookingStatus getStatus() {
        return status;
    }

    /**
     * Checks whether the booking is still in effect.
     *
     * @return true if the status is ACTIVE
     */
    public boolean isActive() {
        return status == BookingStatus.ACTIVE;
    }

    /**
     * Checks whether a given user owns this booking.
     *
     * @param userId the user ID to check
     * @return true if that user made this booking
     */
    public boolean isOwnedBy(String userId) {
        return ownerId.equals(userId);
    }

    /**
     * Changes the room, time slot and purpose. The ID and owner stay the same.
     *
     * @param newRoomId  the new room ID
     * @param newSlot    the new date and time
     * @param newPurpose the new purpose
     * @throws InvalidInputException if the new purpose is empty
     */
    public void update(String newRoomId, TimeSlot newSlot, String newPurpose) {
        checkPurpose(newPurpose);
        this.roomId = newRoomId;
        this.slot = newSlot;
        this.purpose = newPurpose;
    }

    /**
     * Cancels the booking.
     *
     * @throws ConflictException if the booking is already cancelled
     */
    public void cancel() {
        if (!isActive()) {
            throw new ConflictException("Booking is already cancelled");
        }
        status = BookingStatus.CANCELLED;
    }

    /**
     * Makes a cancelled booking active again. Used only by undo.
     */
    public void reactivate() {
        status = BookingStatus.ACTIVE;
    }

    /**
     * Checks whether this booking blocks a given room at a given time.
     * A cancelled booking never blocks anything.
     *
     * @param otherRoomId the room to check
     * @param otherSlot   the time to check
     * @return true if this booking is active, in the same room, and the times overlap
     */
    public boolean conflictsWith(String otherRoomId, TimeSlot otherSlot) {
        return isActive() && roomId.equals(otherRoomId) && slot.overlaps(otherSlot);
    }

    /**
     * Returns the order used for listing bookings: by date and start time, then by ID.
     *
     * @return the comparator
     */
    public static Comparator<Booking> byTime() {
        return BY_TIME;
    }

    private static void checkPurpose(String purpose) {
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new InvalidInputException("Purpose cannot be empty");
        }
    }
}


