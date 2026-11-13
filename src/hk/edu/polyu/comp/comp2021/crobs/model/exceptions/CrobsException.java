package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * This is the base type for all Crobs errors
 */
public class CrobsException extends RuntimeException {
    /**
     * @param msg message to be shown to user
     */
    public CrobsException(String msg) {
        super(msg);
    }
}
