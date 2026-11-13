package hk.edu.polyu.comp.comp2021.crobs.model.bookings;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

/**
 * A time interval on a single day: a date, a start time and an end time.
 * Instances are immutable.
 */
public final class TimeSlot implements Comparable<TimeSlot> {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;

    /**
     * Creates a time slot.
     *
     * @param date  the day of the slot
     * @param start the start time
     * @param end   the end time
     * @throws InvalidInputException if the start time is not before the end time
     */
    public TimeSlot(LocalDate date, LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new InvalidInputException("Start time must be before end time");
        }
        this.date = date;
        this.start = start;
        this.end = end;
    }

    /**
     * Builds a time slot from the text the user typed.
     *
     * @param date  the date as yyyy-MM-dd
     * @param start the start time as HH:mm
     * @param end   the end time as HH:mm
     * @return the matching time slot
     * @throws InvalidInputException if any value is badly formatted, or start is not before end
     */
    public static TimeSlot parse(String date, String start, String end) {
        try {
            return new TimeSlot(
                    LocalDate.parse(date, DATE_FORMAT),
                    LocalTime.parse(start, TIME_FORMAT),
                    LocalTime.parse(end, TIME_FORMAT));
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date/time. Use yyyy-MM-dd and HH:mm");
        }
    }

    /**
     * Reads a date from the text the user typed.
     *
     * @param date the date as yyyy-MM-dd
     * @return the matching date
     * @throws InvalidInputException if the date is badly formatted or does not exist
     */
    public static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date. Use yyyy-MM-dd");
        }
    }

    /**
     * Returns the day of this slot.
     *
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the start time of this slot.
     *
     * @return the start time
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * Returns the end time of this slot.
     *
     * @return the end time
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * Returns the date and start time together, for comparing against "now".
     *
     * @return the moment this slot starts
     */
    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(date, start);
    }

    /**
     * Checks whether this slot and another share any time.
     * Back-to-back slots (09:00-10:00 and 10:00-11:00) do not overlap.
     *
     * @param other the slot to compare with
     * @return true if both are on the same day and their times overlap
     */
    public boolean overlaps(TimeSlot other) {
        return date.equals(other.getDate())
                && start.isBefore(other.getEnd())
                && other.getStart().isBefore(end);
    }

    /**
     * Checks whether this slot's date falls inside a date range.
     *
     * @param from        the first day of the range (included)
     * @param toExclusive the day after the last day of the range (not included)
     * @return true if the date is on or after from, and before toExclusive
     */
    public boolean isWithin(LocalDate from, LocalDate toExclusive) {
        return !date.isBefore(from) && date.isBefore(toExclusive);
    }

    /**
     * Orders slots by date, then by start time.
     *
     * @param o the slot to compare with
     * @return a negative number, zero or a positive number if this slot
     *         comes before, at the same time as, or after the other
     */
    @Override
    public int compareTo(TimeSlot o) {
        int byDate = date.compareTo(o.getDate());
        if (byDate != 0) {
            return byDate;
        }
        return start.compareTo(o.getStart());
    }

    /**
     * Two slots are equal when date, start and end all match.
     *
     * @param obj the object to compare with
     * @return true if the other object is an equal time slot
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TimeSlot)) {
            return false;
        }
        TimeSlot other = (TimeSlot) obj;
        return date.equals(other.getDate())
                && start.equals(other.getStart())
                && end.equals(other.getEnd());
    }

    /**
     * Hash code built from date, start and end.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, start, end);
    }

    /**
     * Formats the slot as text, e.g. "2026-11-20 09:00-11:00".
     *
     * @return the slot as a string
     */
    @Override
    public String toString() {
        return date.format(DATE_FORMAT) + " " + start.format(TIME_FORMAT) + "-" + end.format(TIME_FORMAT);
    }
}