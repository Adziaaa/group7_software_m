# Actualization — Undo / Redo

**Feature:** Undo / Redo
**Case study classes:** `org.jhotdraw.action.edit.UndoAction`, `org.jhotdraw.undo.UndoRedoManager`

## SOLID principles in the context of the case study

### Single Responsibility Principle (SRP)
Each class in the feature has one reason to change. `UndoAction` (in `org.jhotdraw.action.edit`) is responsible only for being the Edit-menu entry point: it locates the real action and delegates to it. `UndoRedoManager` (in `org.jhotdraw.undo`) is responsible only for managing the undo/redo edit stacks and the state of the corresponding actions. The presentation concern (menu hook) and the management concern (edit stacks) are kept in separate classes, so a change to the menu wiring does not affect the stack logic, and vice versa.

### Open/Closed Principle (OCP)
`UndoRedoManager` extends `javax.swing.undo.UndoManager`, adding action support (`getUndoAction()`, `getRedoAction()`) and the in-progress guard without modifying the JDK base class. The behavior of the framework is extended through subclassing rather than by editing existing, tested code, which is the essence of being open for extension but closed for modification.

### Liskov Substitution Principle (LSP)
`UndoRedoManager` overrides `undo()`, `redo()`, `undoOrRedo()`, `addEdit()`, and `discardAllEdits()` but continues to honor the contract of `UndoManager` — it still performs the undo/redo operations expected of a manager, only adding the tracking flag and action updates around them. An instance of `UndoRedoManager` can therefore be used wherever an `UndoManager` is expected without breaking client expectations.

### Interface Segregation Principle (ISP)
The feature depends on small, focused abstractions rather than large general-purpose ones. `UndoAction` works through the `View` and `Application` interfaces (in `org.jhotdraw.api.app`) and the standard `javax.swing.Action` contract, and the edits handled by the manager implement the narrow `UndoableEdit` interface, which exposes only the operations a reversible edit needs (`undo()`, `redo()`, `canUndo()`, `canRedo()`). Clients are not forced to depend on methods they do not use.

### Dependency Inversion Principle (DIP)
`UndoAction` does not depend on the concrete `UndoRedoManager`. It retrieves the real action from the active view's action map by the abstract key `"edit.undo"` and invokes it through the `Action` interface. The high-level menu component therefore depends on an abstraction, not on the concrete implementation that performs the undo, allowing the implementation behind the action map to vary independently.

## Clean Architecture in the context of the case study

Clean Architecture organizes a system into concentric layers, with dependencies pointing inward toward the domain and away from frameworks and UI. The Undo/Redo feature maps onto this structure:

- **Frameworks / UI layer (outermost):** the Swing menu items and the `UndoAction` / `RedoAction` classes in `org.jhotdraw.action.edit`. These are the delivery mechanism — how the user triggers the feature — and are the most volatile, framework-dependent part.
- **Interface adapters / application layer:** `UndoRedoManager` and its inner actions in `org.jhotdraw.undo`. This layer coordinates the use case (perform an undo or a redo), translating a user trigger into operations on the edit stacks and keeping the action state consistent.
- **Domain / entities layer (innermost):** the `UndoableEdit` objects and the model they modify (the drawing and its figures). These represent the core, behavior-defining state and are the least dependent on any framework.

The dependency direction is respected: the outer `UndoAction` depends inward on abstractions (the `Action` contract resolved through the `View`'s action map) rather than the inner layers depending outward on the menu. Because the menu hook is decoupled from the manager via the action map, the volatile UI layer can change without forcing changes to the stable inner logic — which is precisely the separation Clean Architecture aims to achieve.

---

## Improved implementation

The refactoring chosen for this feature targets the **Duplicated Code** smell in `UndoRedoManager` and consolidates it through Extract Method and a parameterised inner action. The excerpts below show the changes that were actually applied, each read through the Clean Architecture lens — that is, in terms of *which layer the change touches* and *how it affects the inward direction of dependencies*. All three changes are confined to the interface-adapters / application layer (`UndoRedoManager`); none alters the domain entities (`UndoableEdit` and the model) or the outer UI layer, so the layering and the inward dependency rule are preserved by construction. Only the affected members are shown; the remainder of the class is unchanged.

### Change 1 — Extracting the tracking concern (`runTracked`)

The `undo()`, `redo()`, and `undoOrRedo()` methods each repeated the same guard: set the in-progress flag, run the corresponding `super` operation in a `try`, then clear the flag and refresh the actions in a `finally`. The shared logic was extracted into one private helper.

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
// redo() and undoOrRedo() repeat the same structure
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

In Clean Architecture terms, this change is wholly internal to the application layer. The use case the manager coordinates — "perform an undo/redo while the model is guarded against re-entrant edits" — is unchanged; only its implementation is now expressed once. Because `runTracked` is private, the change is invisible to both the outer UI layer and the inner domain, so no dependency crosses a layer boundary as a result of it.

### Change 2 — Extracting the action-state update (`configureActionState`)

`updateActions()` previously contained two mirror-image blocks, one per action, each enabling/disabling the action and setting its presentation name. The shared work was extracted into a parameterised helper.

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
    // the redo block repeats the same structure
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
 */
private void configureActionState(Action action, boolean available,
        String presentationName, String disabledLabelKey) {
    String label = available ? presentationName : labels.getString(disabledLabelKey);
    action.setEnabled(available);
    action.putValue(Action.NAME, label);
    action.putValue(Action.SHORT_DESCRIPTION, label);
}
```

This is the point where the application layer talks *outward* to the delivery mechanism, and the refactoring keeps that conversation correctly directed at an abstraction. The helper is typed against the framework's `javax.swing.Action` interface, not against the concrete inner action class, so the manager updates presentation state without depending on a concrete UI widget. The outward reference remains to a stable contract (`Action`), consistent with Clean Architecture's rule that any outward reference should be to an abstraction.

### Change 3 — Unifying the two inner actions (`UndoRedoAction`)

The near-identical `UndoAction` and `RedoAction` inner classes were collapsed into a single class parameterised by a label key and a `Runnable`.

**Before**

```java
private class UndoAction extends AbstractAction {
    public UndoAction() {
        labels.configureAction(this, "edit.undo");
        setEnabled(false);
    }
    @Override
    public void actionPerformed(ActionEvent evt) {
        try { undo(); }
        catch (CannotUndoException e) { /* log */ }
    }
}
// RedoAction is structurally identical, differing only in the label
// key ("edit.redo") and the operation invoked (redo()).
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

These inner actions are the application layer's adapter to the Swing UI. The unified `UndoRedoAction` receives its behaviour as a `Runnable` injected at construction, so the action object no longer hard-codes *which* operation it triggers; the dependency on the specific undo-vs-redo behaviour is supplied from outside the action rather than baked into a dedicated class. The action therefore depends only on the `Runnable` abstraction, keeping the adapter thin and pointing its dependency inward at the operation it is given.

### Why the domain layer is untouched

The innermost layer — the `UndoableEdit` objects and the model they modify — is deliberately absent from the changes above. The manager continues to operate on edits only through the narrow `UndoableEdit` contract (`undo()`, `redo()`, `canUndo()`, `canRedo()`), exactly as before:

```java
@Override
public boolean addEdit(UndoableEdit anEdit) {
    if (undoOrRedoInProgress) {
        anEdit.die();
        return true;
    }
    boolean success = super.addEdit(anEdit);
    updateActions();
    if (success && anEdit.isSignificant() && editToBeUndone() == anEdit) {
        setHasSignificantEdits(true);
    }
    return success;
}
```

That this method needed no change is the point: a refactoring confined to the application layer left the domain contract — and therefore the inward dependency on it — completely stable. This is the separation Clean Architecture aims for, where churn in an outer or middle layer does not ripple into the core.

## How the refactoring maps to the architecture

| Change | Layer affected | Clean Architecture effect |
|---|---|---|
| `runTracked(...)` extraction | Application (`UndoRedoManager`) | Use case implementation consolidated; private, so no layer boundary is crossed. |
| `configureActionState(...)` extraction | Application → UI boundary | Presentation state updated through the `Action` abstraction, not a concrete widget. |
| `UndoRedoAction` unification | Application adapter to Swing | Behaviour injected as a `Runnable`; the adapter depends on an abstraction, not a fixed operation. |
| `addEdit(...)` (unchanged) | Application ↔ Domain boundary | Domain `UndoableEdit` contract left stable; inward dependency unaffected by the refactoring. |
