# Refactoring — Undo / Redo

**Feature:** Undo / Redo
**Class under analysis:** `org.jhotdraw.undo.UndoRedoManager`

## Identified code smell

A SonarLint analysis of `UndoRedoManager` reveals a clear instance of the Duplicated Code smell, occurring in three distinct locations within the class.

The first instance is found in the two inner action classes. `UndoAction` and `RedoAction` are structurally near-identical: both define a constructor that configures a label and disables the action, and both implement an `actionPerformed` method that wraps a single call within a try/catch block. The only substantive differences are the label key (`"edit.undo"` versus `"edit.redo"`) and the method invoked (`undo()` versus `redo()`).

The second instance appears in the `updateActions()` method, which contains two mirror-image if/else blocks. One block configures the undo action and the other configures the redo action, but both perform the same operation, differing only in the action referenced and the condition checked (`canUndo()` versus `canRedo()`).

The third instance occurs across the `undo()`, `redo()`, and `undoOrRedo()` methods, each of which repeats the same guard logic: setting `undoOrRedoInProgress` to true, invoking the corresponding `super` method within a try block, and resetting the flag while calling `updateActions()` in the finally block. This wrapper is duplicated three times.

## Objective of the refactoring

The objective is to consolidate the repeated logic into a single location while preserving the external behavior of the feature. The public methods and the observable menu behavior must remain unchanged before and after the refactoring, in keeping with the behavior-preserving definition of refactoring.

## Proposed strategy

The refactoring would proceed as a sequence of small, behavior-preserving transformations, with compilation and test verification performed after each step to ensure correctness and allow safe reversion.

The first step addresses the guard logic, as it carries the lowest risk. The repeated set-flag / try / `super` / finally / `updateActions()` pattern would be extracted into a single private helper method (for example, `runTracked(...)`), which `undo()`, `redo()`, and `undoOrRedo()` would then invoke. This eliminates three copies of the same wrapper.

The second step targets `updateActions()`. Because the undo and redo blocks perform equivalent work, a single helper method would be extracted, parameterized by the action, its availability, and its labels, and invoked twice in place of the two near-identical inline blocks.

The third step is treated as a candidate rather than a definite transformation. The two inner action classes could be merged into a single parameterized action or unified under a common abstract parent via Form Template Method, since they differ only in a label and the operation invoked. As this change is more invasive, it would be applied only if it could be kept fully behavior-preserving.

## Applied refactorings and rationale

The principal refactoring is Extract Method, applied twice: once to the undo/redo guard logic and once to the duplicated blocks within `updateActions()`. This refactoring is selected because it represents a minimal, low-risk transformation that directly resolves the Duplicated Code smell by providing the repeated logic with a single definition.

The remaining candidate is Form Template Method, applied to the `UndoAction` and `RedoAction` pair. The rationale is consistent: the two classes are structurally identical, so unifying them would remove the final instance of duplication.

## Justification

The purpose of these refactorings is not to introduce new functionality but to establish a single source of truth for the undo/redo logic, thereby reducing maintenance cost and the likelihood of inconsistent modifications, while leaving the external behavior of the feature unchanged.

---

## Improved implementation

The following excerpts show the proposed transformations applied. Only the affected members of `UndoRedoManager` are shown; the remainder of the class is unchanged.

### Step 1 — Extract Method on the undo/redo guard

**Before**

```java
@Override
public void undo() throws CannotUndoException {
    undoOrRedoInProgress = true;
    try {
        super.undo();
    } finally {
        undoOrRedoInProgress = false;
        updateActions();
    }
}

@Override
public void redo() throws CannotUndoException {
    undoOrRedoInProgress = true;
    try {
        super.redo();
    } finally {
        undoOrRedoInProgress = false;
        updateActions();
    }
}

@Override
public void undoOrRedo() throws CannotUndoException, CannotRedoException {
    undoOrRedoInProgress = true;
    try {
        super.undoOrRedo();
    } finally {
        undoOrRedoInProgress = false;
        updateActions();
    }
}
```

**After**

```java
/**
 * Runs an undo/redo operation while suppressing incoming UndoableEdit
 * events, and refreshes the action state afterwards. Centralises the
 * guard logic previously duplicated in undo(), redo() and undoOrRedo().
 */
private void runTracked(Runnable operation) {
    undoOrRedoInProgress = true;
    try {
        operation.run();
    } finally {
        undoOrRedoInProgress = false;
        updateActions();
    }
}

@Override
public void undo() throws CannotUndoException {
    runTracked(super::undo);
}

@Override
public void redo() throws CannotUndoException {
    runTracked(super::redo);
}

@Override
public void undoOrRedo() throws CannotUndoException, CannotRedoException {
    runTracked(super::undoOrRedo);
}
```

The three methods now express only their intent (which super operation to run), and the shared tracking and refresh logic exists in exactly one place. The external behavior — suppressing edits during the operation and updating the actions afterwards — is preserved.

### Step 2 — Extract Method on `updateActions()`

**Before**

```java
private void updateActions() {
    String label;
    if (canUndo()) {
        undoAction.setEnabled(true);
        label = getUndoPresentationName();
    } else {
        undoAction.setEnabled(false);
        label = labels.getString("edit.undo.text");
    }
    undoAction.putValue(Action.NAME, label);
    undoAction.putValue(Action.SHORT_DESCRIPTION, label);

    if (canRedo()) {
        redoAction.setEnabled(true);
        label = getRedoPresentationName();
    } else {
        redoAction.setEnabled(false);
        label = labels.getString("edit.redo.text");
    }
    redoAction.putValue(Action.NAME, label);
    redoAction.putValue(Action.SHORT_DESCRIPTION, label);
}
```

**After**

```java
private void updateActions() {
    configureActionState(undoAction, canUndo(),
            getUndoPresentationName(), "edit.undo.text");
    configureActionState(redoAction, canRedo(),
            getRedoPresentationName(), "edit.redo.text");
}

/**
 * Enables or disables the given action and sets its name and short
 * description. Centralises the logic previously duplicated for the
 * undo and redo actions.
 *
 * @param action           the action to configure
 * @param available        whether the operation is currently possible
 * @param presentationName the label to use when the operation is available
 * @param disabledLabelKey the resource key for the label when disabled
 */
private void configureActionState(Action action, boolean available,
        String presentationName, String disabledLabelKey) {
    String label = available ? presentationName : labels.getString(disabledLabelKey);
    action.setEnabled(available);
    action.putValue(Action.NAME, label);
    action.putValue(Action.SHORT_DESCRIPTION, label);
}
```

The two mirror-image blocks are replaced by two calls to a single helper. The labelling and enable/disable behavior is identical to the original.

### Step 3 (candidate) — Form Template Method on the inner actions

**Before**

```java
private class UndoAction extends AbstractAction {
    public UndoAction() {
        labels.configureAction(this, "edit.undo");
        setEnabled(false);
    }
    @Override
    public void actionPerformed(ActionEvent evt) {
        try {
            undo();
        } catch (CannotUndoException e) {
            System.err.println("Cannot undo: " + e);
            e.printStackTrace();
        }
    }
}

private class RedoAction extends AbstractAction {
    public RedoAction() {
        labels.configureAction(this, "edit.redo");
        setEnabled(false);
    }
    @Override
    public void actionPerformed(ActionEvent evt) {
        try {
            redo();
        } catch (CannotRedoException e) {
            System.out.println("Cannot redo: " + e);
        }
    }
}
```

**After**

```java
/**
 * A single parameterised action for both undo and redo. The label key
 * and the operation to run are supplied at construction, removing the
 * duplication between the former UndoAction and RedoAction classes.
 */
private class UndoRedoAction extends AbstractAction {
    private final Runnable operation;

    UndoRedoAction(String labelKey, Runnable operation) {
        this.operation = operation;
        labels.configureAction(this, labelKey);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        try {
            operation.run();
        } catch (CannotUndoException | CannotRedoException e) {
            System.err.println("Cannot perform operation: " + e);
        }
    }
}
```

with construction becoming:

```java
undoAction = new UndoRedoAction("edit.undo", this::undo);
redoAction = new UndoRedoAction("edit.redo", this::redo);
```

This removes the last instance of duplication. It is marked as a candidate because it changes the field types (`undoAction`/`redoAction` become `UndoRedoAction`/`Action`) and merges the two exception-handling paths; it should only be adopted once the existing tests confirm the behavior is unchanged.

> Note: these excerpts assume a Java 8 (or later) source level, which is required for the lambda and method-reference syntax used in `runTracked` and in the action construction. This is consistent with the project's build configuration.
