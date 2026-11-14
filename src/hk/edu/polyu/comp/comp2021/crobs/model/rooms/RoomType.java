package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

/**
 * Supported room types in the Campus Room Booking System.
 */
public enum RoomType {
    /**
     * Lecture theatre room type.
     */
    LECTURE_THEATRE("lectureTheatre"),

    /**
     * Computer laboratory room type.
     */
    COMPUTER_LAB("computerLab"),

    /**
     * Seminar room type.
     */
    SEMINAR_ROOM("seminarRoom"),

    /**
     * Meeting room type.
     */
    MEETING_ROOM("meetingRoom");

    private final String keyword;

    private RoomType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the command-line keyword for this room type.
     *
     * @return the room type keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Parses a room type from its case-insensitive keyword representation.
     *
     * @param s the input string representing a room type
     * @return the matching {@code RoomType}
     * @throws InvalidInputException if {@code s} is null or does not match a supported room type
     */
    public static RoomType fromString(String s) {
        if (s != null) {
            for (RoomType type : values()) {
                if (type.getKeyword().equalsIgnoreCase(s)) {
                    return type;
                }
            }
        }
        throw new InvalidInputException(
                "Unknown room type: " + s + ". Use lectureTheatre, computerLab, seminarRoom or meetingRoom");
    }
}
