# Lab - Testing

**Feature (CASE study):** Arrange - *Send to Front* / *Send to Back*
**Unit under test:** `org.jhotdraw.draw.action.BringToFrontAction` and `org.jhotdraw.draw.action.SendToBackAction`
**Test class:** `jhotdraw-core/src/test/java/org/jhotdraw/draw/action/ArrangeActionTest.java`
**Frameworks:** JUnit 4 + Mockito

---

## 1. What business logic was tested

The important business functionality of the arrange feature is the **z-order reordering** and its **undo/redo behaviour**:

- `BringToFrontAction.bringToFront(view, figures)` - moves each selected figure to the front, processing them in the drawing's sorted order so the relative stacking order of the selection is preserved.
- `SendToBackAction.sendToBack(view, figures)` - moves each selected figure to the back.
- `AbstractArrangeAction.actionPerformed(...)` - the template method that applies the operation and registers a reversible `UndoableEdit` (forward = redo, inverse = undo).

## 2. Isolating the dependency (mocks)

A unit test should follow a single code-path through a single method. When `bringToFront`/`sendToBack` execute, control leaves the method and enters the `Drawing` model, which performs the actual reordering - that is a **dependency**. Following the lab guidance, the `Drawing` (plus `DrawingView` and `DrawingEditor`) are replaced with **Mockito mocks**. The tests then verify the *interaction* with the `Drawing` abstraction (`verify(drawing).bringToFront(f1)`), instead of relying on a concrete drawing implementation. This keeps each test fast, deterministic, and focused on one unit.

## 3. Test cases

**Best-case scenarios**

| Test | Verifies |
| ---- | -------- |
| `bringToFront_movesEverySelectedFigureToFront` | Each selected figure is sent to the front. |
| `bringToFront_appliesFiguresInSortedZOrder` | Figures are processed in the drawing's **sorted** order (relative order preserved) - verified with Mockito `InOrder`. |
| `sendToBack_movesEverySelectedFigureToBack` | Each selected figure is sent to the back, and no pre-sort is performed (the behavioural difference from bring-to-front). |
| `bringToFrontAction_firesUndoableEdit_andUndoSendsBack` | The action applies the forward direction, registers a non-null `UndoableEdit`, `undo()` reverses it (send to back), and `redo()` re-applies it. |
| `sendToBackAction_firesUndoableEdit_andUndoBringsToFront` | Symmetric undo behaviour for send-to-back. |

**Boundary cases**

| Test | Verifies |
| ---- | -------- |
| `bringToFront_emptySelection_doesNothing` | With no figures selected, the drawing is never modified. |
| `sendToBack_emptySelection_doesNothing` | Same for send-to-back. |
| `bringToFront_singleFigure_movesOnlyThatFigure` | A single-figure selection moves exactly that figure and no other. |

## 4. Java assertion for an invariant

Per the lab, a Java `assert` is used to check a condition that *should never happen*: inside `bringToFrontAction_firesUndoableEdit_andUndoSendsBack`, after capturing the registered edit:

```java
assert edit != null : "arrange action must register an undoable edit";
```

An arrange action must always register an undoable edit; if this is ever null, the undo/redo contract is broken and the program is in an invalid state, so an assertion (which halts execution) is the correct tool here - as opposed to an exception, which would let the program continue. Maven Surefire runs tests with assertions enabled by default, so this invariant is checked on every test run.

## 5. How the tests verify the feature

Together the eight tests establish the full behavioural contract of the arrange feature, so a passing run is evidence that the feature does what its specification promises.

The **forward behaviour** is verified by asserting the exact effect each operation must have on the model: `bringToFront_movesEverySelectedFigureToFront` confirms that *every* selected figure is pushed to the front, and `sendToBack_movesEverySelectedFigureToBack` does the same for the back. Because the `Drawing` is mocked, these tests check the precise interaction (`bringToFront`/`sendToBack` is invoked once per selected figure) rather than an incidental side effect - if the action skipped a figure or touched the wrong one, the verification would fail.

The **ordering invariant** that distinguishes the two operations is verified by `bringToFront_appliesFiguresInSortedZOrder` and the `never().sort(...)` check in the send-to-back test. Bring-to-front must process figures in the drawing's sorted order so the selection keeps its relative stacking, whereas send-to-back must not sort. Using Mockito `InOrder`, the test fails if the figures are reordered incorrectly - this guards the subtle correctness property that a user relies on when arranging overlapping shapes.

The **reversibility contract** - the part of the feature a user exercises through Undo/Redo - is verified by `bringToFrontAction_firesUndoableEdit_andUndoSendsBack` and its symmetric counterpart. These drive the real `actionPerformed` template method, capture the `UndoableEdit` it registers, and then confirm that `undo()` applies the *inverse* operation and `redo()` re-applies the original. This proves the feature is not only correct on first use but also fully undoable and redoable, which is a hard requirement for any editing action in the drawing editor.

The **boundary behaviour** is verified by the empty-selection and single-figure tests, which confirm the feature degrades safely: with nothing selected the drawing is never modified (no spurious edits, no exceptions), and with one figure exactly one figure moves. These cover the edge inputs that real users produce and that a naive implementation could mishandle.

Taken as a whole, the suite verifies the feature along every axis that matters - *what* it changes (forward effect), *in what order* (z-order invariant), *whether it can be taken back* (undo/redo), and *how it behaves at the edges* (boundary inputs) - at the class level, in isolation from the rest of the application.

## 6. Dependency / merge note

`jhotdraw-core/pom.xml` previously had only TestNG. JUnit 4 (`4.13.2`) and Mockito (`mockito-core 4.11.0`) were added as `test`-scoped dependencies. These are **byte-identical** to the dependencies added by the teammate who tested the *align* feature (commit `12f14fe`), and were inserted at the **same position** in the file. Because Git's three-way merge recognises identical changes on both branches, merging the two branches produces **no conflict and no duplicate** dependency entry. (Had the versions or placement differed, the merge would have required manual resolution in `pom.xml`.)
