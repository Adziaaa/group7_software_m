| Package name | # of classes | Tool used | Comments |
| --- | --- | --- | --- |
| org.jhotdraw.action.edit | 8 | IDE — Navigate to class | Main package for the feature. Contains all editing actions found during concept location: CutAction, CopyAction, PasteAction, DeleteAction, DuplicateAction, and AbstractSelectionAction. Changed. |
| org.jhotdraw.datatransfer | 9 | IDE — Find Usages from CutAction | Found when tracing ClipboardUtil usage from CutAction. Handles clipboard operations shared by cut, copy, and paste. Not modified but directly tied to the feature. Propagating. |
| org.jhotdraw.api.gui | 1 | IDE — Find Usages | Found when following EditableComponent — the interface that DeleteAction uses directly. Led to identifying the full action class set but doesn't need to be changed. Propagating. |
| org.jhotdraw.draw | 6 | IDE — Call Hierarchy | Visited to understand how actions interact with selected figures on the canvas via DrawingEditor and DrawingView. No changes needed here. Unchanged. |

**Portfolio Work:** Use Table 1 to list the packages and the number of classes you visited after you
located the concept. Write short comments explaining what you have learned about each package
and how they contribute to your feature?