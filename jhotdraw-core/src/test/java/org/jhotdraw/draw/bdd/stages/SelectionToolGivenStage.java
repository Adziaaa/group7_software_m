package org.jhotdraw.draw.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.jhotdraw.draw.tool.SelectionTool;

import java.awt.geom.Point2D;

public class SelectionToolGivenStage extends Stage<SelectionToolGivenStage> {

    @ProvidedScenarioState
    protected Drawing drawing;

    @ProvidedScenarioState
    protected DrawingView drawingView;

    @ProvidedScenarioState
    protected DrawingEditor editor;

    @ProvidedScenarioState
    protected SelectionTool selectionTool;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    protected RectangleFigure figure;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    protected RectangleFigure secondFigure;

    public SelectionToolGivenStage a_drawing_with_the_selection_tool_active() {

        drawing = new DefaultDrawing();

        drawingView = new DefaultDrawingView();
        drawingView.setDrawing(drawing);

        editor = new DefaultDrawingEditor();
        editor.add(drawingView);

        selectionTool = new SelectionTool();
        selectionTool.activate(editor);

        editor.setTool(selectionTool);

        return self();
    }

    public SelectionToolGivenStage a_rectangle_figure_exists() {

        figure = new RectangleFigure();

        figure.setBounds(
                new Point2D.Double(100, 100),
                new Point2D.Double(200, 200));

        drawing.add(figure);

        return self();
    }

    public SelectionToolGivenStage a_second_rectangle_figure_exists() {

        secondFigure = new RectangleFigure();

        secondFigure.setBounds(
                new Point2D.Double(300, 300),
                new Point2D.Double(400, 400));

        drawing.add(secondFigure);

        return self();
    }

    public SelectionToolGivenStage the_figure_is_selected() {

        drawingView.addToSelection(figure);

        return self();
    }

    public SelectionToolGivenStage the_first_figure_is_selected() {

        drawingView.addToSelection(figure);

        return self();
    }

    public SelectionToolGivenStage no_figures_are_selected() {

        drawingView.clearSelection();

        return self();
    }
}