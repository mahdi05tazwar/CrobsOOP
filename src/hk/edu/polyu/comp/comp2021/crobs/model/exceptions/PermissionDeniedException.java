package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * happens when not logged in or not allowed
 */
public class PermissionDeniedException extends CrobsException {
    /**
     * @param msg message to be shown to user
     */
    public PermissionDeniedException(String msg) {
        super(msg);
    }
}
