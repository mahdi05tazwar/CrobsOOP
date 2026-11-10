package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.users.Role;
import hk.edu.polyu.comp.comp2021.crobs.model.users.User;
import hk.edu.polyu.comp.comp2021.crobs.model.users.RegularUser;
import hk.edu.polyu.comp.comp2021.crobs.model.users.UserManager;

import hk.edu.polyu.comp.comp2021.crobs.model.rooms.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomFeature;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomField;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomManager;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomType;

import hk.edu.polyu.comp.comp2021.crobs.model.bookings.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.BookingManager;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.ScheduleEntry;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.TimeSlot;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.PermissionDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class coordinates all model entities to conduct application's high-level operations through a single interface.
 */
public class CrobsSystem {
    private final UserManager users;
    private final RoomManager rooms;
    private final BookingManager bookings;
    private final Session session;
    private final History history;
    private final Clock clock;
    private final String superAdminId;

    private static final int DAYS_IN_WEEK = 7;
    private static final int MIN_ROOM_CAPACITY = 1;
    private static final int MIN_SEARCH_CAPACITY = 0;

    /**
     * Initialises the Crobs system using the specified clock.
     * @param clock the clock used for time-dependent operations
     */
    public CrobsSystem(Clock clock) {
        users = new UserManager();
        rooms = new RoomManager();
        bookings = new BookingManager();
        session = new Session();
        history = new History();
        this.clock = clock;

        User superAdmin = users.createSuperAdmin();
        superAdminId = superAdmin.getId();
    }

    /**
     * Initialises the Crobs system using the system's default time zone.
     */
    public CrobsSystem() {
        this(Clock.systemDefaultZone());
    }

    /**
     * @return id of superadmin
     */
    public String getSuperAdminId() {
        return superAdminId;
    }

    /**
     * Log in a user
     * @param userId id of user being logged in
     * @return logged in user
     */
    public User login(String userId) {
        User user = users.getUser(userId);
        session.login(user);
        history.clear();
        return user;
    }

    /**
     * Log out a user
     * @return logged out user
     */
    public User logout() {
        session.requireUser();
        User user = session.logout();
        history.clear();
        return user;
    }

    /**
     * @return current user
     */
    public User getCurrentUser() {
        return session.requireUser();
    }

    /**
     * Add a new regular user to users (accessible to admins only)
     * @param name name of new user
     * @return a RegularUser object corresponding to this new user
     */
    public RegularUser addUser(String name) {
        session.requireAdmin();
        RegularUser user = users.addRegularUser(name);
        String id = user.getId();

        history.record(new SimpleAction(
                "addUser "+id,
                ()->users.removeUser(id),
                ()->users.restore(user)
        ));

        return user;
    }

    /**
     * Remove a user and cancel all their bookings (accessible to admins only)
     * @param id id of user
     * @return list of bookings cancelled
     */
    public List<Booking> removeUser(String id) {
        session.requireAdmin();
        User user = users.removeUser(id);
        List<Booking> cancelledBookings = bookings.cancelAllByUser(id);

        history.record(new SimpleAction(
                "removeUser "+id,
                ()-> {users.restore(user); for (Booking b: cancelledBookings) b.reactivate();},
                ()-> {users.removeUser(id); for (Booking b: cancelledBookings) b.cancel();}
        ));
        return cancelledBookings;
    }

    /**
     * Change role of a user (accessible to superadmin only)
     * @param id id of user
     * @param newRole new role to be assigned
     * @return affected user
     */
    public User changeRole(String id, String newRole) {
        session.requireSuperAdmin();
        User uOld = users.getUser(id);
        User user = users.changeRole(id, Role.fromString(newRole));

        history.record(new SimpleAction(
                "changeRole "+id,
                ()-> users.replace(uOld),
                ()-> users.replace(user)
        ));

        return user;
    }

    /**
     * Add a new room to the system.
     * @param type type of room
     * @param capacity capacity of room
     * @param features features the room will have
     * @return Room object corresponding to the new room
     */
    public Room addRoom(String type, String capacity, String features) {
        session.requireAdmin();
        Room room = rooms.addRoom(RoomType.fromString(type), parseCapacity(capacity), RoomFeature.parseList(features));
        String id = room.getId();

        history.record(new SimpleAction(
                "addRoom "+id,
                ()-> rooms.removeRoom(id),
                ()-> rooms.restore(room)
        ));

        return room;
    }
    private int parseCapacity(String capacity) {
        final int parsedCapacity;
        try {
            parsedCapacity = Integer.parseInt(capacity);
        }
        catch (NumberFormatException e) {
            throw new InvalidInputException("Capacity must be an integer");
        }
        if (parsedCapacity < MIN_ROOM_CAPACITY) throw new InvalidInputException("Capacity must be at least 1");

        return parsedCapacity;
    }


    /**
     * To modify a room's fields/properties
     * @param id id of room to be modified
     * @param field field to be modified
     * @param newValue new value for this field
     * @return the Room object that underwent modification
     */
    public Room modifyRoom(String id, String field, String newValue) {
        session.requireAdmin();
        Room room = rooms.getRoom(id);
        RoomField rf = RoomField.fromString(field);
        String oldValue = rooms.modifyField(room, rf, newValue);

        history.record(new SimpleAction(
                "modifyRoom "+id,
                ()-> rooms.modifyField(room, rf, oldValue),
                ()-> rooms.modifyField(room, rf, newValue)
        ));

        return room;
    }

    /**
     * Deletes a room and cancels its future bookings.
     * @param roomId the room ID
     * @return the bookings cancelled by this deletion
     */
    public List<Booking> deleteRoom(String roomId) {
        session.requireAdmin();
        Room deletedRoom = rooms.removeRoom(roomId);
        List<Booking> cancelledBookings = bookings.cancelFutureByRoom(roomId, LocalDateTime.now(clock));

        history.record(new SimpleAction(
                "deleteRoom " + roomId,
                () -> {rooms.restore(deletedRoom); for (Booking booking : cancelledBookings) booking.reactivate();},
                () -> {rooms.removeRoom(roomId); for (Booking booking : cancelledBookings) booking.cancel();}
        ));

        return cancelledBookings;
    }

    /**
     * Returns a room by its ID.
     * @param roomId the room ID
     * @return the requested room
     */
    public Room getRoom(String roomId) {
        session.requireUser();
        return rooms.getRoom(roomId);
    }

    /**
     * @return all rooms sorted by ID
     */
    public List<Room> getAllRooms() {
        session.requireUser();
        return rooms.getAllRooms();
    }

    /**
     * Book a room.
     * @param roomId id of room to be booked.
     * @param date date of booking
     * @param start start time
     * @param end end time
     * @param purpose reason of booking
     * @return the booking
     */
    public Booking book(String roomId, String date, String start, String end, String purpose) {
        User user = session.requireUser();
        rooms.getRoom(roomId);

        TimeSlot slot = TimeSlot.parse(date, start, end);
        checkNotPast(slot);

        Booking booking = bookings.createBooking(user.getId(), roomId, slot, purpose);
        String id = booking.getId();

        history.record(new SimpleAction(
                "book " + id,
                () -> bookings.remove(id),
                () -> bookings.restore(booking)
        ));

        return booking;
    }

    /**
     * Modifies a booking.
     * @param bookingId id of booking to be modified
     * @param roomId id of room involved in booking
     * @param date date of booking as String
     * @param start start time of booking as String
     * @param end end time of booking as String
     * @param purpose String explanation of new purpose of booking
     * @return the modified Booking object
     */
    public Booking modifyBooking(String bookingId, String roomId, String date, String start, String end, String purpose) {
        User user = session.requireUser();
        Booking booking = bookings.getBooking(bookingId);
        checkOwner(booking, user);
        if (!booking.isActive()) throw new ConflictException("Booking is already cancelled");
        rooms.getRoom(roomId);

        TimeSlot newSlot = TimeSlot.parse(date, start, end);
        checkNotPast(newSlot);

        String oldRoomId = booking.getRoomId();
        TimeSlot oldSlot = booking.getSlot();
        String oldPurpose = booking.getPurpose();

        bookings.modifyBooking(booking, roomId, newSlot, purpose);

        history.record(new SimpleAction(
                "modifyBooking " + bookingId,
                () -> booking.update(oldRoomId, oldSlot, oldPurpose),
                () -> bookings.modifyBooking(booking, roomId, newSlot, purpose)
        ));

        return booking;
    }

    /**
     * Deletes a booking.
     * @param bookingId id of booking to be deleted.
     * @return Booking object holding the deleted booking
     */
    public Booking deleteBooking(String bookingId) {
        User user = session.requireUser();
        Booking booking = bookings.getBooking(bookingId);

        checkOwner(booking, user);
        booking.cancel();

        history.record(new SimpleAction(
                "deleteBooking " + bookingId,
                () -> booking.reactivate(),
                () -> booking.cancel()
        ));

        return booking;
    }

    /**
     * Returns rooms available at a specific time
     * @param date date of booking
     * @param start start time
     * @param end end time
     * @param minCapacity minimum capacity required
     * @param features features needed in the available rooms
     * @return list of eligible rooms
     */
    public List<Room> searchAvailableRooms(String date, String start, String end, String minCapacity, String features) {
        session.requireUser();
        TimeSlot slot = TimeSlot.parse(date, start, end);
        int capacity = parseMinCapacity(minCapacity);
        Set<RoomFeature> requiredFeatures = RoomFeature.parseList(features);

        List<Room> matchingRooms = rooms.findRooms(capacity, requiredFeatures);
        List<Room> availableRooms = new ArrayList<Room>();

        for (Room room : matchingRooms) {
            if (bookings.isRoomAvailable(room.getId(), slot)) availableRooms.add(room);
        }

        return availableRooms;
    }

    /**
     * Returns the seven-day schedule for a room starting from the specified date.
     * @param roomId the ID of the room
     * @param weekStartDate the first date of the week in yyyy-MM-dd format
     * @return the room's active bookings within the seven-day period
     */
    public List<ScheduleEntry> getWeeklySchedule(String roomId, String weekStartDate) {
        User user = session.requireUser();
        rooms.getRoom(roomId);

        LocalDate weekStart = TimeSlot.parseDate(weekStartDate);
        LocalDate weekEnd = weekStart.plusDays(DAYS_IN_WEEK);

        List<Booking> roomBookings = bookings.getActiveBookingsForRoom(roomId, weekStart, weekEnd);
        List<ScheduleEntry> schedule = new ArrayList<ScheduleEntry>();

        for (Booking booking : roomBookings) {
            boolean ownerVisible = user.canSeeAllBookingOwners() || booking.isOwnedBy(user.getId());
            schedule.add(new ScheduleEntry(booking, ownerVisible));
        }

        return schedule;
    }

    /**
     * @return list of bookings made by current user
     */
    public List<Booking> getMyBookings() {
        User user = session.requireUser();
        return bookings.getBookingsByUser(user.getId());
    }

    /**
     * @return action undone
     */
    public UndoableAction undo() {
        session.requireUser();
        return history.undo();
    }

    /**
     * @return action redone
     */
    public UndoableAction redo() {
        session.requireUser();
        return history.redo();
    }

    // my other helpers
    private void checkNotPast(TimeSlot slot) {
        if (slot.getStartDateTime().isBefore(LocalDateTime.now(clock))) {
            throw new InvalidInputException("Booking cannot be in the past");
        }
    }
    private void checkOwner(Booking booking, User user) {
        if (!booking.isOwnedBy(user.getId())) {
            throw new PermissionDeniedException("You do not own this booking");
        }
    }
    private int parseMinCapacity(String minCapacity) {
        final int parsedCapacity;
        try {
            parsedCapacity = Integer.parseInt(minCapacity);
        }
        catch (NumberFormatException e) {
            throw new InvalidInputException("Minimum capacity must be an integer");
        }

        if (parsedCapacity < MIN_SEARCH_CAPACITY) {
            throw new InvalidInputException("Minimum capacity must be at least 0");
        }
        return parsedCapacity;
    }


}
