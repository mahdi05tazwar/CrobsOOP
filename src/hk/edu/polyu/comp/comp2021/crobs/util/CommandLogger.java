package hk.edu.polyu.comp.comp2021.crobs.util;

/**
 * Interface for command logging systems.
 */
public interface CommandLogger extends AutoCloseable {
    /**
     * @param command command to be logged
     */
    void log(String command);

    @Override
    void close();
}