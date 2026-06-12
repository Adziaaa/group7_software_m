| Domain Class | Responsibility |
| --- | --- |
| CutAction | Cuts the selected region and places its contents into the system clipboard. Removes the selected element(s) from the canvas. Extends AbstractSelectionAction. |
| CopyAction | Copies the selected region and places its contents into the system clipboard without removing them from the canvas. Extends AbstractSelectionAction. |
| PasteAction | Pastes the contents of the system clipboard at the current position on the canvas. Extends AbstractSelectionAction. |
| DeleteAction | Deletes the selected element(s) permanently from the canvas without storing them in the clipboard. Extends TextAction and uses EditableComponent. |
| DuplicateAction | Duplicates the selected region and places the copy directly onto the canvas. Extends AbstractSelectionAction. |
| AbstractSelectionAction | Abstract base class for all selection-dependent actions. Manages the target component and enables/disables actions based on whether a selection exists. Parent of CutAction, CopyAction, PasteAction, and DuplicateAction. |
| EditableComponent | Interface that defines the contract for any component that supports editing operations. Declares methods for cut, copy, paste, delete, and duplicate. Used by DeleteAction and DuplicateAction. |
| ClipboardUtil | Utility class that provides access to the system clipboard. Used by CutAction, CopyAction, and PasteAction to read from and write clipboard content. |