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

The existing code already exhibits several SOLID properties (as described above). The following excerpts show how the implementation can be strengthened so that the principles are expressed more explicitly. Only the affected members are shown; the remainder of the class is unchanged.

### Strengthening DIP and OCP — depending on an abstraction, open for extension

In the original `UndoAction`, the menu hook resolves the real action directly from the view's action map by a hard-coded string key:

**Before**

```java
@Override
public void actionPerformed(ActionEvent e) {
    Action realUndoAction = getRealUndoAction();
    if (realUndoAction != null && realUndoAction != this) {
        realUndoAction.actionPerformed(e);
    }
}

private Action getRealUndoAction() {
    return (getActiveView() == null)
            ? null
            : getActiveView().getActionMap().get(ID);
}
```

**After**

```java
@Override
public void actionPerformed(ActionEvent e) {
    resolveDelegate().ifPresent(action -> action.actionPerformed(e));
}

/**
 * Resolves the concrete undo action registered by the active view, if any.
 * The menu hook depends only on the Action abstraction and on the View
 * contract; it has no knowledge of which concrete class performs the undo
 * (Dependency Inversion). New view implementations may register their own
 * undo action under the same ID without modifying this class (Open/Closed).
 */
private Optional<Action> resolveDelegate() {
    if (getActiveView() == null) {
        return Optional.empty();
    }
    Action delegate = getActiveView().getActionMap().get(ID);
    return (delegate == null || delegate == this)
            ? Optional.empty()
            : Optional.of(delegate);
}
```

The dependency on the concrete undo implementation is still expressed only through the `Action` abstraction and the `View` contract, but the resolution is now isolated in one named method that documents the DIP/OCP intent and removes the repeated null/self checks.

### Strengthening SRP — separating the tracking concern

The `undo()`, `redo()`, and `undoOrRedo()` methods in `UndoRedoManager` each mix two responsibilities: performing the operation and managing the in-progress tracking flag. Extracting the tracking concern gives each method a single reason to change.

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
 * Performs an undo/redo operation while suppressing incoming edits and
 * refreshing the action state. The state-tracking responsibility lives
 * here alone, so the public operations are responsible only for selecting
 * which super operation to run (Single Responsibility).
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

### Strengthening ISP and LSP — programming to the narrow edit contract

The manager operates on edits only through the narrow `UndoableEdit` interface, which exposes just the operations a reversible edit requires. Any conforming edit can be substituted without affecting the manager, satisfying both Interface Segregation (clients depend only on the methods they use) and Liskov Substitution (any `UndoableEdit` behaves correctly in place of another).

```java
/**
 * The manager depends only on the UndoableEdit abstraction. It never
 * refers to a concrete edit type, so any implementation — a figure edit,
 * a composite edit, or a test double — can be added and undone/redone
 * interchangeably (Interface Segregation, Liskov Substitution).
 */
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

> Note: these excerpts assume a Java 8 (or later) source level for the lambda, method-reference, and `Optional` usage, consistent with the project's build configuration.

## How the principles map to the implementation

| Principle | Expressed by |
|---|---|
| Single Responsibility | `runTracked(...)` isolates the tracking concern; each public operation only selects its super call. |
| Open/Closed | `UndoRedoManager` extends `UndoManager`; new views register an undo action under the same `ID` without editing `UndoAction`. |
| Liskov Substitution | Overrides honor the `UndoManager` contract; any `UndoableEdit` is interchangeable. |
| Interface Segregation | Dependence on the narrow `UndoableEdit` and `Action` contracts, not large general-purpose types. |
| Dependency Inversion | `UndoAction` resolves its delegate through the `Action`/`View` abstractions, never a concrete manager. |
