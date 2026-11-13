package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.bookings.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.BookingStatus;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.ScheduleEntry;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.PermissionDeniedException;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomType;
import hk.edu.polyu.comp.comp2021.crobs.model.users.RegularUser;
import hk.edu.polyu.comp.comp2021.crobs.model.users.Role;
import hk.edu.polyu.comp.comp2021.crobs.model.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the CrobsSystem facade. "Now" is fixed at 2026-11-10 08:00 Hong Kong time,
 * so bookings on 2026-11-20 are in the future.
 */
class CrobsSystemTest {

    private static final ZoneId HK = ZoneId.of("Asia/Hong_Kong");
    private static final String DAY = "2026-11-20";

    private MutableClock clock;
    private CrobsSystem system;
    private String superId;

    /** A clock whose time the test can move forward. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(LocalDateTime start) {
            now = start.atZone(HK).toInstant();
        }

        void set(LocalDateTime time) {
            now = time.atZone(HK).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return HK;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock(LocalDateTime.of(2026, 11, 10, 8, 0));
        system = new CrobsSystem(clock);
        superId = system.getSuperAdminId();
    }

    // ---------- helpers ----------

    private String newRegularUser(String name) {
        system.login(superId);
        String id = system.addUser(name).getId();
        system.logout();
        return id;
    }

    private String newRoom(String type, String capacity, String features) {
        system.login(superId);
        String id = system.addRoom(type, capacity, features).getId();
        system.logout();
        return id;
    }

    // ---------- construction ----------

    @Test
    void superAdminExistsAtStartup() {
        assertEquals("U0001", superId);
        User su = system.login(superId);
        assertEquals(Role.SUPER_ADMIN, su.getRole());
    }

    @Test
    void defaultConstructorUsesSystemClock() {
        CrobsSystem s = new CrobsSystem();
        assertNotNull(s.getSuperAdminId());
    }

    // ---------- login / logout ----------

    @Test
    void loginAndGetCurrentUser() {
        User u = system.login(superId);
        assertSame(u, system.getCurrentUser());
    }

    @Test
    void loginUnknownUserThrows() {
        assertThrows(NotFoundException.class, () -> system.login("U9999"));
    }

    @Test
    void loginTwiceThrows() {
        system.login(superId);
        assertThrows(ConflictException.class, () -> system.login(superId));
    }

    @Test
    void logoutReturnsUser() {
        User u = system.login(superId);
        assertSame(u, system.logout());
        assertThrows(PermissionDeniedException.class, () -> system.getCurrentUser());
    }

    @Test
    void logoutWithoutLoginThrows() {
        assertThrows(PermissionDeniedException.class, () -> system.logout());
    }

    @Test
    void commandsRequireLogin() {
        assertThrows(PermissionDeniedException.class, () -> system.getAllRooms());
        assertThrows(PermissionDeniedException.class, () -> system.getRoom("R0001"));
        assertThrows(PermissionDeniedException.class, () -> system.getMyBookings());
        assertThrows(PermissionDeniedException.class, () -> system.book("R0001", DAY, "09:00", "10:00", "x"));
        assertThrows(PermissionDeniedException.class, () -> system.undo());
        assertThrows(PermissionDeniedException.class, () -> system.redo());
    }

    // ---------- addUser / removeUser ----------

    @Test
    void adminCanAddUser() {
        system.login(superId);
        RegularUser u = system.addUser("alice");
        assertEquals("U0002", u.getId());
        assertEquals("alice", u.getName());
        assertEquals(Role.REGULAR, u.getRole());
    }

    @Test
    void regularUserCannotAddUser() {
        String alice = newRegularUser("alice");
        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.addUser("bob"));
    }

    @Test
    void addUserRequiresLogin() {
        assertThrows(PermissionDeniedException.class, () -> system.addUser("bob"));
    }

    @Test
    void undoAndRedoAddUser() {
        system.login(superId);
        String id = system.addUser("alice").getId();

        assertEquals("addUser " + id, system.undo().describe());
        system.logout();
        assertThrows(NotFoundException.class, () -> system.login(id));

        system.login(superId);
        system.addUser("temp");
        system.undo();
        system.redo();
    }

    @Test
    void redoAddUserRestoresUser() {
        system.login(superId);
        String id = system.addUser("alice").getId();
        system.undo();
        assertEquals("addUser " + id, system.redo().describe());
        system.logout();
        assertEquals("alice", system.login(id).getName());
    }

    @Test
    void removeUserCancelsAllTheirBookings() {
        String alice = newRegularUser("alice");
        String room = newRoom("meetingRoom", "10", "none");
        system.login(alice);
        Booking b1 = system.book(room, DAY, "09:00", "10:00", "one");
        Booking b2 = system.book(room, "2026-11-21", "09:00", "10:00", "two");
        system.logout();

        system.login(superId);
        List<Booking> cancelled = system.removeUser(alice);
        assertEquals(2, cancelled.size());
        assertFalse(b1.isActive());
        assertFalse(b2.isActive());
        system.logout();
        assertThrows(NotFoundException.class, () -> system.login(alice));
    }

    @Test
    void regularUserCannotRemoveUser() {
        String alice = newRegularUser("alice");
        String bob = newRegularUser("bob");
        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.removeUser(bob));
    }

    @Test
    void removeUnknownUserThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.removeUser("U9999"));
    }

    @Test
    void undoRemoveUserRestoresUserAndBookings() {
        String alice = newRegularUser("alice");
        String room = newRoom("meetingRoom", "10", "none");
        system.login(alice);
        Booking b = system.book(room, DAY, "09:00", "10:00", "one");
        system.logout();

        system.login(superId);
        system.removeUser(alice);
        assertEquals("removeUser " + alice, system.undo().describe());
        assertTrue(b.isActive());

        system.redo();
        assertFalse(b.isActive());

        system.undo();
        system.logout();
        assertEquals("alice", system.login(alice).getName());
    }

    // ---------- changeRole ----------

    @Test
    void superAdminCanPromoteUser() {
        String alice = newRegularUser("alice");
        system.login(superId);
        User promoted = system.changeRole(alice, "admin");
        assertEquals(Role.ADMIN, promoted.getRole());
        assertEquals(alice, promoted.getId());
        system.logout();

        system.login(alice);
        assertNotNull(system.addRoom("seminarRoom", "20", "none"));
    }

    @Test
    void adminCannotChangeRoles() {
        String alice = newRegularUser("alice");
        String bob = newRegularUser("bob");
        system.login(superId);
        system.changeRole(alice, "admin");
        system.logout();

        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.changeRole(bob, "admin"));
    }

    @Test
    void changeRoleToUnknownRoleThrows() {
        String alice = newRegularUser("alice");
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.changeRole(alice, "king"));
    }

    @Test
    void changeRoleOfUnknownUserThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.changeRole("U9999", "admin"));
    }

    @Test
    void undoAndRedoChangeRole() {
        String alice = newRegularUser("alice");
        system.login(superId);
        system.changeRole(alice, "admin");

        assertEquals("changeRole " + alice, system.undo().describe());
        system.logout();
        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.addRoom("seminarRoom", "20", "none"));
        system.logout();

        system.login(superId);
        system.changeRole(alice, "admin");
        system.undo();
        system.redo();
        system.logout();
        system.login(alice);
        assertNotNull(system.addRoom("seminarRoom", "20", "none"));
    }

    // ---------- rooms ----------

    @Test
    void adminCanAddRoom() {
        system.login(superId);
        Room r = system.addRoom("computerLab", "40", "projector,whiteboard");
        assertEquals("R0001", r.getId());
        assertEquals(RoomType.COMPUTER_LAB, r.getType());
        assertEquals(40, r.getCapacity());
        assertEquals(2, r.getFeatures().size());
    }

    @Test
    void regularUserCannotAddRoom() {
        String alice = newRegularUser("alice");
        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.addRoom("computerLab", "40", "none"));
    }

    @Test
    void addRoomRejectsNonNumericCapacity() {
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.addRoom("computerLab", "forty", "none"));
    }

    @Test
    void addRoomRejectsCapacityBelowOne() {
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.addRoom("computerLab", "0", "none"));
    }

    @Test
    void addRoomRejectsUnknownType() {
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.addRoom("kitchen", "10", "none"));
    }

    @Test
    void undoAndRedoAddRoom() {
        system.login(superId);
        String id = system.addRoom("meetingRoom", "8", "none").getId();

        assertEquals("addRoom " + id, system.undo().describe());
        assertThrows(NotFoundException.class, () -> system.getRoom(id));

        system.redo();
        assertEquals(id, system.getRoom(id).getId());
    }

    @Test
    void getAllRoomsSortedById() {
        system.login(superId);
        system.addRoom("meetingRoom", "8", "none");
        system.addRoom("seminarRoom", "30", "none");
        List<Room> all = system.getAllRooms();
        assertEquals(2, all.size());
        assertEquals("R0001", all.get(0).getId());
        assertEquals("R0002", all.get(1).getId());
    }

    @Test
    void regularUserCanViewRooms() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        assertEquals(room, system.getRoom(room).getId());
        assertEquals(1, system.getAllRooms().size());
    }

    @Test
    void getUnknownRoomThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.getRoom("R9999"));
    }

    @Test
    void modifyRoomChangesFieldAndUndoRestoresIt() {
        system.login(superId);
        String id = system.addRoom("meetingRoom", "8", "none").getId();

        Room r = system.modifyRoom(id, "capacity", "12");
        assertEquals(12, r.getCapacity());

        assertEquals("modifyRoom " + id, system.undo().describe());
        assertEquals(8, system.getRoom(id).getCapacity());

        system.redo();
        assertEquals(12, system.getRoom(id).getCapacity());
    }

    @Test
    void modifyRoomRequiresAdmin() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.modifyRoom(room, "capacity", "12"));
    }

    @Test
    void modifyUnknownRoomThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.modifyRoom("R9999", "capacity", "12"));
    }

    @Test
    void modifyRoomUnknownFieldThrows() {
        system.login(superId);
        String id = system.addRoom("meetingRoom", "8", "none").getId();
        assertThrows(InvalidInputException.class, () -> system.modifyRoom(id, "colour", "red"));
    }

    @Test
    void deleteRoomCancelsOnlyFutureBookings() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        Booking early = system.book(room, "2026-11-11", "09:00", "10:00", "early");
        Booking late = system.book(room, DAY, "09:00", "10:00", "late");
        system.logout();

        clock.set(LocalDateTime.of(2026, 11, 12, 8, 0));
        system.login(superId);
        List<Booking> cancelled = system.deleteRoom(room);

        assertEquals(1, cancelled.size());
        assertSame(late, cancelled.get(0));
        assertTrue(early.isActive());
        assertFalse(late.isActive());
        assertThrows(NotFoundException.class, () -> system.getRoom(room));
    }

    @Test
    void undoAndRedoDeleteRoom() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        Booking b = system.book(room, DAY, "09:00", "10:00", "meeting");
        system.logout();

        system.login(superId);
        system.deleteRoom(room);
        assertEquals("deleteRoom " + room, system.undo().describe());
        assertEquals(room, system.getRoom(room).getId());
        assertTrue(b.isActive());

        system.redo();
        assertThrows(NotFoundException.class, () -> system.getRoom(room));
        assertFalse(b.isActive());
    }

    @Test
    void deleteRoomRequiresAdmin() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.deleteRoom(room));
    }

    @Test
    void deleteUnknownRoomThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.deleteRoom("R9999"));
    }

    // ---------- book ----------

    @Test
    void bookCreatesBookingForCurrentUser() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);

        Booking b = system.book(room, DAY, "09:00", "11:00", "team meeting");
        assertEquals("B0001", b.getId());
        assertEquals(alice, b.getOwnerId());
        assertEquals(room, b.getRoomId());
        assertEquals("team meeting", b.getPurpose());
        assertTrue(b.isActive());
    }

    @Test
    void bookUnknownRoomThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.book("R9999", DAY, "09:00", "10:00", "x"));
    }

    @Test
    void bookInThePastThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.book(room, "2026-11-09", "09:00", "10:00", "x"));
        assertThrows(InvalidInputException.class, () -> system.book(room, "2026-11-10", "07:00", "09:00", "x"));
    }

    @Test
    void bookWithBadDateThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.book(room, "20-11-2026", "09:00", "10:00", "x"));
    }

    @Test
    void overlappingBookingThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        system.book(room, DAY, "09:00", "11:00", "first");
        assertThrows(ConflictException.class, () -> system.book(room, DAY, "10:00", "12:00", "second"));
    }

    @Test
    void backToBackBookingsAllowed() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        system.book(room, DAY, "09:00", "10:00", "first");
        assertNotNull(system.book(room, DAY, "10:00", "11:00", "second"));
    }

    @Test
    void undoAndRedoBook() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();

        assertEquals("book " + id, system.undo().describe());
        assertTrue(system.getMyBookings().isEmpty());

        system.redo();
        assertEquals(1, system.getMyBookings().size());
    }

    // ---------- modifyBooking ----------

    @Test
    void ownerCanModifyBooking() {
        String r1 = newRoom("meetingRoom", "8", "none");
        String r2 = newRoom("seminarRoom", "30", "none");
        system.login(superId);
        Booking b = system.book(r1, DAY, "09:00", "10:00", "old");

        Booking m = system.modifyBooking(b.getId(), r2, "2026-11-21", "14:00", "15:00", "new");
        assertSame(b, m);
        assertEquals(b.getId(), m.getId());
        assertEquals(superId, m.getOwnerId());
        assertEquals(r2, m.getRoomId());
        assertEquals(LocalTime.of(14, 0), m.getSlot().getStart());
        assertEquals("new", m.getPurpose());
    }

    @Test
    void modifyBookingMayOverlapItself() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        Booking b = system.book(room, DAY, "09:00", "11:00", "x");
        system.modifyBooking(b.getId(), room, DAY, "10:00", "12:00", "x");
        assertEquals(LocalTime.of(12, 0), b.getSlot().getEnd());
    }

    @Test
    void modifyBookingIntoOtherBookingThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        system.book(room, DAY, "09:00", "10:00", "a");
        Booking b = system.book(room, DAY, "11:00", "12:00", "b");
        assertThrows(ConflictException.class,
                () -> system.modifyBooking(b.getId(), room, DAY, "09:30", "11:30", "b"));
    }

    @Test
    void nonOwnerCannotModifyBooking() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();
        system.logout();

        system.login(alice);
        assertThrows(PermissionDeniedException.class,
                () -> system.modifyBooking(id, room, DAY, "11:00", "12:00", "y"));
    }

    @Test
    void modifyCancelledBookingThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();
        system.deleteBooking(id);
        assertThrows(ConflictException.class,
                () -> system.modifyBooking(id, room, DAY, "11:00", "12:00", "y"));
    }

    @Test
    void modifyBookingIntoPastThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();
        assertThrows(InvalidInputException.class,
                () -> system.modifyBooking(id, room, "2026-11-01", "09:00", "10:00", "x"));
    }

    @Test
    void modifyBookingToUnknownRoomThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();
        assertThrows(NotFoundException.class,
                () -> system.modifyBooking(id, "R9999", DAY, "09:00", "10:00", "x"));
    }

    @Test
    void modifyUnknownBookingThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        assertThrows(NotFoundException.class,
                () -> system.modifyBooking("B9999", room, DAY, "09:00", "10:00", "x"));
    }

    @Test
    void undoAndRedoModifyBooking() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        Booking b = system.book(room, DAY, "09:00", "10:00", "old");
        system.modifyBooking(b.getId(), room, DAY, "13:00", "14:00", "new");

        assertEquals("modifyBooking " + b.getId(), system.undo().describe());
        assertEquals(LocalTime.of(9, 0), b.getSlot().getStart());
        assertEquals("old", b.getPurpose());

        system.redo();
        assertEquals(LocalTime.of(13, 0), b.getSlot().getStart());
        assertEquals("new", b.getPurpose());
    }

    // ---------- deleteBooking ----------

    @Test
    void ownerCanCancelBooking() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();

        Booking b = system.deleteBooking(id);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertNotNull(system.book(room, DAY, "09:00", "10:00", "slot is free again"));
    }

    @Test
    void nonOwnerCannotCancelBooking() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();
        system.logout();

        system.login(alice);
        assertThrows(PermissionDeniedException.class, () -> system.deleteBooking(id));
    }

    @Test
    void cancelUnknownBookingThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.deleteBooking("B9999"));
    }

    @Test
    void undoAndRedoDeleteBooking() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        Booking b = system.book(room, DAY, "09:00", "10:00", "x");
        system.deleteBooking(b.getId());

        assertEquals("deleteBooking " + b.getId(), system.undo().describe());
        assertTrue(b.isActive());

        system.redo();
        assertFalse(b.isActive());
    }

    // ---------- search ----------

    @Test
    void searchFiltersByAvailabilityCapacityAndFeatures() {
        String small = newRoom("meetingRoom", "8", "projector");
        String big = newRoom("lectureTheatre", "100", "projector,microphone");
        String booked = newRoom("seminarRoom", "50", "projector");
        system.login(superId);
        system.book(booked, DAY, "09:00", "12:00", "busy");

        List<Room> found = system.searchAvailableRooms(DAY, "10:00", "11:00", "20", "projector");
        assertEquals(1, found.size());
        assertEquals(big, found.get(0).getId());

        List<Room> anyFeatures = system.searchAvailableRooms(DAY, "10:00", "11:00", "0", "none");
        assertEquals(2, anyFeatures.size());
        assertEquals(small, anyFeatures.get(0).getId());
    }

    @Test
    void searchRequiresLogin() {
        assertThrows(PermissionDeniedException.class,
                () -> system.searchAvailableRooms(DAY, "10:00", "11:00", "0", "none"));
    }

    @Test
    void searchRejectsNonNumericCapacity() {
        system.login(superId);
        assertThrows(InvalidInputException.class,
                () -> system.searchAvailableRooms(DAY, "10:00", "11:00", "many", "none"));
    }

    @Test
    void searchRejectsNegativeCapacity() {
        system.login(superId);
        assertThrows(InvalidInputException.class,
                () -> system.searchAvailableRooms(DAY, "10:00", "11:00", "-1", "none"));
    }

    @Test
    void searchRejectsBadTimes() {
        system.login(superId);
        assertThrows(InvalidInputException.class,
                () -> system.searchAvailableRooms(DAY, "11:00", "10:00", "0", "none"));
    }

    // ---------- schedule ----------

    @Test
    void scheduleHidesOtherOwnersFromRegularUser() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        String bob = newRegularUser("bob");
        system.login(alice);
        system.book(room, DAY, "13:00", "14:00", "alice's");
        system.logout();
        system.login(bob);
        system.book(room, DAY, "09:00", "10:00", "bob's");

        List<ScheduleEntry> s = system.getWeeklySchedule(room, "2026-11-16");
        assertEquals(2, s.size());
        assertEquals("bob's", s.get(0).getBooking().getPurpose());
        assertTrue(s.get(0).isOwnerVisible());
        assertEquals(bob, s.get(0).getOwnerDisplay());
        assertFalse(s.get(1).isOwnerVisible());
    }

    @Test
    void scheduleShowsAllOwnersToAdmin() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        system.book(room, DAY, "09:00", "10:00", "x");
        system.logout();

        system.login(superId);
        List<ScheduleEntry> s = system.getWeeklySchedule(room, "2026-11-16");
        assertEquals(1, s.size());
        assertTrue(s.get(0).isOwnerVisible());
        assertEquals(alice, s.get(0).getOwnerDisplay());
    }

    @Test
    void scheduleCoversExactlySevenDays() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        system.book(room, "2026-11-16", "09:00", "10:00", "first day");
        system.book(room, "2026-11-22", "09:00", "10:00", "last day");
        system.book(room, "2026-11-23", "09:00", "10:00", "next week");

        List<ScheduleEntry> s = system.getWeeklySchedule(room, "2026-11-16");
        assertEquals(2, s.size());
        assertEquals("first day", s.get(0).getBooking().getPurpose());
        assertEquals("last day", s.get(1).getBooking().getPurpose());
    }

    @Test
    void scheduleSkipsCancelledBookings() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        String id = system.book(room, DAY, "09:00", "10:00", "x").getId();
        system.deleteBooking(id);
        assertTrue(system.getWeeklySchedule(room, "2026-11-16").isEmpty());
    }

    @Test
    void scheduleForUnknownRoomThrows() {
        system.login(superId);
        assertThrows(NotFoundException.class, () -> system.getWeeklySchedule("R9999", "2026-11-16"));
    }

    @Test
    void scheduleWithBadDateThrows() {
        String room = newRoom("meetingRoom", "8", "none");
        system.login(superId);
        assertThrows(InvalidInputException.class, () -> system.getWeeklySchedule(room, "16/11/2026"));
    }

    // ---------- listBookings ----------

    @Test
    void myBookingsAreOnlyMineSortedAndIncludeCancelled() {
        String room = newRoom("meetingRoom", "8", "none");
        String alice = newRegularUser("alice");
        system.login(alice);
        system.book(room, "2026-11-21", "09:00", "10:00", "later");
        Booking earlier = system.book(room, DAY, "15:00", "16:00", "earlier");
        system.deleteBooking(earlier.getId());
        system.logout();
        system.login(superId);
        system.book(room, DAY, "09:00", "10:00", "not alice's");
        system.logout();

        system.login(alice);
        List<Booking> mine = system.getMyBookings();
        assertEquals(2, mine.size());
        assertEquals("earlier", mine.get(0).getPurpose());
        assertFalse(mine.get(0).isActive());
        assertEquals("later", mine.get(1).getPurpose());
    }

    // ---------- undo / redo ----------

    @Test
    void undoWithNothingThrows() {
        system.login(superId);
        assertThrows(ConflictException.class, () -> system.undo());
    }

    @Test
    void redoWithNothingThrows() {
        system.login(superId);
        assertThrows(ConflictException.class, () -> system.redo());
    }

    @Test
    void logoutClearsHistory() {
        system.login(superId);
        system.addUser("alice");
        system.logout();
        system.login(superId);
        assertThrows(ConflictException.class, () -> system.undo());
    }

    @Test
    void loginClearsRedo() {
        system.login(superId);
        system.addUser("alice");
        system.undo();
        system.logout();
        system.login(superId);
        assertThrows(ConflictException.class, () -> system.redo());
    }

    @Test
    void newActionClearsRedo() {
        system.login(superId);
        system.addUser("alice");
        system.undo();
        system.addUser("bob");
        assertThrows(ConflictException.class, () -> system.redo());
    }
}
