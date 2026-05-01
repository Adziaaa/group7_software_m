package org.jhotdraw.draw.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;
import org.jhotdraw.draw.bdd.stages.*;

public class SelectionToolBDDTest
        extends ScenarioTest<SelectionToolGivenStage, SelectionToolWhenStage, SelectionToolThenStage> {

    @Test
    public void user_can_select_a_single_figure() {

        given()
                .a_drawing_with_the_selection_tool_active()
                .and()
                .a_rectangle_figure_exists();

        when()
                .the_user_clicks_the_figure();

        then()
                .the_figure_should_be_selected()
                .and()
                .only_one_figure_should_be_selected();
    }

    @Test
    public void user_can_deselect_all_figures_by_clicking_empty_canvas() {

        given()
                .a_drawing_with_the_selection_tool_active()
                .and()
                .a_rectangle_figure_exists()
                .and()
                .the_figure_is_selected();

        when()
                .the_user_clicks_empty_canvas();

        then()
                .no_figures_should_be_selected();
    }

    @Test
    public void user_can_select_multiple_figures_with_shift_click() {

        given()
                .a_drawing_with_the_selection_tool_active()
                .and()
                .a_rectangle_figure_exists()
                .and()
                .a_second_rectangle_figure_exists()
                .and()
                .the_first_figure_is_selected();

        when()
                .the_user_shift_clicks_the_second_figure();

        then()
                .both_figures_should_be_selected();
    }

    @Test
    public void user_can_select_and_drag_a_figure() {

        given()
                .a_drawing_with_the_selection_tool_active()
                .and()
                .a_rectangle_figure_exists();

        when()
                .the_user_clicks_the_figure();

        then()
                .the_figure_should_be_selected();
    }

    @Test
    public void user_can_use_handle_selection() {

        given()
                .a_drawing_with_the_selection_tool_active()
                .and()
                .a_rectangle_figure_exists()
                .and()
                .the_figure_is_selected();

        when()
                .the_user_clicks_a_handle();

        then()
                .the_figure_should_be_selected();
    }
}