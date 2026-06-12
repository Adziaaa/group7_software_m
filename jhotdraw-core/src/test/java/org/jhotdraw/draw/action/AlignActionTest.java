package org.jhotdraw.draw.action;

import org.junit.Before;
import org.junit.Test;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.mockito.ArgumentCaptor;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AlignActionTest {

    private DrawingEditor mockEditor;
    private DrawingView mockView;
    private Drawing mockDrawing;

    @Before
    public void setUp() {
        mockEditor  = mock(DrawingEditor.class);
        mockView    = mock(DrawingView.class);
        mockDrawing = mock(Drawing.class);
        when(mockEditor.getActiveView()).thenReturn(mockView);
        when(mockView.getDrawing()).thenReturn(mockDrawing);
    }

    /** Creates a mock figure with fixed bounds and configurable transformability. */
    private Figure mockFigure(double x, double y, double w, double h, boolean transformable) {
        Figure f = mock(Figure.class);
        when(f.getBounds()).thenReturn(new Rectangle2D.Double(x, y, w, h));
        when(f.isTransformable()).thenReturn(transformable);
        return f;
    }

    private void setSelectedFigures(Set<Figure> figures) {
        when(mockView.getSelectedFigures()).thenReturn(figures);
    }
    // ── NORTH ──────────────────────────────────────────────────────────────────

    @Test
    public void testNorth_movesLowerFigureToTopEdge() {
        Figure f1 = mockFigure(0, 10, 50, 50, true); // already at top (y=10)
        Figure f2 = mockFigure(0, 30, 50, 50, true); // needs to move up 20px

        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f1, f2));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 10, 50, 70);
        new AlignAction.North(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f2).transform(cap.capture());
        assertEquals(0.0,   cap.getValue().getTranslateX(), 0.001);
        assertEquals(-20.0, cap.getValue().getTranslateY(), 0.001);
    }

    @Test
    public void testNorth_alreadyAligned_translationIsZero() {
        Figure f = mockFigure(0, 10, 50, 50, true);
        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 10, 50, 50);
        new AlignAction.North(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f).transform(cap.capture());
        assertEquals(0.0, cap.getValue().getTranslateX(), 0.001);
        assertEquals(0.0, cap.getValue().getTranslateY(), 0.001);
    }

    // ── SOUTH ──────────────────────────────────────────────────────────────────

    @Test
    public void testSouth_movesUpperFigureToBottomEdge() {
        // selection bottom = 10 + 70 = 80
        Figure f1 = mockFigure(0, 10, 50, 50, true); // bottom at 60, needs +20
        Figure f2 = mockFigure(0, 30, 50, 50, true); // bottom at 80, already there

        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f1, f2));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 10, 50, 70);
        new AlignAction.South(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f1).transform(cap.capture());
        assertEquals(0.0,  cap.getValue().getTranslateX(), 0.001);
        assertEquals(20.0, cap.getValue().getTranslateY(), 0.001);
    }

    // ── WEST ───────────────────────────────────────────────────────────────────

    @Test
    public void testWest_movesRightFigureToLeftEdge() {
        Figure f1 = mockFigure(10, 0, 50, 50, true); // needs to move left 10px
        Figure f2 = mockFigure(0,  0, 50, 50, true); // already at left

        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f1, f2));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 0, 60, 50);
        new AlignAction.West(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f1).transform(cap.capture());
        assertEquals(-10.0, cap.getValue().getTranslateX(), 0.001);
        assertEquals(0.0,   cap.getValue().getTranslateY(), 0.001);
    }

    // ── EAST ───────────────────────────────────────────────────────────────────

    @Test
    public void testEast_movesLeftFigureToRightEdge() {
        // selection right = 0 + 60 = 60
        Figure f1 = mockFigure(0,  0, 50, 50, true); // right at 50, needs +10
        Figure f2 = mockFigure(10, 0, 50, 50, true); // right at 60, already there

        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f1, f2));;
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 0, 60, 50);
        new AlignAction.East(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f1).transform(cap.capture());
        assertEquals(10.0, cap.getValue().getTranslateX(), 0.001);
        assertEquals(0.0,  cap.getValue().getTranslateY(), 0.001);
    }

    // ── HORIZONTAL (center on X axis) ──────────────────────────────────────────

    @Test
    public void testHorizontal_centersFigureOnXAxis() {
        // selection centerX = 0 + 100/2 = 50
        // figure centerX   = 10 + 30/2 = 25  → needs +25
        Figure f = mockFigure(10, 0, 30, 50, true);
        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 0, 100, 50);
        new AlignAction.Horizontal(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f).transform(cap.capture());
        assertEquals(25.0, cap.getValue().getTranslateX(), 0.001);
        assertEquals(0.0,  cap.getValue().getTranslateY(), 0.001);
    }

    // ── VERTICAL (center on Y axis) ────────────────────────────────────────────

    @Test
    public void testVertical_centersFigureOnYAxis() {
        // selection centerY = 0 + 100/2 = 50
        // figure centerY   = 10 + 30/2 = 25  → needs +25
        Figure f = mockFigure(0, 10, 50, 30, true);
        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 0, 50, 100);
        new AlignAction.Vertical(mockEditor).alignFigures(figures, sel);

        ArgumentCaptor<AffineTransform> cap = ArgumentCaptor.forClass(AffineTransform.class);
        verify(f).transform(cap.capture());
        assertEquals(0.0,  cap.getValue().getTranslateX(), 0.001);
        assertEquals(25.0, cap.getValue().getTranslateY(), 0.001);
    }

    // ── BOUNDARY: non-transformable figure is skipped ──────────────────────────

    @Test
    public void testNonTransformableFigure_isNeverTransformed() {
        Figure f = mockFigure(0, 30, 50, 50, false);
        Set<Figure> figures = new LinkedHashSet<>(Arrays.asList(f));
        setSelectedFigures(figures);

        Rectangle2D.Double sel = new Rectangle2D.Double(0, 10, 50, 70);
        new AlignAction.North(mockEditor).alignFigures(figures, sel);

        verify(f, never()).transform(any());
        verify(f, never()).willChange();
        verify(f, never()).changed();
    }
}