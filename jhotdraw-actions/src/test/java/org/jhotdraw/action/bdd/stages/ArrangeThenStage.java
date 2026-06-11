package org.jhotdraw.action.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import org.assertj.core.api.Assertions;

import java.util.List;

/**
 * Then-stage for the "arrange" BDD scenarios. Uses AssertJ for the domain
 * assertions on the stacking order.
 */
public class ArrangeThenStage extends Stage<ArrangeThenStage> {

    @ExpectedScenarioState
    private List<String> drawing;

    @ExpectedScenarioState
    private List<String> selected;

    public ArrangeThenStage the_figure_should_be_at_the_front(@Quoted String figure) {
        Assertions.assertThat(drawing.get(drawing.size() - 1))
                .as("Front-most figure")
                .isEqualTo(figure);
        return self();
    }

    public ArrangeThenStage the_figure_should_be_at_the_back(@Quoted String figure) {
        Assertions.assertThat(drawing.get(0))
                .as("Back-most figure")
                .isEqualTo(figure);
        return self();
    }

    public ArrangeThenStage the_stacking_order_should_be_from_back_to_front(@Quoted String... expected) {
        Assertions.assertThat(drawing)
                .as("Stacking order from back to front")
                .containsExactly(expected);
        return self();
    }

    public ArrangeThenStage the_drawing_should_still_contain(@Quoted String figure) {
        Assertions.assertThat(drawing)
                .as("Drawing should still contain the figure")
                .contains(figure);
        return self();
    }
}
