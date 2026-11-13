package hk.edu.polyu.comp.comp2021.crobs.model.core;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryTest {

    private History history;
    private List<String> calls;

    @BeforeEach
    void setUp() {
        history = new History();
        calls = new ArrayList<>();
    }

    private UndoableAction action(String name) {
        return new SimpleAction(name, () -> calls.add("undo " + name), () -> calls.add("redo " + name));
    }

    @Test
    void newHistoryHasNothingToUndoOrRedo() {
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoOnEmptyHistoryThrows() {
        assertThrows(ConflictException.class, () -> history.undo());
    }

    @Test
    void redoOnEmptyHistoryThrows() {
        assertThrows(ConflictException.class, () -> history.redo());
    }

    @Test
    void recordMakesUndoAvailable() {
        history.record(action("a"));
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoRunsActionAndMovesItToRedo() {
        UndoableAction a = action("a");
        history.record(a);

        assertSame(a, history.undo());
        assertEquals(Arrays.asList("undo a"), calls);
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    void redoRunsActionAndMovesItBackToUndo() {
        UndoableAction a = action("a");
        history.record(a);
        history.undo();

        assertSame(a, history.redo());
        assertEquals(Arrays.asList("undo a", "redo a"), calls);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoIsLastInFirstOut() {
        history.record(action("a"));
        history.record(action("b"));
        history.undo();
        history.undo();
        assertEquals(Arrays.asList("undo b", "undo a"), calls);
    }

    @Test
    void redoReappliesInOriginalOrder() {
        history.record(action("a"));
        history.record(action("b"));
        history.undo();
        history.undo();
        history.redo();
        history.redo();
        assertEquals(Arrays.asList("undo b", "undo a", "redo a", "redo b"), calls);
    }

    @Test
    void recordingNewActionClearsRedo() {
        history.record(action("a"));
        history.undo();
        assertTrue(history.canRedo());

        history.record(action("b"));
        assertFalse(history.canRedo());
        assertThrows(ConflictException.class, () -> history.redo());
    }

    @Test
    void clearEmptiesBothStacks() {
        history.record(action("a"));
        history.record(action("b"));
        history.undo();

        history.clear();
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }
}
