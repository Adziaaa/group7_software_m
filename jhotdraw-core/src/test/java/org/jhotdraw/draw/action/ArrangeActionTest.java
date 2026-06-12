/*
 * @(#)ArrangeActionTest.java
 *
 * Unit tests for the "arrange" feature (Send to Front / Send to Back).
 */
package org.jhotdraw.draw.action;

import org.junit.Before;
import org.junit.Test;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.Figure;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.swing.undo.UndoableEdit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BringToFrontAction} and {@link SendToBackAction}.
 * <p>
 * The business logic under test is the z-order reordering and its undo/redo
 * behaviour. The {@link Drawing} model is the dependency that actually performs
 * the reordering, so it is replaced with a Mockito mock: each test exercises a
 * single code-path through a single method and verifies the interaction with
 * that dependency rather than depending on a real drawing implementation.
 */
public class ArrangeActionTest {

    private DrawingEditor editor;
    private DrawingView view;
    private Drawing drawing;
    private Figure f1;
    private Figure f2;

    @Before
    public void setUp() {
        editor  = mock(DrawingEditor.class);
        view    = mock(DrawingView.class);
        drawing = mock(Drawing.class);
        when(editor.getActiveView()).thenReturn(view);
        when(view.getDrawing()).thenReturn(drawing);
        f1 = mock(Figure.class);
        f2 = mock(Figure.class);
    }

    private Set<Figure> selection(Figure... figures) {
        return new LinkedHashSet<>(Arrays.asList(figures));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  BRING TO FRONT — best case
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    public void bringToFront_movesEverySelectedFigureToFront() {
        List<Figure> sorted = Arrays.asList(f1, f2);
        when(drawing.sort(any())).thenReturn(sorted);

        BringToFrontAction.bringToFront(view, selection(f1, f2));

        verify(drawing).bringToFront(f1);
        verify(drawing).bringToFront(f2);
    }

    @Test
    public void bringToFront_appliesFiguresInSortedZOrder() {
        // bringToFront must process figures in the drawing's sorted order so the
        // relative stacking order of the selection is preserved.
        when(drawing.sort(any())).thenReturn(Arrays.asList(f1, f2));

        BringToFrontAction.bringToFront(view, selection(f2, f1));

        InOrder inOrder = inOrder(drawing);
        inOrder.verify(drawing).bringToFront(f1);
        inOrder.verify(drawing).bringToFront(f2);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  SEND TO BACK — best case
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    public void sendToBack_movesEverySelectedFigureToBack() {
        SendToBackAction.sendToBack(view, selection(f1, f2));

        verify(drawing).sendToBack(f1);
        verify(drawing).sendToBack(f2);
        // sendToBack does not pre-sort the figures.
        verify(drawing, never()).sort(any());
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Boundary cases
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    public void bringToFront_emptySelection_doesNothing() {
        when(drawing.sort(any())).thenReturn(Collections.<Figure>emptyList());

        BringToFrontAction.bringToFront(view, Collections.<Figure>emptySet());

        verify(drawing, never()).bringToFront(any());
    }

    @Test
    public void sendToBack_emptySelection_doesNothing() {
        SendToBackAction.sendToBack(view, Collections.<Figure>emptySet());

        verify(drawing, never()).sendToBack(any());
    }

    @Test
    public void bringToFront_singleFigure_movesOnlyThatFigure() {
        when(drawing.sort(any())).thenReturn(Collections.singletonList(f1));

        BringToFrontAction.bringToFront(view, selection(f1));

        verify(drawing, times(1)).bringToFront(f1);
        verify(drawing, never()).bringToFront(f2);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Action + undo/redo — the Template Method in AbstractArrangeAction
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    public void bringToFrontAction_firesUndoableEdit_andUndoSendsBack() {
        when(view.getSelectedFigures()).thenReturn(selection(f1, f2));
        when(drawing.sort(any())).thenReturn(Arrays.asList(f1, f2));

        BringToFrontAction action = new BringToFrontAction(editor);
        action.actionPerformed(null);

        // forward direction applied
        verify(drawing).bringToFront(f1);
        verify(drawing).bringToFront(f2);

        // a reversible edit was registered
        ArgumentCaptor<UndoableEdit> captor = ArgumentCaptor.forClass(UndoableEdit.class);
        verify(drawing).fireUndoableEditHappened(captor.capture());
        UndoableEdit edit = captor.getValue();

        // INVARIANT: an arrange action must always register a non-null undoable
        // edit — this should never fail.
        assert edit != null : "arrange action must register an undoable edit";

        // undo applies the inverse direction (send to back)
        edit.undo();
        verify(drawing).sendToBack(f1);
        verify(drawing).sendToBack(f2);

        // redo re-applies the forward direction
        edit.redo();
        verify(drawing, times(2)).bringToFront(f1);
        verify(drawing, times(2)).bringToFront(f2);
    }

    @Test
    public void sendToBackAction_firesUndoableEdit_andUndoBringsToFront() {
        when(view.getSelectedFigures()).thenReturn(selection(f1, f2));
        when(drawing.sort(any())).thenReturn(Arrays.asList(f1, f2));

        SendToBackAction action = new SendToBackAction(editor);
        action.actionPerformed(null);

        verify(drawing).sendToBack(f1);
        verify(drawing).sendToBack(f2);

        ArgumentCaptor<UndoableEdit> captor = ArgumentCaptor.forClass(UndoableEdit.class);
        verify(drawing).fireUndoableEditHappened(captor.capture());
        UndoableEdit edit = captor.getValue();

        // undo applies the inverse direction (bring to front)
        edit.undo();
        verify(drawing).bringToFront(f1);
        verify(drawing).bringToFront(f2);
    }
}
