package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * happens when an action clashes with the system's current state
 */
public class ConflictException extends CrobsException {
    /**
     * @param msg message to be shown to user
     */
    public ConflictException(String msg) {
        super(msg);
    }
}
