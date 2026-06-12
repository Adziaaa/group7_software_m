# Impact Analysis Lab — Portfolio Work

**Feature analyzed:** Arrange (Send to Front / Send to Back)

**Change request basis:** the concept was located in `BringToFrontAction` / `SendToBackAction`; the table below lists every package visited while tracing the feature outward from that located concept.

## Table 1: Packages visited during impact analysis

| Package name | # of classes | Comments |
| ------------ | ------------ | -------- |
| `org.jhotdraw.draw.action` | 3 | The command/action layer and the entry point of the feature. `BringToFrontAction` and `SendToBackAction` hold the actual arrange logic, and `AbstractSelectedAction` is their shared base that binds them to the current selection and toggles their enabled state. The feature lives in two small, near-mirror-image classes on a common selection-aware base — and this is the only package that is actually modified: just `BringToFrontAction` and `SendToBackAction` are changed, while `AbstractSelectedAction` stays untouched as their base. |
| `org.jhotdraw.draw` | 6 | The core model and editor/view abstractions: `Drawing`, `QuadTreeDrawing`, `DrawingEditor`, `DefaultDrawingEditor`, `DrawingView`, `DefaultDrawingView`. The actions read the selected figures from the view, reach the active view through the editor, and delegate the real work to the model — `QuadTreeDrawing.bringToFront()/sendToBack()`. The action only orchestrates and the model owns the real z-order reordering — but because my change is a behaviour-preserving refactoring, these classes are only *read* (they guide me to the logic); none of them is modified. |
| `org.jhotdraw.draw.figure` | 1 | Defines `Figure`, the graphical object whose position in the stacking order is moved. Arrange operates on `Figure` references but does not modify the figure classes themselves — they are passive participants, so there is no impact here (interface usage only). |
| `org.jhotdraw.samples.svg.gui` | 1 | Application-specific UI. `ArrangeToolBar` wires the two actions into clickable toolbar buttons in the SVG editor. This is where the feature is exposed to the user; it constructs the actions but, because their public constructor and `ID` are preserved, it needs no change. |
| `javax.swing.undo` | 1 | Java Swing's standard undo framework. `AbstractUndoableEdit` is subclassed inline by the actions to make the operation reversible. Undo/redo for arrange is not custom code — it rides on the standard library, so any change to the feature must keep its undoable edit consistent. |

**Total classes visited:** 12 across 5 packages. Of these, only **2 are actually changed** (`BringToFrontAction`, `SendToBackAction`); the other 10 are visited but unchanged.

## Summary of what I learned

The arrange feature is cleanly layered: a thin **action** layer (`org.jhotdraw.draw.action`) captures the user intent and the selection, the **model** layer (`org.jhotdraw.draw`) performs the real stacking-order change, the **figure** layer is only a passive data participant, the **sample GUI** layer exposes the buttons, and **undo** support is borrowed from the Swing library. Because my change is a behaviour-preserving refactoring of the two action classes, the **estimated impact set is concentrated entirely in the action package** — the model, figure, GUI and undo packages are *visited* (they guide me to the logic) but **not changed**; they are used through their existing interfaces or simply read.