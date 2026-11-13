package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

/**
 * The state of a booking.
 */
public enum BookingStatus {
    /** The booking is in effect and blocks its room for its time slot. */
    ACTIVE,
    /** The booking was cancelled and no longer blocks its room. */
    CANCELLED
}

