package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.model.bookings.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.ScheduleEntry;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomFeature;
import hk.edu.polyu.comp.comp2021.crobs.model.users.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stateless formatter for converting model objects and collections into CLI/GUI strings.
 */
public final class OutputFormatter {
    private static final int DAYS_IN_WEEK = 7;
    private static final int SCHEDULE_END_OFFSET = DAYS_IN_WEEK - 1;
    private static final String LINE_SEPARATOR = "\n";

    private OutputFormatter() {
    }

    /**
     * Formats a user as {@code "<id> <name> <role>"}.
     *
     * @param u the {@link User} to format
     * @return formatted user string
     */
    public static String formatUser(User u) {
        Objects.requireNonNull(u, "User cannot be null");
        return u.getId() + " " + u.getName() + " " + u.getRole().getKeyword();
    }

    /**
     * Formats a room as {@code "<id> <type> capacity <capacity> features: <features>"}.
     *
     * @param r the {@link Room} to format
     * @return formatted room string
     */
    public static String formatRoom(Room r) {
        Objects.requireNonNull(r, "Room cannot be null");
        return r.getId()
                + " "
                + r.getType().getKeyword()
                + " capacity "
                + r.getCapacity()
                + " features: "
                + RoomFeature.format(r.getFeatures());
    }

    /**
     * Formats a list of rooms one per line, or {@code "No rooms found."} when empty.
     *
     * @param rooms list of rooms to format
     * @return formatted multi-line string or {@code "No rooms found."}
     */
    public static String formatRooms(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return "No rooms found.";
        }
        return rooms.stream()
                .map(OutputFormatter::formatRoom)
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    /**
     * Formats a booking as {@code "<id> <roomId> <slot> <status> <purpose>"}.
     *
     * @param b the {@link Booking} to format
     * @return formatted booking string
     */
    public static String formatBooking(Booking b) {
        Objects.requireNonNull(b, "Booking cannot be null");
        return b.getId()
                + " "
                + b.getRoomId()
                + " "
                + b.getSlot()
                + " "
                + b.getStatus().name()
                + " "
                + b.getPurpose();
    }

    /**
     * Formats a list of bookings one per line, or {@code "No bookings."} when empty.
     *
     * @param list list of bookings to format
     * @return formatted multi-line string or {@code "No bookings."}
     */
    public static String formatBookings(List<Booking> list) {
        if (list == null || list.isEmpty()) {
            return "No bookings.";
        }
        return list.stream()
                .map(OutputFormatter::formatBooking)
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    /**
     * Formats a 7-day weekly schedule header and all visible entries for a room.
     *
     * @param roomId    the room identifier
     * @param weekStart start date of the 7-day schedule
     * @param entries   list of {@link ScheduleEntry} records
     * @return formatted weekly schedule string
     */
    public static String formatSchedule(String roomId, LocalDate weekStart, List<ScheduleEntry> entries) {
        Objects.requireNonNull(roomId, "Room ID cannot be null");
        Objects.requireNonNull(weekStart, "Week start date cannot be null");
        LocalDate weekEnd = weekStart.plusDays(SCHEDULE_END_OFFSET);
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule for ")
                .append(roomId)
                .append(", ")
                .append(weekStart)
                .append(" to ")
                .append(weekEnd);
        if (entries == null || entries.isEmpty()) {
            sb.append(LINE_SEPARATOR).append("No bookings.");
        } else {
            for (ScheduleEntry entry : entries) {
                Booking b = entry.getBooking();
                sb.append(LINE_SEPARATOR)
                        .append(b.getSlot())
                        .append(" ")
                        .append(b.getId())
                        .append(" ")
                        .append(entry.getOwnerDisplay())
                        .append(" ")
                        .append(b.getPurpose());
            }
        }
        return sb.toString();
    }
}