| **Domain Class**         | **Responsibility**                                                                   |
| ------------------------ | ------------------------------------------------------------------------------------ |
| **Drawing**              | Represents the drawing model and stores the figures contained in the drawing.        |
| **DefaultDrawing**       | Concrete implementation of the drawing model used to manage a collection of figures. |
| **DrawingEditor**        | Coordinates the active tool, the drawing view, and the editing process.              |
| **DefaultDrawingEditor** | Concrete editor implementation that manages tools and views during runtime.          |
| **DrawingView**          | Displays the drawing and supports interaction with figures on screen.                |
| **DefaultDrawingView**   | Concrete implementation of the drawing view used by the editor.                      |
| **Figure**               | Abstract representation of a graphical object in the drawing.                        |
| **RectangleFigure**      | Concrete figure class used to represent rectangles.                                  |
| **EllipseFigure**        | Concrete figure class used to represent ellipses.                                    |
| **LineFigure**           | Concrete figure class used to represent lines.                                       |
| **TextFigure**           | Concrete figure class used to represent text elements.                               |
| **LineConnectionFigure** | Represents connections between figures.                                              |
| **Tool**                 | Handles user actions such as selecting, creating, or manipulating figures.           |
| **Handle**               | Supports direct manipulation of figures, such as resizing or moving them.            |
| **Connector**            | Defines connection points used when linking figures together.                        |
