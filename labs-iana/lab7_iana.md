# Testing — Undo / Redo

**Feature:** Undo / Redo
**Class under test:** `org.jhotdraw.undo.UndoRedoManager`
**Framework:** JUnit 4
**Test file:** `jhotdraw-core/src/test/java/org/jhotdraw/undo/UndoRedoManagerTest.java` (in the `jhotdraw-core` module, the same module that contains `UndoRedoManager` and its `Labels.properties` resource bundle)

## Maven dependency and module placement

`UndoRedoManager` and its internationalisation bundle (`org/jhotdraw/undo/Labels.properties`) both live in the `jhotdraw-core` module. The test must therefore be placed and run inside `jhotdraw-core`, so that the module's `src/main/resources` — and hence `Labels.properties` — is on the test classpath. The manager's constructor calls `getLabels()`, which loads that bundle; running the test from any other module produces `MissingResourceException: Can't find bundle for base name org.jhotdraw.undo.Labels`.

The lab specifies JUnit 4 (noting that Swing and JUnit extensions work best with it), so the JUnit 4 dependency is added to `jhotdraw-core/pom.xml`:

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

If the parent `jhotdraw` POM manages the JUnit version through `<dependencyManagement>`, the `<version>` element can be omitted so the parent controls it. The test is then run from the core module, for example with `mvn -pl jhotdraw-core test`, which puts the resource bundle on the classpath and allows the manager to construct cleanly.

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
- The special `DISCARD_ALL_EDITS` edit reports `canUndo()`/`canRedo()` as false, so the manager offers neither undo nor redo for it.
- Multiple edits are undone in last-in-first-out order, and undo becomes unavailable once the stack is exhausted.

## Assertions and exceptions

The lab distinguishes two mechanisms, and it is worth being precise about which is used here.

The lab PDF refers to Java *language* assertions (the `assert` statement, enabled with `-ea`), which are intended for invariants that should never occur and which, when enabled, halt execution by throwing `AssertionError`. The tests in this feature instead use **JUnit assertions** (`assertTrue`, `assertFalse`, `assertNotNull`). These do not halt the JVM; a failed JUnit assertion fails the individual test and lets the suite continue. JUnit assertions are the appropriate tool here because the goal is to verify post-conditions of the unit under test and report each failure independently, not to guard a runtime invariant inside production code.

The error boundaries — undoing or redoing when nothing is available — are verified with `@Test(expected = ...)`, because `UndoRedoManager` is designed to raise `CannotUndoException` / `CannotRedoException` in those situations and continue running, rather than halt the program. This matches the lab's principle that an exception signals a recoverable condition the program can continue past, whereas an assertion guards a condition that should never occur.

## How the feature was verified

The feature is verified at class level by exercising the public business behavior of `UndoRedoManager` — adding edits, undoing, redoing, querying availability, and clearing history — across both the normal path and the boundary conditions. Each test isolates a single behavior using the `StubEdit` test double, so a failure points to a specific code path. Together the tests confirm that the manager correctly tracks edit state, keeps the undo/redo actions consistent with that state, and handles empty-stack conditions through the expected exceptions.

---

## Unit test source

The full JUnit 4 test class is reproduced below for the portfolio; the runnable copy lives at `src/test/java/org/jhotdraw/undo/UndoRedoManagerTest.java`.

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
- The special `DISCARD_ALL_EDITS` edit reports false for `canUndo()`/`canRedo()`, so the manager offers neither.
- Multiple edits are undone in last-in-first-out order, and undo becomes unavailable once the stack is exhausted.
