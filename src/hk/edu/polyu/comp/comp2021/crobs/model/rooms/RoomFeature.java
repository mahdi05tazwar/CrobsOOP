package hk.edu.polyu.comp.comp2021.crobs.model.rooms;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Immutable value object representing a single room feature, normalized to lower case.
 */
public final class RoomFeature implements Comparable<RoomFeature> {
    private static final String NONE_KEYWORD = "none";
    private static final String CSV_SEPARATOR = ",";
    private static final String DISPLAY_SEPARATOR = ", ";
    private static final int SPLIT_KEEP_TRAILING_EMPTY = -1;

    private final String name;

    /**
     * Constructs a {@code RoomFeature} with the specified feature name.
     *
     * @param name the feature name, which must be non-empty and free of whitespace
     * @throws InvalidInputException if {@code name} is null, empty, or contains whitespace
     */
    public RoomFeature(String name) {
        if (name == null || name.isEmpty()) {
            throw new InvalidInputException("Feature name cannot be empty");
        }
        for (int i = 0; i < name.length(); i++) {
        if (Character.isWhitespace(name.charAt(i))) {
            throw new InvalidInputException("Feature name cannot contain whitespace: " + name);
        }
    }
        this.name = name.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the normalized lower-case feature name.
     *
     * @return the feature name
     */
    public String getName() {
        return name;
    }

    /**
     * Parses a comma-separated feature list without spaces into a sorted set.
     * Passing {@code "none"} (case-insensitive) yields an empty set.
     *
     * @param csv comma-separated feature list or {@code "none"}
     * @return a sorted {@link Set} of parsed {@code RoomFeature} objects
     * @throws InvalidInputException if {@code csv} is null, empty, contains empty items,
     *                               or contains whitespace
     */
    public static Set<RoomFeature> parseList(String csv) {
        if (csv == null || csv.isEmpty()) {
            throw new InvalidInputException("Features cannot be empty; use none for no features");
        }
        if (NONE_KEYWORD.equalsIgnoreCase(csv)) {
            return new TreeSet<>();
        }
        String[] parts = csv.split(CSV_SEPARATOR, SPLIT_KEEP_TRAILING_EMPTY);
        Set<RoomFeature> features = new TreeSet<>();
        for (String part : parts) {
        if (part.isEmpty()) {
            throw new InvalidInputException("Feature list contains an empty item: " + csv);
        }
        features.add(new RoomFeature(part));
    }
        return features;
    }

    /**
     * Formats a set of features as {@code "a, b"} in sorted order, or {@code "none"} if null or empty.
     *
     * @param features set of features to format
     * @return formatted feature display string
     */
    public static String format(Set<RoomFeature> features) {
        if (features == null || features.isEmpty()) {
            return NONE_KEYWORD;
        }
        Set<RoomFeature> sorted = new TreeSet<>(features);
        return sorted.stream()
            .map(RoomFeature::getName)
            .collect(Collectors.joining(DISPLAY_SEPARATOR));
    }

    /**
     * Compares this feature lexicographically with another feature by normalized name.
     *
     * @param other the other {@code RoomFeature} to compare against
     * @return negative, zero, or positive as this feature's name precedes, matches, or follows {@code other}
     */
    @Override
    public int compareTo(RoomFeature other) {
        Objects.requireNonNull(other, "Cannot compare to null RoomFeature");
        return name.compareTo(other.getName());
    }

    /**
     * Indicates whether another object is equal to this feature by normalized name.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if {@code o} is a {@code RoomFeature} with the same name
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoomFeature)) {
            return false;
        }
        RoomFeature other = (RoomFeature) o;
        return name.equals(other.getName());
    }

    /**
     * Returns the hash code of this feature's normalized name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the normalized feature name.
     *
     * @return the feature name
     */
    @Override
    public String toString() {
        return name;
    }
}