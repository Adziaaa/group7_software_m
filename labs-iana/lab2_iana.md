# JHotDraw — User Stories

## 1. Undo / Redo

**As a** user editing a drawing, **I want** to undo and redo my recent actions **so that** I can recover from mistakes without starting over.

**Acceptance criteria**
- `Ctrl+Z` reverts the last action.
- `Ctrl+Y` (or `Ctrl+Shift+Z`) reapplies the reverted action.
- Multiple actions can be undone/redone in sequence.
- The Edit menu shows Undo/Redo with the action name when available.

# Concept Location — Undo / Redo 

**Feature / Change Request:** Undo / Redo
**Method:** IDE debugger concept location, starting from the Edit-menu controller class and stepping through the runtime call stack.

## Runtime call flow

`UndoAction` (menu hook) → `UndoRedoManager.UndoAction` (real action registered under `"edit.undo"`) → `UndoRedoManager.undo()` → `javax.swing.undo.UndoManager` → `UndoableEdit` objects → `Drawing` / `Figure`

## Initial set of classes

| Domain Class | Responsibility |
|---|---|
| `UndoAction` (`org.jhotdraw.action.edit`) | Application-level Edit-menu hook. Does not perform undo itself; delegates to the real undo action registered in the active view's action map under the ID `"edit.undo"`. |
| `AbstractViewAction` | Superclass of `UndoAction`; provides access to the active `View` and the `Application`. |
| `View` (`org.jhotdraw.api.app`) | Holds the action map containing the view-specific undo action; represents the surface being edited. |
| `UndoRedoManager` (`org.jhotdraw.undo`) | Extends `javax.swing.undo.UndoManager`. Maintains the undo/redo edit stacks, tracks whether significant edits exist, blocks incoming edits while an undo/redo is in progress, and exposes the real undo/redo `Action` objects to the menu. |
| `UndoRedoManager.UndoAction` (inner) | The real undo `Action` registered under `"edit.undo"`. Its `actionPerformed` calls `UndoRedoManager.undo()`. |
| `UndoRedoManager.RedoAction` (inner) | The real redo `Action` registered under `"edit.redo"`. Its `actionPerformed` calls `UndoRedoManager.redo()`. |
| `UndoableEdit` (`javax.swing.undo`) | Represents a single reversible edit; defines `undo()` / `redo()`. The manager iterates these to apply or reverse changes. |
| `Drawing` | Model container of figures; the state that edits modify and restore during undo/redo. |
| `Figure` | The drawing element whose geometry/attributes an edit changes and then reverts. |
