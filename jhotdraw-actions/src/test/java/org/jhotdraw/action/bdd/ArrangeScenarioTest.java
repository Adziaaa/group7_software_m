package org.jhotdraw.action.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.jhotdraw.action.bdd.stages.ArrangeGivenStage;
import org.jhotdraw.action.bdd.stages.ArrangeThenStage;
import org.jhotdraw.action.bdd.stages.ArrangeWhenStage;
import org.junit.Test;

/**
 * BDD scenarios for the Arrange feature (Send to Front / Send to Back),
 * derived from the user story:
 * <p>
 * "As a content creator, I want to arrange overlapping figures by sending them
 * to the front or to the back so that I can control their stacking order and
 * achieve the visual layering I need for my design."
 */
public class ArrangeScenarioTest
        extends ScenarioTest<ArrangeGivenStage, ArrangeWhenStage, ArrangeThenStage> {

    @Test
    public void user_sends_a_figure_to_the_front() {
        given().the_drawing_contains_figures_from_back_to_front("Rectangle", "Circle", "Triangle")
               .and().the_user_has_selected("Rectangle");

        when().the_user_sends_the_selection_to_the_front();

        then().the_figure_should_be_at_the_front("Rectangle")
              .and().the_stacking_order_should_be_from_back_to_front("Circle", "Triangle", "Rectangle");
    }

    @Test
    public void user_sends_a_figure_to_the_back() {
        given().the_drawing_contains_figures_from_back_to_front("Rectangle", "Circle", "Triangle")
               .and().the_user_has_selected("Triangle");

        when().the_user_sends_the_selection_to_the_back();

        then().the_figure_should_be_at_the_back("Triangle")
              .and().the_stacking_order_should_be_from_back_to_front("Triangle", "Rectangle", "Circle");
    }

    @Test
    public void bring_to_front_preserves_relative_order_of_the_selection() {
        given().the_drawing_contains_figures_from_back_to_front("A", "B", "C", "D")
               .and().the_user_has_selected_figures("A", "B");

        when().the_user_sends_the_selection_to_the_front();

        then().the_stacking_order_should_be_from_back_to_front("C", "D", "A", "B");
    }

    @Test
    public void undo_restores_the_original_stacking_order() {
        given().the_drawing_contains_figures_from_back_to_front("Rectangle", "Circle", "Triangle")
               .and().the_user_has_selected("Rectangle");

        when().the_user_sends_the_selection_to_the_front()
              .and().the_user_undoes_the_last_action();

        then().the_stacking_order_should_be_from_back_to_front("Rectangle", "Circle", "Triangle");
    }

    @Test
    public void sending_a_front_figure_to_the_front_keeps_the_order_unchanged() {
        given().the_drawing_contains_figures_from_back_to_front("Rectangle", "Circle")
               .and().the_user_has_selected("Circle"); // already the front-most figure

        when().the_user_sends_the_selection_to_the_front();

        then().the_stacking_order_should_be_from_back_to_front("Rectangle", "Circle");
    }
}
