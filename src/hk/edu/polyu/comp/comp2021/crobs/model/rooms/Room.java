package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Domain entity representing a campus room with an immutable ID, type, seating capacity,
 * and a sorted set of features.
 */
public class Room {
    private static final int MIN_CAPACITY = 1;

    private final String id;
    private RoomType type;
    private int capacity;
    private Set<RoomFeature> features;

    /**
     * Constructs and validates a {@code Room}.
     *
     * @param id       unique room identifier
     * @param type     room type
     * @param capacity seating capacity (must be at least 1)
     * @param features set of room features
     * @throws InvalidInputException if any argument is null or fails validation
     */
    public Room(String id, RoomType type, int capacity, Set<RoomFeature> features) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("Room ID cannot be empty");
        }
        this.id = id;
        setType(type);
        setCapacity(capacity);
        setFeatures(features);
    }

    /**
     * Returns the unique room identifier.
     *
     * @return the room ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the room type.
     *
     * @return the {@link RoomType}
     */
    public RoomType getType() {
        return type;
    }

    /**
     * Returns the room seating capacity.
     *
     * @return the seating capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns an unmodifiable view of this room's features.
     *
     * @return unmodifiable set of {@link RoomFeature} instances
     */
    public Set<RoomFeature> getFeatures() {
        return Collections.unmodifiableSet(features);
    }

    /**
     * Updates the room type.
     *
     * @param type new {@link RoomType}
     * @throws InvalidInputException if {@code type} is null
     */
    public void setType(RoomType type) {
        if (type == null) {
            throw new InvalidInputException("Room type cannot be null");
        }
        this.type = type;
    }

    /**
     * Updates the room seating capacity.
     *
     * @param capacity new seating capacity (must be at least 1)
     * @throws InvalidInputException if {@code capacity} is less than 1
     */
    public void setCapacity(int capacity) {
        if (capacity < MIN_CAPACITY) {
            throw new InvalidInputException("Capacity must be at least " + MIN_CAPACITY);
        }
        this.capacity = capacity;
    }

    /**
     * Replaces this room's features with a defensive sorted copy of {@code features}.
     *
     * @param features new set of features
     * @throws InvalidInputException if {@code features} is null or contains a null element
     */
    public void setFeatures(Set<RoomFeature> features) {
        if (features == null) {
            throw new InvalidInputException("Features set cannot be null");
        }
        Set<RoomFeature> copy = new TreeSet<>();
        for (RoomFeature feature : features) {
            if (feature == null) {
                throw new InvalidInputException("Features set cannot contain null elements");
            }
            copy.add(feature);
        }
        this.features = copy;
    }

    /**
     * Checks whether this room contains all features in {@code required}.
     *
     * @param required set of required features
     * @return {@code true} if all required features are present
     * @throws InvalidInputException if {@code required} is null
     */
    public boolean hasAllFeatures(Set<RoomFeature> required) {
        if (required == null) {
            throw new InvalidInputException("Required features set cannot be null");
        }
        return features.containsAll(required);
    }

    /**
     * Checks whether this room meets both the minimum capacity and required features.
     *
     * @param minCapacity minimum required seating capacity
     * @param required    set of required features
     * @return {@code true} if {@code capacity >= minCapacity} and {@code hasAllFeatures(required)} is true
     * @throws InvalidInputException if {@code required} is null
     */
    public boolean fits(int minCapacity, Set<RoomFeature> required) {
        return capacity >= minCapacity && hasAllFeatures(required);
    }
}