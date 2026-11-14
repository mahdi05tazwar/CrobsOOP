package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.core.IdGenerator;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Manages room storage in memory, ID allocation, modifications, and capability queries.
 */
public class RoomManager {
    private static final String ROOM_PREFIX = "R";
    private static final String NONE_KEYWORD = "none";
    private static final String CSV_SEPARATOR = ",";
    private static final int MIN_CAPACITY = 1;
    private static final int MIN_SEARCH_CAPACITY = 0;

    private final Map<String, Room> rooms = new TreeMap<>(IdGenerator.idOrder());
    private final IdGenerator ids = new IdGenerator(ROOM_PREFIX);

    /**
     * Creates and stores a new room with an automatically generated unique ID.
     * Arguments are validated before generating a new ID so failed attempts never consume IDs.
     *
     * @param type     room type
     * @param capacity seating capacity (at least 1)
     * @param features set of features
     * @return the newly created {@link Room}
     * @throws InvalidInputException if any room attribute is invalid
     */
    public Room addRoom(RoomType type, int capacity, Set<RoomFeature> features) {
        if (type == null) {
            throw new InvalidInputException("Room type cannot be null");
        }
        if (capacity < MIN_CAPACITY) {
            throw new InvalidInputException("Capacity must be at least " + MIN_CAPACITY);
        }
        if (features == null || features.contains(null)) {
            throw new InvalidInputException("Features set cannot be null or contain null");
        }
        String id = ids.next();
        Room room = new Room(id, type, capacity, features);
        rooms.put(id, room);
        return room;
    }

    /**
     * Retrieves a room by ID.
     *
     * @param id room identifier
     * @return the matching {@link Room}
     * @throws NotFoundException if no room exists with {@code id}
     */
    public Room getRoom(String id) {
        if (id == null) {
            throw new NotFoundException("Room not found: null");
        }
        Room room = rooms.get(id);
        if (room == null) {
            throw new NotFoundException("Room not found: " + id);
        }
        return room;
    }

    /**
     * Checks whether a room with the specified ID exists.
     *
     * @param id room identifier
     * @return {@code true} if the room exists, {@code false} otherwise
     */
    public boolean hasRoom(String id) {
        return id != null && rooms.containsKey(id);
    }

    /**
     * Removes and returns the room with the specified ID.
     *
     * @param id room identifier
     * @return the removed {@link Room}
     * @throws NotFoundException if no room exists with {@code id}
     */
    public Room removeRoom(String id) {
        if (id == null) {
            throw new NotFoundException("Room not found: null");
        }
        Room removed = rooms.remove(id);
        if (removed == null) {
            throw new NotFoundException("Room not found: " + id);
        }
        return removed;
    }

    /**
     * Re-inserts a previously created or deleted room into the manager (for undo/redo).
     *
     * @param room the {@link Room} to restore
     * @throws InvalidInputException if {@code room} is null
     */
    public void restore(Room room) {
        if (room == null) {
            throw new InvalidInputException("Room cannot be null");
        }
        rooms.put(room.getId(), room);
    }

    /**
     * Parses and applies a single field change to {@code room}, returning the previous value
     * encoded as a string suitable for passing back into {@code modifyField} during undo.
     *
     * @param room     the {@link Room} to update
     * @param field    the {@link RoomField} to modify
     * @param newValue raw string representation of the new value
     * @return the old field value encoded as a parseable string
     * @throws InvalidInputException if any argument is null or {@code newValue} is invalid
     */
    public String modifyField(Room room, RoomField field, String newValue) {
        if (room == null) {
            throw new InvalidInputException("Room cannot be null");
        }
        if (field == null) {
            throw new InvalidInputException("Room field cannot be null");
        }
        if (newValue == null) {
            throw new InvalidInputException("New value cannot be null");
        }
        switch (field) {
            case ROOM_TYPE:
                RoomType parsedType = RoomType.fromString(newValue);
                String oldType = room.getType().getKeyword();
                room.setType(parsedType);
                return oldType;
            case CAPACITY:
                final int parsedCapacity;
                try {
                    parsedCapacity = Integer.parseInt(newValue);
                } catch (NumberFormatException e) {
                    throw new InvalidInputException("Capacity must be an integer: " + newValue);
                }
                String oldCapacity = String.valueOf(room.getCapacity());
                room.setCapacity(parsedCapacity);
                return oldCapacity;
            case FEATURES:
                Set<RoomFeature> parsedFeatures = RoomFeature.parseList(newValue);
                String oldFeatures = formatFeaturesForUndo(room.getFeatures());
                room.setFeatures(parsedFeatures);
                return oldFeatures;
            default:
                throw new InvalidInputException("Unsupported room field: " + field);
        }
    }

    private static String formatFeaturesForUndo(Set<RoomFeature> features) {
        if (features.isEmpty()) {
            return NONE_KEYWORD;
        }
        return features.stream()
                .map(RoomFeature::getName)
                .collect(Collectors.joining(CSV_SEPARATOR));
    }

    /**
     * Returns all rooms sorted in ascending order of room ID.
     *
     * @return unmodifiable list of all rooms sorted by ID
     */
    public List<Room> getAllRooms() {
        return Collections.unmodifiableList(new ArrayList<>(rooms.values()));
    }

    /**
     * Finds all rooms matching the minimum capacity and required features, sorted by room ID.
     *
     * @param minCapacity minimum required seating capacity (non-negative)
     * @param required    set of required features
     * @return unmodifiable list of matching rooms sorted by ID
     * @throws InvalidInputException if {@code minCapacity} is negative or {@code required} is null
     */
    public List<Room> findRooms(int minCapacity, Set<RoomFeature> required) {
        if (minCapacity < MIN_SEARCH_CAPACITY) {
            throw new InvalidInputException("Minimum capacity cannot be negative");
        }
        if (required == null) {
            throw new InvalidInputException("Required features set cannot be null");
        }
        List<Room> matching = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (room.fits(minCapacity, required)) {
                matching.add(room);
            }
        }
        return Collections.unmodifiableList(matching);
    }
}