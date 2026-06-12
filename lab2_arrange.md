# Concept Location — Arrange Feature (Send to Front / Send to Back)

| **Domain Class**          | **Responsibility**                                                                                                                                |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| **BringToFrontAction**    | Action triggered by the user that takes the currently selected figures and moves them to the front of the stacking order, and registers an undoable edit. |
| **SendToBackAction**      | Action that takes the selected figures and moves them to the back of the stacking order, and registers the corresponding undoable edit.            |
| **AbstractSelectedAction**| Abstract superclass of both arrange actions; tracks the active editor/view and enables or disables the action depending on the current selection.  |
| **ArrangeToolBar**        | Tool palette (UI) that exposes the "Bring to Front" and "Send to Back" actions as toolbar buttons.                                                 |
| **DrawingEditor**         | Coordinates the active tool and view, and provides the active drawing view on which the arrange action operates.                                   |
| **DefaultDrawingEditor**  | Concrete implementation of the drawing editor used at runtime.                                                                                     |
| **DrawingView**           | Displays the drawing and reports the set of currently selected figures to the action.                                                             |
| **DefaultDrawingView**    | Concrete implementation of the drawing view that manages the selection.                                                                            |
| **Drawing**               | Model interface that declares the z-ordering operations `bringToFront(Figure)`, `sendToBack(Figure)`, and `sort()`.                                |
| **QuadTreeDrawing**       | Concrete drawing model that stores the figures and performs the actual reordering of a figure within the stacking order.                           |
| **Figure**                | Abstract representation of the graphical object whose position in the stacking order is changed.                                                   |
| **AbstractUndoableEdit**  | Represents the undoable/redoable record of an arrange operation, enabling the action to be undone and redone.                                      |
