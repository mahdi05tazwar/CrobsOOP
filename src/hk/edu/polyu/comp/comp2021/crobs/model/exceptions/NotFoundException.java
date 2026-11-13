package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * happens due to unknown ID
 */
public class NotFoundException extends CrobsException {
    /**
     * @param message message to be shown to user
     */
    public NotFoundException(String message) {
        super(message);
    }
}
