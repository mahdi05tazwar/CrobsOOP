package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.bookings.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.ScheduleEntry;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.TimeSlot;
import hk.edu.polyu.comp.comp2021.crobs.model.core.CrobsSystem;
import hk.edu.polyu.comp.comp2021.crobs.view.OutputFormatter;
import hk.edu.polyu.comp.comp2021.crobs.view.ParsedCommand;

import java.util.List;

/**
 * Handles the booking commands: book, modifyBooking, deleteBooking,
 * search, schedule and listBookings.
 * Each handler only checks the number of arguments, calls one CrobsSystem
 * method and formats the result. All rules live in the model.
 */
public class BookingController {

    private static final String BOOK_USAGE = "book roomId date startTime endTime purpose";
    private static final int BOOK_MIN_ARGS = 5;
    private static final int BOOK_ROOM = 0;
    private static final int BOOK_DATE = 1;
    private static final int BOOK_START = 2;
    private static final int BOOK_END = 3;
    private static final int BOOK_PURPOSE = 4;

    private static final String MODIFY_USAGE = "modifyBooking bookingId roomId date startTime endTime purpose";
    private static final int MODIFY_MIN_ARGS = 6;
    private static final int MODIFY_BOOKING = 0;
    private static final int MODIFY_ROOM = 1;
    private static final int MODIFY_DATE = 2;
    private static final int MODIFY_START = 3;
    private static final int MODIFY_END = 4;
    private static final int MODIFY_PURPOSE = 5;

    private static final String DELETE_USAGE = "deleteBooking bookingId";
    private static final int DELETE_ARGS = 1;
    private static final int DELETE_BOOKING = 0;

    private static final String SEARCH_USAGE = "search date startTime endTime minCapacity features";
    private static final int SEARCH_ARGS = 5;
    private static final int SEARCH_DATE = 0;
    private static final int SEARCH_START = 1;
    private static final int SEARCH_END = 2;
    private static final int SEARCH_CAPACITY = 3;
    private static final int SEARCH_FEATURES = 4;

    private static final String SCHEDULE_USAGE = "schedule roomId weekStartDate";
    private static final int SCHEDULE_ARGS = 2;
    private static final int SCHEDULE_ROOM = 0;
    private static final int SCHEDULE_WEEK_START = 1;

    private static final String LIST_USAGE = "listBookings";
    private static final int LIST_ARGS = 0;

    private final CrobsSystem system;

    /**
     * Creates the controller and registers its six commands.
     *
     * @param system   the model facade that carries out each command
     * @param registry the main controller that maps command names to handlers
     */
    public BookingController(CrobsSystem system, CommandController registry) {
        this.system = system;
        registry.register("book", this::book);
        registry.register("modifyBooking", this::modifyBooking);
        registry.register("deleteBooking", this::deleteBooking);
        registry.register("search", this::search);
        registry.register("schedule", this::schedule);
        registry.register("listBookings", this::listBookings);
    }

    private String book(ParsedCommand cmd) {
        cmd.requireMinArgs(BOOK_MIN_ARGS, BOOK_USAGE);
        Booking booking = system.book(
                cmd.arg(BOOK_ROOM),
                cmd.arg(BOOK_DATE),
                cmd.arg(BOOK_START),
                cmd.arg(BOOK_END),
                cmd.joinFrom(BOOK_PURPOSE));
        return "Booked: " + OutputFormatter.formatBooking(booking);
    }

    private String modifyBooking(ParsedCommand cmd) {
        cmd.requireMinArgs(MODIFY_MIN_ARGS, MODIFY_USAGE);
        Booking booking = system.modifyBooking(
                cmd.arg(MODIFY_BOOKING),
                cmd.arg(MODIFY_ROOM),
                cmd.arg(MODIFY_DATE),
                cmd.arg(MODIFY_START),
                cmd.arg(MODIFY_END),
                cmd.joinFrom(MODIFY_PURPOSE));
        return "Updated booking: " + OutputFormatter.formatBooking(booking);
    }

    private String deleteBooking(ParsedCommand cmd) {
        cmd.requireArgs(DELETE_ARGS, DELETE_USAGE);
        Booking booking = system.deleteBooking(cmd.arg(DELETE_BOOKING));
        return "Cancelled booking " + booking.getId() + ".";
    }

    private String search(ParsedCommand cmd) {
        cmd.requireArgs(SEARCH_ARGS, SEARCH_USAGE);
        return OutputFormatter.formatRooms(system.searchAvailableRooms(
                cmd.arg(SEARCH_DATE),
                cmd.arg(SEARCH_START),
                cmd.arg(SEARCH_END),
                cmd.arg(SEARCH_CAPACITY),
                cmd.arg(SEARCH_FEATURES)));
    }

    private String schedule(ParsedCommand cmd) {
        cmd.requireArgs(SCHEDULE_ARGS, SCHEDULE_USAGE);
        String roomId = cmd.arg(SCHEDULE_ROOM);
        String weekStart = cmd.arg(SCHEDULE_WEEK_START);
        List<ScheduleEntry> entries = system.getWeeklySchedule(roomId, weekStart);
        return OutputFormatter.formatSchedule(roomId, TimeSlot.parseDate(weekStart), entries);
    }

    private String listBookings(ParsedCommand cmd) {
        cmd.requireArgs(LIST_ARGS, LIST_USAGE);
        return OutputFormatter.formatBookings(system.getMyBookings());
    }
}


