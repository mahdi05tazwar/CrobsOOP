package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.core.users.User;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

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

    public CrobsSystem() {
        this(Clock.systemDefaultZone());
    }

    public String getSuperAdminId() {
        return superAdminId;
    }

    public User login(String userId) {
        User user = users.getUser(userId);
        session.login(user);
        history.clear();
        return user;
    }

    public User logout() {
        session.requireUser();
        User user = session.logout();
        history.clear();
        return user;
    }

    public User getCurrentUser() {
        return session.requireUser();
    }

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
        if (parsedCapacity < 1) throw new InvalidInputException("Capacity must be at least 1");

        return parsedCapacity;
    }


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
     * Returns all rooms sorted by ID.
     * @return all rooms sorted by ID
     */
    public List<Room> getAllRooms() {
        session.requireUser();
        return rooms.getAllRooms();
    }




}
