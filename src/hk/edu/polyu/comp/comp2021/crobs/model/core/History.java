package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Keeps track of actions so they may be undone and redone as needed
 */
public class History {
    private final Deque<UndoableAction> undoActionsStack = new ArrayDeque<>(),
            redoActionsStack = new ArrayDeque<>();

    /**
     * Records a new action and puts it in the undoable actions stack so it may be undone (popped) if needed
     * Upon a new action, it clears the existing redo queue as that's no longer valid
     * @param action the new action to be recorded
     */
    void record(UndoableAction action) {
        undoActionsStack.push(action);
        redoActionsStack.clear();
    }

    /**
     * Undoes an action by popping from undo stack (throws error if stack empty) and pushing to redo stack.
     * @return the action that has been undone
     */
    UndoableAction undo() {
        if (! canUndo()) throw new ConflictException("No action to undo.");
        UndoableAction action = undoActionsStack.pop();
        action.undo();
        redoActionsStack.push(action);
        return action;
    }

    /**
     * Redoes an action by popping from redo stack (throws error if stack empty) and pushing to undo stack.
     * @return the action that has been redone
     */
    UndoableAction redo() {
        if (! canRedo()) throw new ConflictException("No action to redo.");
        UndoableAction action = redoActionsStack.pop();
        action.redo();
        undoActionsStack.push(action);
        return action;
    }

    /**
     * @return true if there is something to undo
     */
    boolean canUndo() {
        return ! undoActionsStack.isEmpty();
    }

    /**
     * @return true if there is something to redo
     */
    boolean canRedo() {
        return ! redoActionsStack.isEmpty();
    }

    /**
     * Clears history (both undo and redo stack)
     */
    void clear() {
        undoActionsStack.clear();
        redoActionsStack.clear();
    }
}
