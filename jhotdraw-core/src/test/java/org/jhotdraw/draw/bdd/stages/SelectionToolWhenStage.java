package org.jhotdraw.draw.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;

import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.jhotdraw.draw.tool.SelectionTool;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class SelectionToolWhenStage extends Stage<SelectionToolWhenStage> {

    @ExpectedScenarioState
    protected DrawingView drawingView;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    protected RectangleFigure figure;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    protected RectangleFigure secondFigure;

    @ProvidedScenarioState
    protected SelectionTool selectionTool;

    public SelectionToolWhenStage the_user_clicks_the_figure() {

        simulateClick(new Point2D.Double(
            figure.getBounds().getCenterX(),
            figure.getBounds().getCenterY()
        ));
        return self();
    }

    public SelectionToolWhenStage the_user_clicks_the_second_figure() {

        simulateClick(new Point2D.Double(
            secondFigure.getBounds().getCenterX(),
            secondFigure.getBounds().getCenterY()
        ));

        return self();
    }

    public SelectionToolWhenStage the_user_clicks_empty_canvas() {

        simulateClick(new Point2D.Double(10, 10));

        return self();
    }

    public SelectionToolWhenStage the_user_shift_clicks_the_second_figure() {

        simulateShiftClick(new Point2D.Double(
            secondFigure.getBounds().getCenterX(),
            secondFigure.getBounds().getCenterY()
        ));

        return self();
    }

    public SelectionToolWhenStage the_user_clicks_a_handle() {

        // simplified: assumes handle is triggered via figure click in test setup
        simulateClick(new Point2D.Double(
            figure.getBounds().getCenterX(),
            figure.getBounds().getCenterY()
        ));

        return self();
    }

    private void simulateClick(Point2D.Double point) {
        java.awt.Component comp = drawingView.getComponent();

        MouseEvent press = new MouseEvent(
                comp,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                (int) point.x,
                (int) point.y,
                1,
                false
        );

        MouseEvent release = new MouseEvent(
                comp,
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                0,
                (int) point.x,
                (int) point.y,
                1,
                false
        );

        comp.dispatchEvent(press);
        comp.dispatchEvent(release);
    }

    private void simulateShiftClick(Point2D.Double point) {
        java.awt.Component comp = drawingView.getComponent();

        MouseEvent press = new MouseEvent(
                comp,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                InputEvent.SHIFT_DOWN_MASK,
                (int) point.x,
                (int) point.y,
                1,
                false
        );

        MouseEvent release = new MouseEvent(
                comp,
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                InputEvent.SHIFT_DOWN_MASK,
                (int) point.x,
                (int) point.y,
                1,
                false
        );

        comp.dispatchEvent(press);
        comp.dispatchEvent(release);
    }
}