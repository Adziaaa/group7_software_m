# Impact Analysis Lab — Portfolio Work

**Feature analyzed:** Arrange (Send to Front / Send to Back)

**Change request basis:** the concept was located in `BringToFrontAction` / `SendToBackAction`; the table below lists every package visited while tracing the feature outward from that located concept.

## Table 1: Packages visited during impact analysis

| Package name | # of classes | Comments |
| ------------ | ------------ | -------- |
| `org.jhotdraw.draw.action` | 3 | The command/action layer and the entry point of the feature. `BringToFrontAction` and `SendToBackAction` hold the actual arrange logic, and `AbstractSelectedAction` is their shared base that binds them to the current selection and toggles their enabled state. I learned the feature lives in two small, near-mirror-image classes on a common selection-aware base — this is where any change to arrange would start, and where most of the impact concentrates. |
| `org.jhotdraw.draw` | 6 | The core model and editor/view abstractions: `Drawing`, `QuadTreeDrawing`, `DrawingEditor`, `DefaultDrawingEditor`, `DrawingView`, `DefaultDrawingView`. The actions read the selected figures from the view, reach the active view through the editor, and delegate the real work to the model — `QuadTreeDrawing.bringToFront()/sendToBack()`. I learned that the action only orchestrates; the actual z-order reordering is owned by the model, so a behavioral change to stacking order propagates into this package. |
| `org.jhotdraw.draw.figure` | 1 | Defines `Figure`, the graphical object whose position in the stacking order is moved. I learned that arrange operates on `Figure` references but does not modify the figure classes themselves — they are passive participants, so the impact here is low (interface usage only). |
| `org.jhotdraw.samples.svg.gui` | 1 | Application-specific UI. `ArrangeToolBar` wires the two actions into clickable toolbar buttons in the SVG editor. I learned this is where the feature is exposed to the user; changes to presentation, icons, or labels touch this package, but not the core logic. |
| `javax.swing.undo` | 1 | Java Swing's standard undo framework. `AbstractUndoableEdit` is subclassed inline by each action to make the operation reversible. I learned that undo/redo for arrange is not custom code — it rides on the standard library, so any change to the feature must keep its undoable edit consistent. |

**Total classes visited:** 12 across 5 packages.

## Summary of what I learned

The arrange feature is cleanly layered: a thin **action** layer (`org.jhotdraw.draw.action`) captures the user intent and the selection, the **model** layer (`org.jhotdraw.draw`) performs the real stacking-order change, the **figure** layer is only a passive data participant, the **sample GUI** layer exposes the buttons, and **undo** support is borrowed from the Swing library. The estimated impact set is therefore concentrated in the action and model packages; the figure, GUI, and undo packages are touched only lightly (interface use or standard-library subclassing).