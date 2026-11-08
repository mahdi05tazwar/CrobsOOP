package hk.edu.polyu.comp.comp2021.crobs.model.core;

/**
 * The class is to hold a simple undoable action implementing UndoableAction
 */
public class SimpleAction implements UndoableAction {
    private final String description;
    private final Runnable undoStep, redoStep;

    /**
     * @param description String describing the action
     * @param undoStep the Runnable object to run upon undo
     * @param redoStep the Runnable object to run upon redo
     */
    public SimpleAction(String description, Runnable undoStep, Runnable redoStep) {
        this.description = description;
        this.undoStep = undoStep;
        this.redoStep = redoStep;
    }

    @Override
    public void undo() {
        undoStep.run();
    }

    @Override
    public void redo() {
        redoStep.run();
    }

    @Override
    public String describe() {
        return description;
    }
}
