package hk.edu.polyu.comp.comp2021.crobs.model.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SimpleActionTest {

    @Test
    void describeReturnsDescription() {
        SimpleAction action = new SimpleAction("book B0003", () -> { }, () -> { });
        assertEquals("book B0003", action.describe());
    }

    @Test
    void undoRunsOnlyTheUndoStep() {
        List<String> calls = new ArrayList<>();
        SimpleAction action = new SimpleAction("x", () -> calls.add("undo"), () -> calls.add("redo"));
        action.undo();
        assertEquals(Arrays.asList("undo"), calls);
    }

    @Test
    void redoRunsOnlyTheRedoStep() {
        List<String> calls = new ArrayList<>();
        SimpleAction action = new SimpleAction("x", () -> calls.add("undo"), () -> calls.add("redo"));
        action.redo();
        assertEquals(Arrays.asList("redo"), calls);
    }

    @Test
    void stepsCanRunRepeatedly() {
        List<String> calls = new ArrayList<>();
        SimpleAction action = new SimpleAction("x", () -> calls.add("undo"), () -> calls.add("redo"));
        action.undo();
        action.redo();
        action.undo();
        assertEquals(Arrays.asList("undo", "redo", "undo"), calls);
    }

    @Test
    void isAnUndoableAction() {
        assertInstanceOf(UndoableAction.class, new SimpleAction("x", () -> { }, () -> { }));
    }
}
