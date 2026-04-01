package org.jhotdraw.draw.action;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

public class AlignActionScenarioTest
        extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @Test
    public void figures_are_aligned_to_north_edge() {
        given().two_figures_at_different_vertical_positions();
        when().the_user_performs_the_align_action();
        then().all_transformable_figures_are_translated();
    }

    @Test
    public void figures_are_aligned_to_west_edge() {
        given().two_figures_at_different_horizontal_positions();
        when().the_user_performs_the_align_action();
        then().all_transformable_figures_are_translated();
    }

    @Test
    public void figure_is_centered_horizontally() {
        given().a_figure_offset_from_horizontal_center();
        when().the_user_performs_the_align_action();
        then().all_transformable_figures_are_translated();
    }

    @Test
    public void non_transformable_figure_is_not_moved() {
        given().a_non_transformable_figure();
        when().the_user_performs_the_align_action();
        then().no_figure_is_transformed();
    }
}