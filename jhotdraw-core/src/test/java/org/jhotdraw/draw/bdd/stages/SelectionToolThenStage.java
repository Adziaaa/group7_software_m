package org.jhotdraw.draw.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;

import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.figure.RectangleFigure;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectionToolThenStage extends Stage<SelectionToolThenStage> {

    @ExpectedScenarioState
    protected DrawingView drawingView;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    protected RectangleFigure figure;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    protected RectangleFigure secondFigure;

    public SelectionToolThenStage the_figure_should_be_selected() {

        assertThat(drawingView.isFigureSelected(figure))
                .isTrue();

        return self();
    }

    public SelectionToolThenStage the_figure_should_not_be_selected() {

        assertThat(drawingView.isFigureSelected(figure))
                .isFalse();

        return self();
    }

    public SelectionToolThenStage only_one_figure_should_be_selected() {

        assertThat(drawingView.getSelectedFigures())
                .hasSize(1);

        return self();
    }

    public SelectionToolThenStage no_figures_should_be_selected() {

        assertThat(drawingView.getSelectedFigures())
                .isEmpty();

        return self();
    }

    public SelectionToolThenStage both_figures_should_be_selected() {

        assertThat(drawingView.isFigureSelected(figure))
                .isTrue();

        assertThat(drawingView.isFigureSelected(secondFigure))
                .isTrue();

        assertThat(drawingView.getSelectedFigures())
                .hasSize(2);

        return self();
    }

    public SelectionToolThenStage the_second_figure_should_be_selected() {

        assertThat(drawingView.isFigureSelected(secondFigure))
                .isTrue();

        return self();
    }

    public SelectionToolThenStage the_selection_should_contain_two_figures() {

        assertThat(drawingView.getSelectedFigures())
                .hasSize(2);

        return self();
    }

    public SelectionToolThenStage the_selection_should_be_empty() {

        assertThat(drawingView.getSelectedFigures())
                .isEmpty();

        return self();
    }
}