# Lab 5 - Actualization

**Feature (CASE study):** Arrange - *Send to Front* / *Send to Back*
**Refactoring applied:** Form Template Method extracted `AbstractArrangeAction` as the shared base of `BringToFrontAction` and `SendToBackAction`.

---

## 1. Actualization and change propagation

The actualization phase incorporated the new structure into the existing code and propagated the change to every place that depended on the modified classes.

**New code incorporated**

- `org.jhotdraw.draw.action.AbstractArrangeAction` (new) - holds the invariant algorithm shared by all arrange actions as a template method: read the selected figures, apply the operation, and register a reversible `UndoableEdit`.
- `BringToFrontAction` and `SendToBackAction` were re-parented from `AbstractSelectedAction` to `AbstractArrangeAction` and reduced to their two primitive operations (`arrange` / `reverseArrange`) plus their existing static z-order helpers.

**Change propagation (secondary modifications)**

I traced every place that constructs or references the two actions to confirm what needed updating:

| Call site | Usage | Secondary change needed? |
| --------- | ----- | ------------------------ |
| `jhotdraw-gui` `ButtonFactory` | `new BringToFrontAction(editor)` / `new SendToBackAction(editor)`, `.ID` | No |
| `samples/svg` `ArrangeToolBar`, `ToolsToolBar` | constructor + `.ID` | No |
| `samples/svg` `SVGApplicationModel` | constructor + `.ID` in action map / menu | No |
| `samples/odg`, `samples/net`, `samples/pert`, `samples/draw` panels | constructor | No |

Because the **public contract was preserved** - the one-argument constructor `(DrawingEditor)`, the `ID` constants, and the public static helpers `bringToFront(...)` / `sendToBack(...)` all stayed the same - **no secondary modifications were required**. Comparing the actual change set with the estimate: the two **existing** classes that changed (`BringToFrontAction`, `SendToBackAction`) match the estimated impact set from the impact-analysis lab exactly; the refactoring **additionally introduced one new class** (`AbstractArrangeAction`) that impact analysis could not have predicted, since it did not yet exist; and **nothing else propagated**. This is close to the ideal actualization outcome: the change is contained to the action package and does not ripple outward.

---

## 2. SOLID principles in context of the CASE study

**Single Responsibility Principle (SRP)**
Before the refactoring, each action class did two jobs: it ran the whole workflow (read selection → reorder → build the undoable edit) *and* defined its specific direction. After the refactoring, `AbstractArrangeAction` is solely responsible for the workflow, and each subclass is responsible only for *which direction* to move figures. Each class now has a single reason to change - e.g. a change to how undo is recorded touches only the base class.

**Open/Closed Principle (OCP)**
The template method makes the hierarchy open for extension but closed for modification. A new arrange operation - for example a future `BringForwardAction` (move one step toward the front) - is added by writing a new subclass that implements `arrange` and `reverseArrange`; the algorithm in `AbstractArrangeAction.actionPerformed` does not change. Before the refactoring, every new direction meant copy-pasting the full method body.

**Liskov Substitution Principle (LSP)**
`BringToFrontAction` and `SendToBackAction` both honor the `AbstractArrangeAction` contract, so any client that holds the base type (or the Swing `Action` type) - such as `ArrangeToolBar`, `ToolsToolBar`, or the action map in `SVGApplicationModel` - can use either subclass interchangeably without knowing the concrete type. The refactoring preserves substitutability because the public API is unchanged.

**Interface Segregation Principle (ISP)**
Clients of the arrange actions (the toolbars and menu builder) depend only on the public surface they actually use: the constructor, the `ID`, and the `Action` interface. The new primitive operations `arrange` and `reverseArrange` are declared `protected` - internal to the action hierarchy and invisible to any UI client. No client is forced to depend on methods it does not use.

**Dependency Inversion Principle (DIP)**
`AbstractArrangeAction` depends on abstractions, not concretions: `DrawingView`, `DrawingEditor`, `Drawing`, and `Figure` are all interfaces from `jhotdraw-api`. The actual reordering is delegated to the `Drawing` abstraction via `drawing.bringToFront(figure)` / `drawing.sendToBack(figure)`; the action never references a concrete figure or drawing implementation (e.g. `QuadTreeDrawing`). High-level policy (the arrange workflow) and low-level detail (how a specific drawing stores z-order) both depend on the shared abstraction.

---

## 3. Clean Architecture in context of the CASE study

JHotDraw is organized into modules that form concentric layers, with dependencies pointing inward toward abstractions:

- **`jhotdraw-api` - abstraction/interface layer.** Defines `Figure`, `Drawing`, `DrawingEditor`, `DrawingView`. Both the core logic and the UI depend inward on these interfaces.
- **`jhotdraw-core` - business-logic layer.** Contains `AbstractSelectedAction`, the new `AbstractArrangeAction`, and the two arrange actions. This is where the refactoring lives. It depends only on the API abstractions, never on Swing UI or on any sample application.
- **`jhotdraw-samples` - outer / UI-framework layer.** Contains `ArrangeToolBar` and `SVGApplicationModel`, which wire Swing buttons and menu items to the arrange actions. It depends on `jhotdraw-core`, not the other way around.

The refactoring respects these boundaries completely. The change is confined to the core layer - exactly where business-logic improvements belong. No dependency crosses a boundary in the wrong direction: `AbstractArrangeAction` still does not import or reference any Swing toolbar, and `ArrangeToolBar` remains unaware of the internal template-method structure. The only framework dependency in the base class, `javax.swing.undo.UndoableEdit`, is an actualization detail used at the boundary to record reversibility; the actual model mutation is still delegated inward through the `Drawing` abstraction.

A practical benefit of keeping the change inside the core layer: if the UI toolkit were replaced, `ArrangeToolBar` could be rewritten without touching `AbstractArrangeAction` at all - and conversely, the arrange workflow could be extended (OCP) without any change to the UI layer.
