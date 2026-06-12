## 5. Refactoring Implementation

### For the CASE study


### Changes Made

The refactoring would be applied entirely within one file: 
`jhotdraw-core/src/main/java/org/jhotdraw/draw/action/AlignAction.java`.

Two methods were added to the base class `AlignAction`:

- `applyTransform(Figure f, double dx, double dy)` — extracted from the duplicated 
  loop body. Handles the full lifecycle: checks transformability, calls `willChange()`, 
  builds and applies the `AffineTransform`, calls `changed()`, fires the `TransformEdit`.
- `computeTranslation(Rectangle2D.Double figureBounds, Rectangle2D.Double selectionBounds)` 
  — new abstract hook method. Returns a `double[]` of `{dx, dy}` for a given figure.

The existing `alignFigures()` abstract method was made concrete in the base class. 
It now owns the iteration loop and delegates per-figure translation to `computeTranslation()`.

All six inner classes were reduced to only overriding `computeTranslation()`. 
Their constructors and label keys are unchanged.

`AlignToolBar.java` required no changes — constructor signatures were preserved.

**Actual change set = estimated impact set.** Only `org.jhotdraw.draw.action.AlignAction` 
was modified, exactly as predicted in the impact analysis.

---

### SOLID Principles in Context of AlignAction

**Single Responsibility Principle (SRP)**  
Before refactoring, each inner class was responsible for both the algorithm (iterate, 
transform, fire event) and the direction-specific logic. After refactoring, the base 
class owns the algorithm and each subclass is responsible only for computing its 
translation. One class, one reason to change.

**Open/Closed Principle (OCP)**  
The Template Method makes `AlignAction` open for extension and closed for modification. 
Adding a new alignment direction (e.g., `AlignAction.Center`) requires only a new 
subclass implementing `computeTranslation()` — the base class loop does not change.

**Liskov Substitution Principle (LSP)**  
All six subclasses satisfy the `AlignAction` contract. Any code holding a reference to 
`AlignAction` (such as `AlignToolBar`) can use any subclass without knowing the concrete 
type. The refactoring preserves this — the public API is unchanged.

**Interface Segregation Principle (ISP)**  
`AlignAction` exposes only `alignFigures()` to callers. The new `computeTranslation()` 
and `applyTransform()` methods are `protected` — internal to the hierarchy and invisible 
to `AlignToolBar` or any other client.

**Dependency Inversion Principle (DIP)**  
`AlignAction` depends on the `Figure` interface and `DrawingEditor` interface — both 
abstractions. It never references a concrete figure type. The refactoring preserves this: 
`computeTranslation()` receives `Rectangle2D.Double` (a value object), not a concrete 
figure subclass.

---

### Clean Architecture in Context of AlignAction

Clean Architecture organises code into layers where inner layers contain business logic 
and must not depend on outer layers (UI, frameworks). JHotDraw follows this idea through 
its module structure:

- `jhotdraw-core` — business logic layer. Contains `AlignAction` and `AbstractSelectedAction`. 
  This is where the refactoring lives. It depends only on `Figure` and `DrawingEditor` 
  abstractions, not on Swing or any sample application.
- `jhotdraw-api` — interface layer. Defines abstractions like `Figure`, `DrawingEditor`, 
  `DrawingView`. Both core and UI depend on this layer inward.
- `jhotdraw-samples` — outer/UI layer. Contains `AlignToolBar`, which wires Swing buttons 
  to `AlignAction` instances. It depends on `jhotdraw-core`, not the other way around.

The refactoring respects this architecture entirely. No dependency crosses a layer 
boundary in the wrong direction — `AlignAction` still does not reference Swing, and 
`AlignToolBar` still does not know about the internal loop structure. The change is 
confined to the core layer, which is exactly where business logic improvements belong.

This separation also improves future maintainability: if the UI toolkit were replaced, 
`AlignToolBar` could be rewritten without touching `AlignAction` at all.