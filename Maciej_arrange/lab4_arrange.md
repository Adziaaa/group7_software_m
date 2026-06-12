# Refactoring Lab — Portfolio Work

**Feature / change request:** Arrange (Send to Front / Send to Back)
**Classes under refactoring:** `org.jhotdraw.draw.action.BringToFrontAction`, `org.jhotdraw.draw.action.SendToBackAction`

---

## 1. The code smell that triggered the refactoring

The dominant smell is **Duplicated Code** (Kerievsky, *Refactoring to Patterns*, Chapter 4). (A secondary observation is that the two classes' static z-order helpers, `bringToFront` and `sendToBack`, do the analogous job under different names; the two action classes themselves, however, share the same interface, so the core smell is Duplicated Code rather than *Alternative Classes with Different Interfaces*.)

`BringToFrontAction` and `SendToBackAction` are near mirror images. Both `actionPerformed` methods follow the exact same algorithm in the same order:

1. get the active `DrawingView`,
2. copy the selected figures into a `LinkedList`,
3. call a static helper that performs the reordering,
4. call `fireUndoableEditHappened(...)` with an anonymous `AbstractUndoableEdit` whose `getPresentationName()` returns a label and whose `redo()` / `undo()` re-apply the forward / reverse operation.

The only things that differ between the two classes are: the action `ID`/label, which helper is called for the forward direction, and which helper is called for the reverse direction. The undoable-edit boilerplate (roughly 25 lines) is copy-pasted in both classes.

Evidence from the current code:

```java
// BringToFrontAction.actionPerformed (abridged)
bringToFront(view, figures);
fireUndoableEditHappened(new AbstractUndoableEdit() {
    public String getPresentationName() { return labels.getTextProperty(ID); }
    public void redo() { BringToFrontAction.bringToFront(view, figures); }
    public void undo() { SendToBackAction.sendToBack(view, figures); }
});

// SendToBackAction.actionPerformed (abridged)
sendToBack(view, figures);
fireUndoableEditHappened(new AbstractUndoableEdit() {
    public String getPresentationName() { return labels.getTextProperty(ID); }
    public void redo() { SendToBackAction.sendToBack(view, figures); }
    public void undo() { BringToFrontAction.bringToFront(view, figures); }
});
```

The smell also shows as **cross-class static coupling**: each action's `undo()` reaches into the *other* class's static method, so the two classes are knotted together rather than sharing a clean abstraction.

Why this is a problem: any change to how an arrange operation is recorded for undo/redo (e.g. fixing presentation names, changing how the edit is grouped, adding a "bring forward by one step" variant) must be made in two places and kept in sync — exactly the maintenance risk Duplicated Code warns about.

---

## 2. What I plan to change

Capture the invariant algorithm — *select figures → apply a direction → register a reversible edit* — **once**, in a shared abstract base, and let each concrete action supply only the parts that vary (the forward direction, the reverse direction, and the label).

Concretely:

- Introduce an abstract base `AbstractArrangeAction extends AbstractSelectedAction`.
- Move the common `actionPerformed` skeleton and the undoable-edit creation into that base as a single **template method**.
- Define two abstract primitive operations the subclasses must implement: `arrange(view, figures)` (forward) and `reverseArrange(view, figures)` (the undo direction).
- Pass the **label/resource id via the base constructor** (`AbstractArrangeAction(DrawingEditor editor, String labelId)`), which configures the action and is reused for the undo edit's presentation name.
- `BringToFrontAction` defines `arrange = bringToFront`, `reverseArrange = sendToBack`; `SendToBackAction` defines the opposite. The static z-order helpers themselves stay (they already delegate to the model), but they are now called through the primitive operations instead of being duplicated inside boilerplate.

The actual implemented structure:

```java
abstract class AbstractArrangeAction extends AbstractSelectedAction {
    private final String labelId;               // resource id for the menu label + undo name

    protected AbstractArrangeAction(DrawingEditor editor, String labelId) {
        super(editor);
        this.labelId = labelId;
        ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels").configureAction(this, labelId);
        updateEnabledState();
    }

    protected abstract void arrange(DrawingView view, Collection<Figure> figures);
    protected abstract void reverseArrange(DrawingView view, Collection<Figure> figures);

    @Override
    public final void actionPerformed(java.awt.event.ActionEvent e) {   // <-- template method
        final DrawingView view = getView();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        arrange(view, figures);
        fireUndoableEditHappened(new AbstractUndoableEdit() {
            @Override
            public String getPresentationName() {
                return ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels")
                        .getTextProperty(labelId);
            }
            @Override
            public void redo() throws CannotRedoException { super.redo(); arrange(view, figures); }
            @Override
            public void undo() throws CannotUndoException { super.undo(); reverseArrange(view, figures); }
        });
    }
}
```

Each subclass shrinks to a constructor that passes its `ID` up (`super(editor, ID)`) plus the two direction methods.

---

## 3. Strategy of the refactoring

The refactoring is done as a **sequence of small, behaviour-preserving steps**, keeping the build and tests green after each one:

1. **Extract Method** — in each action, ensure the forward and reverse operations are isolated calls (they already are: the static `bringToFront`/`sendToBack` helpers), so the bodies of `actionPerformed` become structurally identical except for those calls.
2. **Extract Superclass** — create `AbstractArrangeAction` between the two actions and `AbstractSelectedAction`.
3. **Form Template Method** — pull the now-identical `actionPerformed` skeleton up into the superclass, and replace the differing steps with calls to abstract primitive operations (`arrange`, `reverseArrange`) while the differing label is passed in through the base constructor (`labelId`).
4. **Pull Up** the shared boilerplate; leave only the primitive-operation implementations in the subclasses.
5. Run the build and the existing arrange tests to confirm **external behaviour is unchanged**.

This is deliberately **behaviour-preserving**: the model calls (`Drawing.bringToFront`/`sendToBack`), the selection handling, and the undo semantics are all kept exactly as before — only their *location* changes.

---

## 4. Which Kerievsky refactoring(s) I applied, and the reasoning

**Primary: Form Template Method** (Kerievsky, *Refactoring to Patterns*).
*Purpose:* "Two methods in subclasses perform similar steps in the same order, yet the steps are different. Generalize the methods by extracting their steps into methods with identical signatures, then pull up the generalized methods to form a Template Method."
*Reasoning:* this is an exact match. The two `actionPerformed` methods run the same ordered algorithm; only individual steps (forward direction, reverse direction, label) differ. Form Template Method removes the duplication by stating the algorithm **once** in the superclass and expressing the variation through polymorphic primitive operations. The undo direction is now an explicit, overridable primitive operation (`reverseArrange`) instead of being buried in duplicated edit boilerplate in both classes. The cross-class reference to the sibling's static helper still exists (`BringToFrontAction.reverseArrange` calls `SendToBackAction.sendToBack` and vice versa), but it is no longer duplicated — it is localized to a single one-line method per class.

**Supporting mechanics** (the smaller refactorings Form Template Method is built from): **Extract Method**, **Extract Superclass**, and **Pull Up Method**. These are the standard moves used to reach the template method safely and incrementally.

*Why Form Template Method rather than an alternative:* the variation here is a fixed pair of directions baked into two existing `Action` subclasses that the menu and toolbar already instantiate by type. A class-based generalization (Template Method) preserves those existing types and wiring while killing the duplication. A Strategy-object approach would also remove the duplication but would force changing how the actions are constructed and registered (`ArrangeToolBar`, `SVGApplicationModel`), which is larger and unnecessary for this smell.

---

## 5. Out of scope (noted, not changed)

`SendToBackAction.sendToBack` contains an `XXX` comment questioning whether the figures should be sorted back-to-front (unlike `bringToFront`, which sorts via `drawing.sort`). This is a possible latent defect, but **fixing it would change external behaviour**, which violates the definition of refactoring. It is therefore left untouched here and recorded as a separate potential change request.
