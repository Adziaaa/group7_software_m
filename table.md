| # | Domain Class | Tool Used | Comments |
| --- | --- | --- | --- |
| 1 | EditableComponent | IDE — Find Usages | Interface that defines the contract for cut, copy, paste, delete, and duplicate. Starting point for understanding what operations are expected from any editable component. |
| 2 | AbstractSelectionAction | IDE — Find Usages / Source reading | Abstract base class for all selection-dependent actions. Manages the target component and enables/disables actions based on whether a selection exists. Parent of CutAction, CopyAction, PasteAction, and DuplicateAction. |
| 3 | CutAction | IDE — Navigate to class | Cuts the selected element(s) from the canvas and places them into the system clipboard via ClipboardUtil. Extends AbstractSelectionAction. |
| 4 | CopyAction | IDE — Navigate to class | Copies the selected element(s) into the system clipboard without removing them from the canvas. Extends AbstractSelectionAction. |
| 5 | PasteAction | IDE — Navigate to class | Pastes clipboard contents onto the canvas at the current position. Extends AbstractSelectionAction. |
| 6 | DeleteAction | IDE — Navigate to class | Permanently removes the selected element(s) without storing them in the clipboard. Extends TextAction, uses EditableComponent directly. |
| 7 | DuplicateAction | IDE — Navigate to class | Creates a copy of the selected element(s) and places it on the canvas. Extends AbstractSelectionAction. |
| 8 | ClipboardUtil | IDE — Find Usages from CutAction | Utility class providing access to the system clipboard. Shared by CutAction, CopyAction, and PasteAction. |

T1