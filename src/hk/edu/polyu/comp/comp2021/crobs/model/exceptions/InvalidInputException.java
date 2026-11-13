package hk.edu.polyu.comp.comp2021.crobs.model.exceptions;

/**
 * happens due to bad format or value
 */
public class InvalidInputException extends CrobsException {
    /**
     * @param msg message to be shown to user
     */
    public InvalidInputException(String msg) {
        super(msg);
    }
}
