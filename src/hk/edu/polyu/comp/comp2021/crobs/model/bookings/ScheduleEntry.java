package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

/**
 * One line of a room's weekly schedule: a booking, plus whether the
 * person viewing the schedule is allowed to see who made it.
 * Instances are immutable.
 */
public final class ScheduleEntry {

    private static final String HIDDEN_OWNER = "(hidden)";

    private final Booking booking;
    private final boolean ownerVisible;

    /**
     * Creates a schedule entry.
     *
     * @param booking      the booking shown on this line
     * @param ownerVisible true if the viewer may see the owner's user ID
     */
    public ScheduleEntry(Booking booking, boolean ownerVisible) {
        this.booking = booking;
        this.ownerVisible = ownerVisible;
    }

    /**
     * Returns the booking shown on this line.
     *
     * @return the booking
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * Tells whether the viewer may see the owner's user ID.
     *
     * @return true if the owner is visible
     */
    public boolean isOwnerVisible() {
        return ownerVisible;
    }

    /**
     * Returns the text to show in the owner column of the schedule.
     *
     * @return the owner's user ID if visible, otherwise "(hidden)"
     */
    public String getOwnerDisplay() {
        if (ownerVisible) {
            return booking.getOwnerId();
        }
        return HIDDEN_OWNER;
    }
}

