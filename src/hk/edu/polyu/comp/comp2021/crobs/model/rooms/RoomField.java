package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

/**
 * Modifiable fields of a {@link Room} used by the {@code modifyRoom} command.
 */
public enum RoomField {
    /**
     * Room type field ({@code roomType}).
     */
    ROOM_TYPE("roomType"),

    /**
     * Room seating capacity field ({@code capacity}).
     */
    CAPACITY("capacity"),

    /**
     * Room features field ({@code features}).
     */
    FEATURES("features");

    private final String keyword;

    private RoomField(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the command-line keyword for this room field.
     *
     * @return the field keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Parses a room field from its case-insensitive keyword representation.
     *
     * @param s the input string representing a room field
     * @return the matching {@code RoomField}
     * @throws InvalidInputException if {@code s} is null or does not match a supported field
     */
    public static RoomField fromString(String s) {
        if (s != null) {
            for (RoomField field : values()) {
                if (field.getKeyword().equalsIgnoreCase(s)) {
                    return field;
                }
            }
        }
        throw new InvalidInputException(
                "Unknown room field: " + s + ". Use roomType, capacity or features");
    }
}