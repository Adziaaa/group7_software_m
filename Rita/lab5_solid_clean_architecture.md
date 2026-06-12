# Case Study Analysis: SOLID Principles & Clean Architecture

Applying Robert C. Martin's architectural frameworks to the JHotDraw Selection Tool feature and its refactoring.

---

## Part 1: SOLID Principles — Analysis with Code Examples

### S — Single Responsibility Principle

**Definition:** A class should have only one reason to change.

#### Example: SelectionToolTrackerFactory (Correctly Follows SRP)

**The Refactoring Success:**

The refactoring exemplifies SRP by separating concerns:

**Before (Violation):**

```java
public class SelectionTool extends AbstractTool {
    private Tool tracker;
    private HandleTracker handleTracker;
    private SelectAreaTracker selectAreaTracker;
    private DragTracker dragTracker;

    // Reason to change #1: Selection coordination logic changes
    @Override
    public void mousePressed(MouseEvent evt) { ... }

    // Reason to change #2: Tracker creation strategy changes
    protected HandleTracker getHandleTracker(Handle handle) { ... }
    protected DragTracker getDragTracker(Figure f) { ... }
    protected SelectAreaTracker getSelectAreaTracker() { ... }
}
```

SelectionTool had **two reasons to change**:

1. When selection coordination behavior needs modification
2. When tracker creation/instantiation strategy needs modification

**After (Correctly Follows SRP):**

```java
// Responsibility #1: Tracker Creation
public class SelectionToolTrackerFactory {
    private HandleTracker handleTracker;
    private SelectAreaTracker selectAreaTracker;
    private DragTracker dragTracker;

    public HandleTracker getHandleTracker(Handle handle) { ... }
    public DragTracker getDragTracker(Figure figure) { ... }
    public SelectAreaTracker getSelectAreaTracker() { ... }
}

// Responsibility #2: Selection Coordination
public class SelectionTool extends AbstractTool {
    private Tool tracker;
    private SelectionToolTrackerFactory trackerFactory;

    @Override
    public void mousePressed(MouseEvent evt) { ... }
}
```

Now each class has **exactly one reason to change**:

- `SelectionToolTrackerFactory` changes only if tracker creation strategy changes
- `SelectionTool` changes only if selection coordination logic changes

**Why This Matters:** When requirements change, modifications are now localized. A new tracker type doesn't touch SelectionTool's coordination logic, and vice versa.

---

### O — Open/Closed Principle

**Definition:** Software entities should be open for extension, but closed for modification.

#### Example: Tracker Strategy Pattern (Correctly Follows OCP)

**The Design:**

SelectionTool is **open for extension** through the Strategy pattern but **closed for modification**:

```java
public class SelectionTool extends AbstractTool {
    private Tool tracker;  // Abstraction, not concretion

    protected void setTracker(Tool newTracker) {
        if (tracker != null) {
            tracker.deactivate(getEditor());
        }
        tracker = newTracker;
        if (tracker != null) {
            tracker.activate(getEditor());
        }
    }

    // Can extend by creating new Tracker implementations
    // WITHOUT modifying SelectionTool itself
    @Override
    public void mousePressed(MouseEvent evt) {
        // Determine which tracker strategy to use
        if (handle != null) {
            Tool newTracker = trackerFactory.getHandleTracker(handle);
        } else if (figure != null) {
            Tool newTracker = trackerFactory.getDragTracker(figure);
        } else {
            Tool newTracker = trackerFactory.getSelectAreaTracker();
        }
        setTracker(newTracker);
    }
}
```

**Extension Without Modification:**

To add a new selection behavior (e.g., `RotationTracker` for rotating selected figures):

```java
// No changes to SelectionTool needed!
public class RotationTracker extends AbstractTool implements Tool {
    // New tracker implementation
    @Override
    public void mousePressed(MouseEvent evt) { ... }
}

// Just extend the factory
public class SelectionToolTrackerFactory {
    public Tool getRotationTracker(Figure figure) {
        return new RotationTracker(figure);
    }
}
```

SelectionTool remains **closed for modification** while the system is **open for extension**.

---

### L — Liskov Substitution Principle

**Definition:** Objects should be replaceable with instances of their subtypes without altering correctness.

#### Example: Tool Interface Implementations (Correctly Follows LSP)

**The Abstraction:**

```java
public interface Tool extends MouseListener, KeyListener {
    void activate(DrawingEditor editor);
    void deactivate(DrawingEditor editor);
    void draw(Graphics2D g);
}
```

**The Implementations:**

```java
public class DefaultHandleTracker extends AbstractTool implements HandleTracker, Tool {
    @Override
    public void mousePressed(MouseEvent evt) { /* Handle tracker logic */ }
}

public class DefaultDragTracker extends AbstractTool implements DragTracker, Tool {
    @Override
    public void mousePressed(MouseEvent evt) { /* Drag logic */ }
}

public class DefaultSelectAreaTracker extends AbstractTool implements SelectAreaTracker, Tool {
    @Override
    public void mousePressed(MouseEvent evt) { /* Area selection logic */ }
}
```

**Substitutability:**

In SelectionTool, any `Tool` implementation can be substituted without breaking behavior:

```java
public class SelectionTool extends AbstractTool {
    private Tool tracker;  // Could be ANY Tool implementation

    @Override
    public void mousePressed(MouseEvent evt) {
        tracker.mousePressed(evt);  // Works regardless of concrete type
    }
}
```

**Custom Implementations Work Transparently:**

```java
public class CustomSelectionTracker extends AbstractTool implements Tool {
    // Custom implementation for specialized behavior
}

// Can be used without any changes to SelectionTool
selectionTool.setTracker(new CustomSelectionTracker());
selectionTool.mousePressed(evt);  // Still works correctly
```

All tracker implementations are **substitutable** because they correctly implement the `Tool` contract.

---

### I — Interface Segregation Principle

**Definition:** Many client-specific interfaces are better than one general-purpose interface.

#### Example: Specialized Tracker Interfaces (Correctly Follows ISP)

**The Design:**

Instead of one monolithic "Tracker" interface, segregated, specific interfaces:

```java
// Specific interface for handle tracking
public interface HandleTracker extends Tool {
    void setHandles(Handle handle, Collection<Handle> compatibleHandles);
}

// Specific interface for drag tracking
public interface DragTracker extends Tool {
    void setDraggedFigure(Figure f);
}

// Specific interface for area selection
public interface SelectAreaTracker extends Tool {
    // No additional methods; inherits from Tool
}
```

**Client-Specific Binding:**

```java
public class SelectionTool {
    // Only uses what it needs from each interface
    public HandleTracker getHandleTracker(Handle handle) {
        HandleTracker tracker = new DefaultHandleTracker();
        tracker.setHandles(handle, getView().getCompatibleHandles(handle));
        return tracker;
    }

    public DragTracker getDragTracker(Figure f) {
        DragTracker tracker = new DefaultDragTracker();
        tracker.setDraggedFigure(f);
        return tracker;
    }
}
```

**Why This Matters:**

- Clients don't depend on methods they don't use
- Each tracker type only implements methods relevant to its role
- Future changes to one tracker interface don't force changes to others
- Clear contracts: HandleTracker clients know exactly what they can do

**Contrast (Violation):**

If there were a monolithic `Tracker` interface with all methods, clients would depend on methods they don't use:

```java
// BAD - Interface Segregation Violation
public interface Tracker extends Tool {
    void setHandles(Handle handle, Collection<Handle> compatibleHandles);  // Only for handle tracking
    void setDraggedFigure(Figure f);  // Only for drag tracking
    void setArea(Rectangle area);  // Only for area selection
}

// Now SelectAreaTracker must implement methods it doesn't use
public class DefaultSelectAreaTracker implements Tracker {
    @Override
    public void setHandles(...) { throw new UnsupportedOperationException(); }  // BAD

    @Override
    public void setDraggedFigure(...) { throw new UnsupportedOperationException(); }  // BAD
}
```

The segregated design avoids this "interface pollution."

---

### D — Dependency Inversion Principle

**Definition:** Depend upon abstractions, not concretions.

#### Example: Factory Pattern & Abstraction (Correctly Follows DIP)

**The Design:**

```java
public class SelectionTool extends AbstractTool {
    private Tool tracker;  // DEPENDS ON ABSTRACTION (Tool interface)
    private SelectionToolTrackerFactory trackerFactory;

    @Override
    public void mousePressed(MouseEvent evt) {
        // Request tracker from factory
        Tool newTracker = trackerFactory.getHandleTracker(handle);  // Returns abstraction
        // SelectionTool knows nothing about DefaultHandleTracker
    }
}

public class SelectionToolTrackerFactory {
    // FACTORY HANDLES CONCRETION DETAILS
    public HandleTracker getHandleTracker(Handle handle) {
        if (handleTracker == null) {
            handleTracker = new DefaultHandleTracker();  // Concrete instantiation isolated here
        }
        return handleTracker;
    }
}
```

**Dependency Direction:**

```
SelectionTool (High-level)
    ↓ depends on abstraction
Tool interface (abstraction)
    ↑ implemented by
DefaultHandleTracker, DefaultDragTracker, DefaultSelectAreaTracker (Low-level)
```

Dependencies point **upward** to abstractions, not downward to concrete classes.

**Flexibility:**

Because SelectionTool depends on the abstraction (Tool interface), swapping implementations doesn't require changing SelectionTool:

```java
public class SelectionToolTrackerFactory {
    public HandleTracker getHandleTracker(Handle handle) {
        // Can easily swap this
        if (useCustomTrackers) {
            return new CustomHandleTracker(handle);  // Alternative implementation
        }
        return new DefaultHandleTracker();
    }
}

// SelectionTool is unaffected by the change
```

**Violation Example (BAD - Tight Coupling):**

```java
// VIOLATION: Direct dependency on concrete class
public class SelectionTool extends AbstractTool {
    @Override
    public void mousePressed(MouseEvent evt) {
        // Directly instantiating concrete class - tight coupling!
        DefaultHandleTracker tracker = new DefaultHandleTracker();
        tracker.setHandles(handle, ...);
    }
}
// Now SelectionTool is locked into DefaultHandleTracker
// Cannot use CustomHandleTracker without modifying SelectionTool
```

The refactored design correctly inverts the dependency to abstractions.

---

## Part 2: Clean Architecture — Layered Analysis

### Four-Layer Architecture Mapping

The Selection Tool feature maps to Clean Architecture as follows:

#### Layer 1: Entities (Enterprise Business Rules)

**Definition:** Enterprise-wide business rules; most general, least likely to change.

**Selection Tool Entities:**

```
org.jhotdraw.draw.figure.Figure (abstract)
├── Core entity representing any drawable object
├── Business rule: Figures have bounds, can be selected, transformed
└── Likely to change: Only if core drawing concepts change (rare)

org.jhotdraw.draw.Drawing
├── Container managing a collection of figures
├── Business rule: Manages figure relationships, finding figures
└── Likely to change: Only if drawing model fundamentals change

org.jhotdraw.draw.handle.Handle (interface)
├── Represents a point for direct manipulation
├── Business rule: Handles provide control points for transforming figures
└── Likely to change: Only if manipulation concepts change

org.jhotdraw.draw.tool.Tool (interface)
├── Abstract concept of a user interaction tool
├── Business rule: Tools receive events and coordinate operations
└── Likely to change: Only if the tool concept itself changes
```

**These entities are independent of:**

- UI framework (Swing, AWT, JavaFX) — could be swapped
- How selection is rendered — handles could be drawn differently
- How events are received — could use different event systems

---

#### Layer 2: Use Cases (Application-Specific Business Rules / Interactors)

**Definition:** Application-specific business rules; isolated from UI, database, frameworks.

**Selection Tool Interactors:**

```
org.jhotdraw.draw.tool.SelectionTool (Interactor)
├── Use Case: Coordinate user interaction for selecting/manipulating figures
├── Responsibility: Determine which tracker to use based on mouse position
├── Business Logic:
│   ├── If mouse on handle → use HandleTracker
│   ├── If mouse on figure → use DragTracker
│   └── If mouse on background → use SelectAreaTracker
└── Independent of:
    ├── How events are dispatched (UI framework detail)
    ├── How figures are rendered (rendering detail)
    └── Where figures are stored (persistence detail)

org.jhotdraw.draw.tool.DefaultHandleTracker (Interactor)
├── Use Case: Handle direct manipulation via selection handles
├── Responsibility: Track handle dragging, apply transformations to figures
├── Business Logic:
│   ├── Calculate transformation from mouse movement
│   ├── Apply affine transform to selected figures
│   └── Record undo/redo information
└── Independent of: Handle appearance, event source, figure storage

org.jhotdraw.draw.tool.DefaultDragTracker (Interactor)
├── Use Case: Drag selected figures across canvas
├── Responsibility: Move figures to new position
├── Business Logic:
│   ├── Constrain movement if configured
│   ├── Handle multi-figure dragging
│   └── Support drop target notification
└── Independent of: Canvas rendering, event mechanism, storage

org.jhotdraw.draw.tool.DefaultSelectAreaTracker (Interactor)
├── Use Case: Select multiple figures via rubberband area
├── Responsibility: Find and select all figures within drawn rectangle
├── Business Logic:
│   ├── Draw rubberband rectangle
│   ├── Find figures in area
│   ├── Apply selection modifiers (Shift key)
│   └── Handle hover feedback
└── Independent of: Rectangle rendering, event system, persistence
```

**These use cases know nothing about:**

- Swing components or AWT event details
- How figures are drawn or rendered
- Where figures are persisted
- HTTP, databases, or external systems

---

#### Layer 3: Interface Adapters (Controllers, Presenters, Gateways)

**Definition:** Convert data between use cases/entities and external formats.

**Selection Tool Adapters:**

```
org.jhotdraw.draw.tool.SelectionToolTrackerFactory (Gateway)
├── Adapter Type: Creational Gateway / Factory
├── Purpose: Adapt tracker creation logic, isolate instantiation details
├── Responsibility:
│   ├── Provide tracker instances to SelectionTool
│   ├── Manage tracker configuration
│   └── Handle substitution of tracker implementations
├── Bridges: Use Case (SelectionTool) ↔ Concrete implementations (Trackers)

org.jhotdraw.draw.DrawingView (Presenter + Adapter)
├── Adapter Type: Presenter / View Adapter / Controller
├── Purpose: Convert between domain model and UI rendering
├── Responsibility:
│   ├── Listen to selection events from tools
│   ├── Render figures and handles to screen
│   ├── Translate mouse coordinates (View → Drawing coordinates)
│   ├── Manage display state (selected figures, detail level)
│   └── Provide query interfaces (findFigure, getSelectedFigures)
├── Bridges: Entities/Use Cases ↔ Graphics2D rendering

org.jhotdraw.draw.DrawingEditor (Controller / Coordinator)
├── Adapter Type: Controller
├── Purpose: Coordinate interaction between tools and views
├── Responsibility:
│   ├── Manage active tool
│   ├── Activate/deactivate tools
│   ├── Dispatch tool to appropriate view
│   └── Manage undo/redo for user actions
├── Bridges: UI framework ↔ Use Cases

org.jhotdraw.draw.event.* (Event Adapters)
├── ToolEvent, HandleEvent, FigureEvent (Response Models)
├── ToolListener, HandleListener, FigureListener (Output Boundaries)
├── Purpose: Adapt domain state changes to event notifications
└── Bridges: Entities/Use Cases ↔ External observers
```

---

#### Layer 4: Frameworks & Drivers (Outermost)

**Definition:** Web, UI, database, HTTP clients, etc.

**Selection Tool Framework Dependencies:**

```
AWT/Swing Framework
├── java.awt.event.MouseEvent (Request Model)
│   ├── Source: UI event system
│   └── Consumed by: SelectionTool.mousePressed()
├── java.awt.Graphics2D (Renderer)
│   ├── Used by: DrawingView, Handles
│   └── Driver: Window/screen rendering
├── java.awt.Point, Rectangle (Geometry)
│   ├── Data types from AWT
│   └── Used by: All layers
└── javax.swing.* (UI Container)
    ├── JFrame, JPanel for display
    └── Container for DrawingView

Event System (Framework Driver)
├── MouseListener interface
├── KeyListener interface
└── EventListener pattern (external mechanism)

Graphics Rendering (Framework Driver)
├── Graphics2D API
├── Transform and render operations
└── Screen as the "database"
```

---

### Clean Architecture Data Flow: Selection Scenario

**Scenario:** User clicks on a selection handle to resize a figure

#### Request Phase:

```
1. FRAMEWORKS & DRIVERS: Mouse Click Event
   └─→ java.awt.event.MouseEvent created by AWT event system

2. INPUT BOUNDARY: Request Model enters through Tool interface
   └─→ SelectionTool.mousePressed(MouseEvent evt)

3. CONTROLLER/ADAPTER: DrawingEditor dispatches event to active tool
   └─→ tool.mousePressed(evt)
```

#### Processing Phase:

```
4. INTERACTOR: SelectionTool (Use Case) processes request
   ├─→ Find what was clicked: view.findHandle(point)
   ├─→ Determine appropriate tracker
   └─→ Transition to new tracker state
       └─→ newTracker = trackerFactory.getHandleTracker(handle)

5. INTERACTOR: DefaultHandleTracker (Use Case) takes over
   ├─→ Record anchor point
   ├─→ Listen for mouse drag events
   └─→ Calculate transformation based on mouse movement

6. ENTITY: Handle and Figure business rules engaged
   ├─→ Handle.trackStep(anchor, lead, modifiers)
   ├─→ Figure.transform(affineTransform)
   └─→ Figure state updated (geometry changed)

7. ENTITY: Drawing records change
   └─→ drawing.fireUndoableEditHappened(new TransformEdit(...))
```

#### Response Phase:

```
8. INTERACTOR: Tracker completes operation
   └─→ tracker.fireToolDone()

9. EVENT ADAPTER (Output Boundary): Events propagated
   ├─→ ToolEvent fired
   ├─→ FigureEvent fired
   ├─→ ToolListener.toolDone(event)
   └─→ FigureListener.figureChanged(event)

10. PRESENTER (Adapter): DrawingView (listens to events)
    ├─→ drawingView.figureChanged(FigureEvent)
    ├─→ Calculate affected screen region
    └─→ Request repaint

11. PRESENTER: View Model prepared (what to draw)
    ├─→ Determine selection state
    ├─→ Calculate handle positions
    └─→ Prepare render instructions

12. FRAMEWORKS & DRIVERS: Rendering occurs
    ├─→ drawingView.paint(Graphics2D)
    ├─→ Handle.draw(g) draws selection feedback
    ├─→ AWT/Swing renders to screen
    └─→ User sees updated display
```

**Data Flow Diagram:**

```
MouseEvent (Framework)
    ↓
SelectionTool.mousePressed() [INPUT BOUNDARY]
    ↓
SelectionTool (INTERACTOR) - determines tracker
    ↓
DefaultHandleTracker (INTERACTOR) - applies transformation
    ↓
Figure, Handle, Drawing (ENTITIES) - updated state
    ↓
ToolEvent, FigureEvent (RESPONSE MODELS)
    ↓
DrawingView.figureChanged() [OUTPUT BOUNDARY]
    ↓
DrawingView.paint() [PRESENTER] - prepares screen
    ↓
Graphics2D.draw() [FRAMEWORK]
    ↓
Screen (Display)
```

---

### Dependency Rule Analysis

**Clean Architecture Rule:** Dependencies can only point inward (toward the center). Outer layers depend on inner layers, never the reverse.

#### Current Dependency Mapping:

```
Frameworks & Drivers (Outermost)
    ↓ depends on ↓
Interface Adapters (DrawingView, DrawingEditor, Factory)
    ↓ depends on ↓
Use Cases (SelectionTool, Trackers)
    ↓ depends on ↓
Entities (Figure, Drawing, Handle, Tool interface)
    ↓ (Entities don't depend on anything)
```

#### Compliance Evaluation:

✓ **GOOD — Dependencies Point Inward:**

1. **SelectionTool (Use Case)** depends on:
   - `Tool` interface (Entity) ✓
   - `DrawingView.getView()` (Adapter) — abstracted through interface ✓
   - `SelectionToolTrackerFactory` (Adapter/Gateway) ✓

2. **Trackers (Use Cases)** depend on:
   - `Figure` (Entity) ✓
   - `Handle` (Entity) ✓
   - `Drawing` (Entity) ✓
   - `DrawingView` (Adapter) — through abstraction ✓

3. **SelectionToolTrackerFactory (Adapter/Gateway)** depends on:
   - `SelectionTool` (Use Case) — for reference only ✓
   - Concrete tracker classes (lower layer) ✓

4. **DrawingView (Presenter/Adapter)** depends on:
   - `Figure`, `Drawing`, `Handle` (Entities) ✓
   - `Tool` (Entity interface) ✓
   - Graphics2D (Framework) — acceptable boundary ✓

#### Potential Violations:

⚠ **CAUTION — Coupling to Adapters:**

In `SelectionTool.mousePressed()`:

```java
DrawingView view = getView();  // Gets adapter
Handle handle = view.findHandle(anchor);  // Calls adapter method
```

**Issue:** SelectionTool (Use Case) directly calls `DrawingView` (Adapter) methods.

**Analysis:**

- Not a strict violation because `DrawingView` is in the same layer (adapters orchestrate)
- But ideally, `DrawingView` should be abstracted to an interface
- `AbstractTool.getView()` returns concrete `DrawingView`, not interface

**Better Design (Stricter Compliance):**

```java
// Define abstraction for drawing viewport
public interface DrawingViewport {
    Figure findFigure(Point p);
    Handle findHandle(Point p);
    Collection<Handle> getCompatibleHandles(Handle h);
}

// SelectionTool depends on abstraction
public class SelectionTool extends AbstractTool {
    private DrawingViewport viewport;

    @Override
    public void mousePressed(MouseEvent evt) {
        Handle handle = viewport.findHandle(anchor);  // Depends on abstraction
    }
}

// DrawingView implements the adapter
public class DrawingView implements DrawingViewport {
    @Override
    public Handle findHandle(Point p) { ... }
}
```

This would ensure **dependencies only point inward** with no coupling to concrete adapter classes.

#### Entity Independence:

✓ **EXCELLENT — Entities Are Fully Independent:**

The `Figure`, `Drawing`, `Handle`, and `Tool` abstractions:

- Know nothing about Swing, AWT, or any framework
- Would work unchanged with different UI framework (JavaFX, web-based, etc.)
- Could be tested with pure unit tests, no framework dependencies
- Are completely independent of persistence mechanism (could use any database or file format)

**Example:** Testing entity logic requires no framework:

```java
@Test
public void testFigureTransformation() {
    // Pure business logic test - no UI, no framework
    Figure figure = new RectangleFigure();
    AffineTransform tx = new AffineTransform();
    tx.translate(10, 20);

    figure.transform(tx);

    assertTrue(figure.getBounds().contains(new Point2D.Double(10, 20)));
    // No GUI, no event system, no graphics library needed
}
```

---

## Summary: Architecture Quality Assessment

### SOLID Principles Compliance: ✓ EXCELLENT

| Principle                     | Status      | Evidence                                                                                           |
| ----------------------------- | ----------- | -------------------------------------------------------------------------------------------------- |
| **S** — Single Responsibility | ✓ Excellent | SelectionTool handles coordination; Factory handles creation                                       |
| **O** — Open/Closed           | ✓ Excellent | New trackers can be added without modifying SelectionTool                                          |
| **L** — Liskov Substitution   | ✓ Excellent | All Tool implementations are interchangeable                                                       |
| **I** — Interface Segregation | ✓ Good      | Tracker interfaces (HandleTracker, DragTracker, SelectAreaTracker) are specialized, not monolithic |
| **D** — Dependency Inversion  | ✓ Good      | Depends on abstractions (Tool, DrawingView); inversely, Factory handles concretions                |

### Clean Architecture Compliance: ✓ GOOD (with Cautions)

| Layer                    | Compliance  | Notes                                                                       |
| ------------------------ | ----------- | --------------------------------------------------------------------------- |
| **Entities**             | ✓ Excellent | Fully independent, testable without framework                               |
| **Use Cases**            | ✓ Excellent | Clear interactors; business logic isolated from UI                          |
| **Interface Adapters**   | ✓ Good      | Factory pattern excellent; ViewAdapter could use stronger abstraction       |
| **Frameworks & Drivers** | ✓ Good      | Framework details properly isolated to outer layer                          |
| **Dependency Rule**      | ⚠ Good      | Generally inward; minor coupling to ViewAdapter could be abstracted further |

### Refactoring Impact on Architecture

**The "Move Creation Knowledge to Factory" refactoring improved:**

- **SRP:** Separated two distinct responsibilities
- **DIP:** Isolated concrete instantiation to factory layer
- **Architecture Clarity:** Clearer layer separation between use cases and adapters

**Architectural Recommendation:**

Consider introducing `DrawingViewport` interface to complete the DIP implementation:

- Eliminates SelectionTool's dependency on concrete DrawingView
- Strengthens the Dependency Rule
- Makes testing easier (mock viewport without framework)
- Enables alternative view implementations transparently

This would move the architecture from "Good" to "Excellent" compliance with Clean Architecture principles.
