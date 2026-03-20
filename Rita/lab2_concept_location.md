# Concept Location: Selection Tool Feature

## Key Domain Concepts

From the Selection Tool user story, the following domain concepts were identified:

- **Selection**: The process of marking figures for interaction
- **Handle**: Visual elements for resizing and manipulating selected figures
- **Tool**: The primary abstraction for user interaction mechanisms
- **Tracker**: Strategy pattern implementations for different selection states
- **Figure**: The drawable objects that can be selected
- **DrawingView**: The canvas display and interaction layer
- **Drawing**: The container and model for all figures

## Initial Candidate Classes

| Domain Class                 | Responsibility                                                                                                                     |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| **SelectionTool**            | Main tool that coordinates selection behavior; manages three tracker states (area selection, figure dragging, handle manipulation) |
| **DefaultSelectAreaTracker** | Handles area/rubberband selection when user clicks on empty canvas; selects all figures within the drawn rectangle                 |
| **DefaultDragTracker**       | Manages figure movement and dragging; handles single figure selection and multi-figure selection with Shift key                    |
| **DefaultHandleTracker**     | Processes interactions with selection handles; enables resizing, rotating, and other direct figure manipulations                   |
| **Handle**                   | Interface defining the contract for all handle types; manages cursor, bounds, and tracking operations                              |
| **AbstractHandle**           | Base implementation providing common handle functionality; manages figure listeners and drawing area invalidation                  |
| **DragHandle**               | Concrete handle implementation for moving/dragging selected figures across the canvas                                              |
| **LocatorHandle**            | Abstract base class for handles positioned via locators; used for resize and corner/edge handles                                   |
| **Figure**                   | Abstract representation of drawable objects; provides methods for selection, bounds, and handle creation                           |
| **Drawing**                  | Container for all figures; serves as mediator for figure finding and undo/redo operations                                          |
| **DrawingView**              | Displays the drawing and manages selection state; provides coordinate transformation and figure lookup                             |
| **DrawingEditor**            | Coordinates the active tool, drawing view, and overall editing process; manages tool activation/deactivation                       |
