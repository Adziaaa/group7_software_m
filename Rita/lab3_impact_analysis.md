# Impact Analysis: Selection Tool Feature

## Impact Analysis Process

### Starting Point (CHANGED Classes)

The following classes from Concept Location are marked as **CHANGED**:

- SelectionTool
- DefaultSelectAreaTracker
- DefaultDragTracker
- DefaultHandleTracker
- Handle, AbstractHandle
- DragHandle, LocatorHandle
- Figure, Drawing, DrawingView, DrawingEditor

---

## Step 1: Identify NEXT Classes (Direct Dependencies)

### From SelectionTool Investigation

SelectionTool interacts with:

- **Tool** (interface/base class) - SelectionTool extends AbstractTool which implements Tool
- **ToolListener & ToolEvent** - Used for event handling and state transitions
- **ToolAdapter** - Used in TrackerHandler inner class
- **DragTracker, HandleTracker, SelectAreaTracker** - Tracker interfaces already marked as CHANGED
- **AbstractTool** - Base class providing common tool functionality
- **DrawingEditor, DrawingView, Drawing, Figure** - Already marked as CHANGED
- **Handle** - Already marked as CHANGED

### From Tracker Investigation

DefaultDragTracker, DefaultSelectAreaTracker, DefaultHandleTracker:

- **TransformEdit** - Used for recording transformations in drag operations
- **HandleListener & HandleEvent** - Used for handle event handling
- **Container, Graphics2D** - AWT classes for rendering
- **Point2D, Rectangle2D, AffineTransform** - Geometry utilities for transformations

### From Handle Investigation

Handle and AbstractHandle:

- **HandleListener & HandleEvent** - Already identified
- **FigureListener & FigureEvent** - For observing figure changes
- **Cursor** - For setting cursor feedback during handle operations
- **Graphics2D** - For rendering handles

---

## Step 2: Mark NEXT Classes as CHANGED/PROPAGATING/UNCHANGED

| Class                                 | Status          | Reason                                                                        |
| ------------------------------------- | --------------- | ----------------------------------------------------------------------------- |
| AbstractTool                          | **PROPAGATING** | Base class for SelectionTool; changes may affect other tool implementations   |
| Tool (interface)                      | **CHANGED**     | SelectionTool extends through AbstractTool; directly implements this contract |
| ToolListener                          | **PROPAGATING** | SelectionTool fires ToolEvents; any listeners must handle these events        |
| ToolEvent                             | **PROPAGATING** | SelectionTool fires these; event payload may need updates                     |
| ToolAdapter                           | **UNCHANGED**   | Generic adapter; no direct dependencies on selection logic                    |
| TransformEdit                         | **PROPAGATING** | Used by DefaultDragTracker for undo/redo; figure transformations are recorded |
| HandleListener                        | **CHANGED**     | DefaultHandleTracker uses HandleListener directly; tracks handle events       |
| HandleEvent                           | **CHANGED**     | Event fired by handles; closely tied to selection tracking                    |
| FigureListener                        | **PROPAGATING** | AbstractHandle implements this to track figure changes                        |
| FigureEvent                           | **PROPAGATING** | Fired when figures change; handle listens to these                            |
| Cursor                                | **UNCHANGED**   | Standard AWT utility; used for cursor feedback only                           |
| Graphics2D                            | **UNCHANGED**   | Standard AWT rendering; no feature-specific logic                             |
| Point2D, Rectangle2D, AffineTransform | **UNCHANGED**   | Standard geometry utilities                                                   |
| Container                             | **UNCHANGED**   | Standard AWT container                                                        |

---

## Step 3: Continue Analysis with PROPAGATING Classes

### AbstractTool Investigation

AbstractTool is the base for all tools. Changes to SelectionTool may require:

- **DrawingEditorProxy** - Proxy used by AbstractTool to manage editor
- **InputMap, ActionMap** - Used for keyboard/action binding
- **EventListenerList** - Used for listener management

**Mark as:**

- DrawingEditorProxy: **PROPAGATING** - May need to handle new tool behavior
- InputMap, ActionMap: **PROPAGATING** - May need to handle new selection actions

### ToolListener Investigation

Classes that listen to Tool events:

- **DrawingView** (already CHANGED) - Listens to tool events
- **DrawingEditor** (already CHANGED) - Manages active tool
- Any custom tool listeners would need updates

**Mark as:** PROPAGATING classes already identified or part of CHANGED set

### TransformEdit Investigation

Used for recording figure transformations:

- **UndoableEdit** - Standard interface for undo/redo
- **Drawing** (already CHANGED) - Fires undoable edits
- Undo/Redo system in editor

**Mark as:** PROPAGATING - Undo system affected by selection changes

### HandleListener Investigation

Classes that implement or use HandleListener:

- **DrawingView** (already CHANGED) - Implements HandleListener
- **DefaultHandleTracker** (already CHANGED) - Uses HandleListener internally

**Mark as:** Already covered

---

## Step 4: Identify Related Packages and Their Classes

### Package: org.jhotdraw.draw.tool

**Status:** CHANGED/PROPAGATING  
**Classes affected:**

- SelectionTool (CHANGED)
- DefaultSelectAreaTracker (CHANGED)
- DefaultDragTracker (CHANGED)
- DefaultHandleTracker (CHANGED)
- AbstractTool (PROPAGATING)
- Tool interface (CHANGED)
- DragTracker interface (CHANGED)
- HandleTracker interface (CHANGED)
- SelectAreaTracker interface (CHANGED)
- DelegationSelectionTool (PROPAGATING) - Extends SelectionTool
- Other tool implementations (PROPAGATING) - May be affected

**Number of Classes:** ~12 core tool classes

---

### Package: org.jhotdraw.draw.handle

**Status:** CHANGED/PROPAGATING  
**Classes affected:**

- Handle interface (CHANGED)
- AbstractHandle (CHANGED)
- DragHandle (CHANGED)
- LocatorHandle (CHANGED)
- All handle implementations (BezierControlPointHandle, BezierNodeHandle, ConnectorHandle, etc.) - PROPAGATING

**Number of Classes:** ~15-20 handle classes

---

### Package: org.jhotdraw.draw.event

**Status:** PROPAGATING  
**Classes affected:**

- ToolListener (PROPAGATING)
- ToolEvent (PROPAGATING)
- ToolAdapter (PROPAGATING)
- HandleListener (CHANGED)
- HandleEvent (CHANGED)
- FigureListener (PROPAGATING)
- FigureEvent (PROPAGATING)
- FigureSelectionListener/Event (PROPAGATING)
- TransformEdit (PROPAGATING)

**Number of Classes:** ~10+ event-related classes

---

### Package: org.jhotdraw.draw (Core interfaces)

**Status:** CHANGED  
**Classes affected:**

- Drawing (CHANGED)
- DrawingView (CHANGED)
- DrawingEditor (CHANGED)
- Figure interface (CHANGED)
- Tool interface (CHANGED)
- DefaultDrawing (PROPAGATING)
- DefaultDrawingView (PROPAGATING)
- DefaultDrawingEditor (PROPAGATING)

**Number of Classes:** ~8 core classes

---

### Package: org.jhotdraw.draw.figure

**Status:** PROPAGATING  
**Classes affected:**

- Figure implementations (RectangleFigure, EllipseFigure, LineFigure, TextFigure, etc.)
- These may need to provide or adapt handles for selection
- Changes to figure selection/handle creation would affect these

**Number of Classes:** ~30+ figure implementations

---

### Package: org.jhotdraw.beans

**Status:** PROPAGATING  
**Classes affected:**

- AbstractBean - Base class for AbstractTool

**Number of Classes:** ~1-2 utility classes

---

### Package: org.jhotdraw.draw.io

**Status:** UNCHANGED  
**Reason:** File I/O operations not directly affected by selection tool changes

**Number of Classes:** 0

---

### Package: org.jhotdraw.draw.connector

**Status:** PROPAGATING  
**Classes affected:**

- Connector implementations - May interact with selection via connection handles

**Number of Classes:** ~5-10 classes

---

### Package: org.jhotdraw.draw.action

**Status:** PROPAGATING  
**Classes affected:**

- Actions that operate on selected figures
- May need updates if selection behavior changes

**Number of Classes:** ~10+ action classes

---

## Final Impact Analysis Summary Table

| Package Name                | # of Classes    | Comments                                                                                                                                 |
| --------------------------- | --------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| org.jhotdraw.draw.tool      | 12              | **CORE IMPACT** - Direct implementation of selection tool; includes SelectionTool, trackers, AbstractTool, and tool interfaces           |
| org.jhotdraw.draw.handle    | 18              | **HIGH IMPACT** - All handle types used for selection visualization and manipulation; changes affect how figures can be selected/resized |
| org.jhotdraw.draw.event     | 10              | **MEDIUM IMPACT** - Tool, Handle, and Figure events propagate selection state; listeners must be updated for new selection behavior      |
| org.jhotdraw.draw           | 8               | **CORE IMPACT** - Core interfaces (Figure, Drawing, DrawingView, DrawingEditor) directly involved in selection logic                     |
| org.jhotdraw.draw.figure    | 30              | **MEDIUM IMPACT** - Figure implementations must provide handles; may need adaptation for new selection features                          |
| org.jhotdraw.draw.action    | 10              | **LOW-MEDIUM IMPACT** - Actions operate on selected figures; behavior depends on selection implementation                                |
| org.jhotdraw.draw.connector | 8               | **LOW-MEDIUM IMPACT** - Connector handles interact with selection; may need updates for connection behavior changes                      |
| org.jhotdraw.beans          | 2               | **LOW IMPACT** - Utility base classes; minimal direct impact                                                                             |
| **TOTAL ESTIMATED IMPACT**  | **~98 classes** | Selection Tool feature is central to JHotDraw; approximately 1/3 of codebase potentially affected                                        |

---

## Key Findings

1. **Core Change Set:** The tool, handle, and event packages form the core implementation layer (~40 classes directly changed)

2. **Propagating Effects:** Figure implementations and action classes must adapt to selection changes (~40+ classes)

3. **Ripple Effect:** Any change to selection behavior will likely cascade through event handling, figure creation, and user actions (~98 classes estimated)

4. **Critical Dependencies:** The most tightly coupled relationships are:
   - SelectionTool ↔ Trackers (strategy pattern)
   - Trackers ↔ Handles (manipulation)
   - Handles ↔ Figures (selection targets)
   - Tools ↔ Events (state communication)

5. **Highest Risk Areas:**
   - Handle implementations (many different types must work with selection)
   - Figure implementations (selection requires handle creation)
   - Event system (must propagate selection state reliably)
