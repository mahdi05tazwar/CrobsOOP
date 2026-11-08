package hk.edu.polyu.comp.comp2021.crobs.model.core;

/**
 * Interface representing actions that can be undone
 */
public interface UndoableAction {
    /**
     * Undo the action
     */
    void undo();

    /**
     * Redo the undone action
     */
    void redo();

    /**
     * @return a String description of the action e.g. "book B0003"
     */
    String describe();
}
