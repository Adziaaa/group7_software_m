## 4. Refactoring Patterns and Code Smells

### Code Smell Identified

The code smell that triggered this refactoring is **Duplicated Code**, one of the most 
common and damaging smells described in Chapter 3 of [Ker05]. SonarLint flagged 
repeated identical blocks across the six inner classes of `AlignAction`. Specifically, 
every subclass (`North`, `South`, `East`, `West`, `Horizontal`, `Vertical`) contains 
this identical 7-line loop inside its `alignFigures()` method:

```java
for (Figure f : getView().getSelectedFigures()) {
    if (f.isTransformable()) {
        f.willChange();
        Rectangle2D.Double b = f.getBounds();
        AffineTransform tx = new AffineTransform();
        tx.translate(/* only this line differs */);
        f.transform(tx);
        f.changed();
        fireUndoableEditHappened(new TransformEdit(f, tx));
    }
}
```

The only variation across all six classes is how `dx` and `dy` are computed for 
`tx.translate(dx, dy)`. The entire surrounding algorithm — iterating the selection, 
checking transformability, calling `willChange()`/`changed()`, applying the transform, 
and firing the undo event — is duplicated verbatim. This violates the DRY principle and 
means any future change to this loop (e.g., fixing the undo grouping noted in the 
existing `XXX - Fire edit events` comment) must be made in six places independently.

---

### Planned Change

The refactoring will eliminate the duplication by moving the shared algorithm into the 
base class `AlignAction`, leaving each subclass responsible only for computing the 
translation offset specific to its direction. No external behavior changes — the six 
alignment operations will produce identical results before and after refactoring.

---

### Refactoring Strategy: Form Template Method

The applicable pattern from [Ker05] is **Form Template Method** (Chapter 8). This 
pattern applies when subclasses contain similar algorithms that differ only in certain 
steps. The solution is to move the invariant algorithm into the base class as a 
*template method*, and define an abstract hook method for the step that varies.

The strategy applied in three steps:

**Step 1 — Extract Method.**  
Extract the duplicated loop from each `alignFigures()` override into a new protected 
method in the base class `AlignAction`:

```java
protected void applyTransform(Figure f, double dx, double dy) {
    if (f.isTransformable()) {
        f.willChange();
        AffineTransform tx = new AffineTransform();
        tx.translate(dx, dy);
        f.transform(tx);
        f.changed();
        fireUndoableEditHappened(new TransformEdit(f, tx));
    }
}
```

**Step 2 — Introduce abstract hook.**  
Replace the abstract method `alignFigures()` with a new abstract method that only 
asks each subclass for its translation offset per figure:

```java
protected abstract double[] computeTranslation(
    Rectangle2D.Double figureBounds, 
    Rectangle2D.Double selectionBounds
);
```

**Step 3 — Pull Up Method.**  
Move the now-unified loop into `alignFigures()` in the base class. Each subclass 
no longer overrides `alignFigures()` at all — it only implements `computeTranslation()`:

```java
// In AlignAction base class:
@Override
protected void alignFigures(Collection<Figure> selectedFigures, 
                             Rectangle2D.Double selectionBounds) {
    for (Figure f : getView().getSelectedFigures()) {
        Rectangle2D.Double b = f.getBounds();
        double[] t = computeTranslation(b, selectionBounds);
        applyTransform(f, t[0], t[1]);
    }
}
```

Example of a refactored subclass — from 14 lines to 4:

```java
public static class North extends AlignAction {
    public North(DrawingEditor editor) {
        super(editor);
        labels.configureAction(this, "edit.alignNorth");
    }

    @Override
    protected double[] computeTranslation(Rectangle2D.Double b, 
                                          Rectangle2D.Double selectionBounds) {
        return new double[]{ 0, selectionBounds.y - b.y };
    }
}
```

---

### Reasoning

Form Template Method is the right choice here because the problem is not just 
duplicated lines — it is a duplicated *algorithm structure* across sibling classes. 
Extract Method alone would reduce repetition but still leave six copies of the loop. 
By pulling the algorithm into the base class and making subclasses supply only the 
varying data, we achieve a single authoritative implementation of the transform loop. 
Any future fix (such as resolving the incomplete `CompositeEdit` undo grouping flagged 
in the existing `XXX` comment) now requires a change in exactly one place.

---

### Review Questions

**1. Definition of Refactoring:**  
Refactoring is the process of restructuring existing source code — changing its internal 
structure — without altering its observable external behavior. It is done through a 
sequence of small, behavior-preserving transformations, each leaving the system in a 
working state [Ker05].

**2. What are Refactoring Patterns?**  
Refactoring patterns are named, reusable strategies for common refactoring situations. 
They go beyond simple mechanical transforms (like Extract Method) to describe how to 
move code toward a well-known design pattern incrementally and safely, as described 
in [Ker05].

**3. How do you identify code smells? Give examples.**  
Code smells are identified by reading source code and recognizing structural symptoms 
that suggest deeper problems. Tools like SonarLint can flag them automatically. 
Common examples: **Duplicated Code** (same logic in multiple places), **Long Method** 
(a method that does too much), **Large Class** (a class with too many responsibilities), 
**Feature Envy** (a method that uses another class's data more than its own).