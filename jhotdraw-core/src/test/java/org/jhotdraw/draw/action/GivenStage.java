package org.jhotdraw.draw.action;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.action.AlignAction;
import org.jhotdraw.draw.figure.Figure;

import java.awt.geom.Rectangle2D;
import java.util.LinkedHashSet;
import java.util.Set;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class GivenStage extends Stage<GivenStage> {

    @ProvidedScenarioState
    DrawingEditor mockEditor;

    @ProvidedScenarioState
    DrawingView mockView;

    @ProvidedScenarioState
    Drawing mockDrawing;

    @ProvidedScenarioState
    Set<Figure> selectedFigures;

    @ProvidedScenarioState
    Rectangle2D.Double selectionBounds;

    @ProvidedScenarioState
    AlignAction currentAction;

    public GivenStage two_figures_at_different_vertical_positions() {
        setUpMocks();
        Figure f1 = mockFigure(0, 10, 50, 50);
        Figure f2 = mockFigure(0, 30, 50, 50);
        selectedFigures = new LinkedHashSet<>();
        selectedFigures.add(f1);
        selectedFigures.add(f2);
        Mockito.when(mockView.getSelectedFigures()).thenReturn(selectedFigures);
        selectionBounds = new Rectangle2D.Double(0, 10, 50, 70);
        currentAction = new AlignAction.North(mockEditor);
        return self();
    }

    public GivenStage two_figures_at_different_horizontal_positions() {
        setUpMocks();
        Figure f1 = mockFigure(10, 0, 50, 50);
        Figure f2 = mockFigure(0,  0, 50, 50);
        selectedFigures = new LinkedHashSet<>();
        selectedFigures.add(f1);
        selectedFigures.add(f2);
        Mockito.when(mockView.getSelectedFigures()).thenReturn(selectedFigures);
        selectionBounds = new Rectangle2D.Double(0, 0, 60, 50);
        currentAction = new AlignAction.West(mockEditor);
        return self();
    }

    public GivenStage a_figure_offset_from_horizontal_center() {
        setUpMocks();
        Figure f = mockFigure(10, 0, 30, 50);
        selectedFigures = new LinkedHashSet<>();
        selectedFigures.add(f);
        Mockito.when(mockView.getSelectedFigures()).thenReturn(selectedFigures);
        selectionBounds = new Rectangle2D.Double(0, 0, 100, 50);
        currentAction = new AlignAction.Horizontal(mockEditor);
        return self();
    }

    public GivenStage a_non_transformable_figure() {
        setUpMocks();
        Figure f = mock(Figure.class);
        Mockito.when(f.getBounds()).thenReturn(new Rectangle2D.Double(0, 30, 50, 50));
        Mockito.when(f.isTransformable()).thenReturn(false);
        selectedFigures = new LinkedHashSet<>();
        selectedFigures.add(f);
        Mockito.when(mockView.getSelectedFigures()).thenReturn(selectedFigures);
        selectionBounds = new Rectangle2D.Double(0, 10, 50, 70);
        currentAction = new AlignAction.North(mockEditor);
        return self();
    }

    private void setUpMocks() {
        mockEditor  = mock(DrawingEditor.class);
        mockView    = mock(DrawingView.class);
        mockDrawing = mock(Drawing.class);
        Mockito.when(mockEditor.getActiveView()).thenReturn(mockView);
        Mockito.when(mockView.getDrawing()).thenReturn(mockDrawing);
    }

    private Figure mockFigure(double x, double y, double w, double h) {
        Figure f = mock(Figure.class);
        Mockito.when(f.getBounds()).thenReturn(new Rectangle2D.Double(x, y, w, h));
        Mockito.when(f.isTransformable()).thenReturn(true);
        return f;
    }
}