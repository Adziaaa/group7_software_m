/*
 * UndoRedoManagerTest.java
 *
 * JUnit 4 unit tests for the Undo / Redo feature.
 * Target class: org.jhotdraw.undo.UndoRedoManager
 *
 * Place this file at:
 *   src/test/java/org/jhotdraw/undo/UndoRedoManagerTest.java
 */
package org.jhotdraw.undo;

import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UndoRedoManagerTest {

    private UndoRedoManager manager;

    @Before
    public void setUp() {
        manager = new UndoRedoManager();
    }

    /**
     * A simple in-memory edit used as a test stub so the unit test does not
     * depend on the real Drawing/Figure model. It records whether it is
     * currently in the "done" or "undone" state.
     */
    private static class StubEdit extends AbstractUndoableEdit {
        private static final long serialVersionUID = 1L;
        boolean done = true;

        @Override
        public void undo() {
            super.undo();
            done = false;
        }

        @Override
        public void redo() {
            super.redo();
            done = true;
        }
    }

    // ---------- Best-case scenarios ----------

    @Test
    public void newManagerCannotUndoOrRedo() {
        assertFalse("A fresh manager should not be able to undo", manager.canUndo());
        assertFalse("A fresh manager should not be able to redo", manager.canRedo());
    }

    @Test
    public void addingEditEnablesUndo() {
        manager.addEdit(new StubEdit());
        assertTrue("After adding an edit, undo should be possible", manager.canUndo());
        assertFalse("Before undoing, redo should not be possible", manager.canRedo());
    }

    @Test
    public void undoThenRedoRestoresState() {
        StubEdit edit = new StubEdit();
        manager.addEdit(edit);

        manager.undo();
        assertFalse("Edit should be undone after undo()", edit.done);
        assertTrue("After undo, redo should be possible", manager.canRedo());

        manager.redo();
        assertTrue("Edit should be re-done after redo()", edit.done);
        assertTrue("After redo, undo should be possible again", manager.canUndo());
    }

    @Test
    public void undoActionAndRedoActionAreProvided() {
        assertNotNull("Undo action must be exposed for the menu", manager.getUndoAction());
        assertNotNull("Redo action must be exposed for the menu", manager.getRedoAction());
    }

    @Test
    public void actionsReflectAvailability() {
        Action undoAction = manager.getUndoAction();
        Action redoAction = manager.getRedoAction();

        assertFalse("Undo action should start disabled", undoAction.isEnabled());
        assertFalse("Redo action should start disabled", redoAction.isEnabled());

        manager.addEdit(new StubEdit());
        assertTrue("Undo action should be enabled after an edit", undoAction.isEnabled());
        assertFalse("Redo action should be disabled before any undo", redoAction.isEnabled());
    }

    @Test
    public void significantEditFlagIsSet() {
        assertFalse("No significant edits on a fresh manager", manager.hasSignificantEdits());
        manager.addEdit(new StubEdit());
        assertTrue("A significant edit should set the flag", manager.hasSignificantEdits());
    }

    // ---------- Boundary cases ----------

    @Test(expected = javax.swing.undo.CannotUndoException.class)
    public void undoWithNothingToUndoThrows() {
        manager.undo();
    }

    @Test(expected = javax.swing.undo.CannotRedoException.class)
    public void redoWithNothingToRedoThrows() {
        manager.redo();
    }

    @Test
    public void discardAllEditsClearsHistory() {
        manager.addEdit(new StubEdit());
        manager.addEdit(new StubEdit());

        manager.discardAllEdits();

        assertFalse("After discardAllEdits, undo should not be possible", manager.canUndo());
        assertFalse("After discardAllEdits, redo should not be possible", manager.canRedo());
        assertFalse("Significant-edits flag should be reset", manager.hasSignificantEdits());
    }

    /**
     * DISCARD_ALL_EDITS is an edit whose canUndo()/canRedo() both return false.
     * This test verifies the manager's observed behaviour when such an edit
     * becomes the edit-to-be-undone, rather than assuming a "disable" contract
     * that the class does not explicitly implement.
     */
    @Test
    public void discardAllEditsEditCannotBeUndoneOrRedone() {
        manager.addEdit(UndoRedoManager.DISCARD_ALL_EDITS);

        assertFalse("DISCARD_ALL_EDITS should not be undoable", manager.canUndo());
        assertFalse("DISCARD_ALL_EDITS should not be redoable", manager.canRedo());
    }

    @Test
    public void multipleUndosInSequence() {
        StubEdit first = new StubEdit();
        StubEdit second = new StubEdit();
        manager.addEdit(first);
        manager.addEdit(second);

        manager.undo();
        assertFalse("Most recent edit should be undone first", second.done);
        assertTrue("Earlier edit should still be done", first.done);

        manager.undo();
        assertFalse("Earlier edit should be undone second", first.done);
        assertFalse("No further undo should be possible", manager.canUndo());
    }
}
