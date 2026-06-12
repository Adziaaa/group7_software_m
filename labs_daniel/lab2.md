## Initial Set of Classes — Concept Location

| Domain Class | Responsibility |
|---|---|
| `AlignAction` | Abstract base class for all alignment actions. Manages undo wrapping via `CompositeEdit` and defines the `alignFigures()` contract that each direction implements. |
| `AlignAction.North` | Aligns all selected figures to the topmost edge of the selection bounds by translating each figure on the Y axis. |
| `AlignAction.South` | Aligns all selected figures to the bottommost edge of the selection bounds by translating each figure on the Y axis. |
| `AlignAction.East` | Aligns all selected figures to the rightmost edge of the selection bounds by translating each figure on the X axis. |
| `AlignAction.West` | Aligns all selected figures to the leftmost edge of the selection bounds by translating each figure on the X axis. |
| `AlignAction.Horizontal` | Centers all selected figures on the horizontal (X) center axis of the selection bounds. |
| `AlignAction.Vertical` | Centers all selected figures on the vertical (Y) center axis of the selection bounds. |
| `AbstractSelectedAction` | Parent class providing access to the active `DrawingView`, selected figures, and the `fireUndoableEditHappened()` mechanism. |
| `AlignToolBar` | Swing toolbar that instantiates each `AlignAction` subclass and binds it to a `JButton` in the SVG editor palette. |
| `DrawingEditor` | Interface providing access to the active view and selection. Passed into every `AlignAction` constructor. |