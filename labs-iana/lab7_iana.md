# Testing — Undo / Redo

**Feature:** Undo / Redo
**Class under test:** `org.jhotdraw.undo.UndoRedoManager`
**Framework:** JUnit 4

## Maven dependency

JUnit 4 is added to the project `pom.xml`:

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

## Unit under test and isolation strategy

`UndoRedoManager` holds the domain logic of the feature: it maintains the undo/redo stacks, exposes the undo and redo menu actions, and tracks whether significant edits exist. The `UndoAction` menu class was not chosen as the primary unit because it mostly delegates to the action registered in the view's action map, which would pull in Swing view dependencies.

To keep each test focused on a single code path through `UndoRedoManager` without depending on the real `Drawing`/`Figure` model, a lightweight test stub (`StubEdit`, extending `AbstractUndoableEdit`) is used. The stub records its own done/undone state, so the tests can assert that undo and redo actually flip the edit's state without involving any real figures. This is the stub/mock approach recommended in the lab: when execution would pass outside the unit into a dependency, that dependency is replaced.

## Best-case scenarios verified

- A freshly created manager can neither undo nor redo.
- Adding an edit enables undo and leaves redo disabled.
- Undoing then redoing an edit restores the edit's state, and the availability of undo/redo flips correctly at each step.
- The manager exposes non-null undo and redo `Action` objects for the menu.
- The enabled state of those actions tracks the availability of undo/redo (disabled initially, undo enabled after an edit).
- Adding a significant edit sets the `hasSignificantEdits` flag.

## Boundary cases verified

- Calling `undo()` with nothing to undo throws `CannotUndoException`.
- Calling `redo()` with nothing to redo throws `CannotRedoException`.
- `discardAllEdits()` clears the history so that neither undo nor redo is possible and the significant-edits flag is reset.
- The special `DISCARD_ALL_EDITS` edit disables both undo and redo.
- Multiple edits are undone in last-in-first-out order, and undo becomes unavailable once the stack is exhausted.

## Assertions and exceptions

The tests follow the lab's distinction between the two mechanisms. JUnit assertions (`assertTrue`, `assertFalse`, `assertEquals`, `assertNotNull`) verify expected post-conditions of normal operation. The error boundaries — undoing or redoing when nothing is available — are verified with `@Test(expected = ...)`, because `UndoRedoManager` is designed to raise `CannotUndoException` / `CannotRedoException` in those situations and continue running, rather than halt the program. This matches the principle that exceptions signal recoverable conditions the program can continue past, whereas assertions guard conditions that should never occur.

## How the feature was verified

The feature is verified at class level by exercising the public business behavior of `UndoRedoManager` — adding edits, undoing, redoing, querying availability, and clearing history — across both the normal path and the boundary conditions. Each test isolates a single behavior using the `StubEdit` test double, so a failure points to a specific code path. Together the tests confirm that the manager correctly tracks edit state, keeps the undo/redo actions consistent with that state, and handles empty-stack conditions through the expected exceptions.

# Unit Tests — Undo / Redo

**Feature:** Undo / Redo
**Class under test:** `org.jhotdraw.undo.UndoRedoManager`
**Framework:** JUnit 4

The following JUnit 4 test class exercises the domain logic of `UndoRedoManager` across best-case and boundary scenarios. A lightweight test stub (`StubEdit`) is used in place of the real `Drawing`/`Figure` model so that each test isolates a single code path through the manager.

```java
/*
 * UndoRedoManagerTest.java
 *
 * JUnit 4 unit tests for the Undo / Redo feature.
 * Target class: org.jhotdraw.undo.UndoRedoManager
 */
package org.jhotdraw.undo;

import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void discardAllEditsEditDisablesManager() {
        manager.addEdit(new StubEdit());
        // The special edit that disables undo/redo (canUndo/canRedo == false).
        manager.addEdit(UndoRedoManager.DISCARD_ALL_EDITS);

        assertFalse("DISCARD_ALL_EDITS should disable undo", manager.canUndo());
        assertFalse("DISCARD_ALL_EDITS should disable redo", manager.canRedo());
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
```

## Coverage summary

**Best-case scenarios**
- A freshly created manager can neither undo nor redo.
- Adding an edit enables undo and leaves redo disabled.
- Undoing then redoing restores the edit's state, with availability flipping correctly at each step.
- The manager exposes non-null undo and redo `Action` objects.
- The enabled state of those actions tracks undo/redo availability.
- Adding a significant edit sets the `hasSignificantEdits` flag.

**Boundary cases**
- Undoing with nothing to undo throws `CannotUndoException`.
- Redoing with nothing to redo throws `CannotRedoException`.
- `discardAllEdits()` clears history and resets the significant-edits flag.
- The special `DISCARD_ALL_EDITS` edit disables both undo and redo.
- Multiple edits are undone in last-in-first-out order, and undo becomes unavailable once the stack is exhausted.
