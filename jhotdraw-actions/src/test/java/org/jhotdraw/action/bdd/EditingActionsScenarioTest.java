package org.jhotdraw.action.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.jhotdraw.action.bdd.stages.GivenStage;
import org.jhotdraw.action.bdd.stages.ThenStage;
import org.jhotdraw.action.bdd.stages.WhenStage;
import org.junit.Test;

public class EditingActionsScenarioTest extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

   @Test
    public void user_cuts_selected_elements() {
        given().the_user_has_selected_an_element("Rectangle");

        when().the_user_performs_a_cut_action();

        then().the_element_should_be_removed_from_the_drawing("Rectangle")
              .and().the_element_should_be_stored_in_the_clipboard("Rectangle")
              .and().the_selection_should_be_cleared();
    }

    @Test
    public void user_cuts_multiple_elements() {
        given().the_user_has_selected_multiple_elements("Circle", "Rectangle", "Triangle");

        when().the_user_performs_a_cut_action();

        then().the_element_should_be_removed_from_the_drawing("Circle")
              .and().the_element_should_be_removed_from_the_drawing("Rectangle")
              .and().the_element_should_be_removed_from_the_drawing("Triangle")
              .and().the_element_should_be_stored_in_the_clipboard("Circle")
              .and().the_element_should_be_stored_in_the_clipboard("Rectangle");
    }

    @Test
    public void user_copies_selected_element() {
        given().the_user_has_selected_an_element("Square");

        when().the_user_performs_a_copy_action();

        then().the_element_should_still_be_in_the_drawing("Square")
              .and().the_clipboard_should_contain_the_element("Square");
    }

    @Test
    public void user_pastes_element_from_clipboard() {
        given().the_user_has_selected_an_element("Polygon");

        when().the_user_performs_a_copy_action()
              .and().the_user_performs_a_paste_action();

        then().the_drawing_should_contain_a_pasted_element("Polygon")
              .and().the_element_should_still_be_in_the_drawing("Polygon");
    }

    @Test
    public void user_deletes_selected_element() {
        given().the_drawing_contains_elements("Line", "Circle")
               .and().the_user_has_selected_an_element("Circle");

        when().the_user_performs_a_delete_action();

        then().the_element_should_be_removed_from_the_drawing("Circle")
              .and().the_selection_should_be_cleared()
              .and().the_element_should_still_be_in_the_drawing("Line");
    }

    @Test
    public void user_duplicates_selected_element() {
        given().the_drawing_contains_elements("Rectangle")
               .and().the_user_has_selected_an_element("Rectangle");

        when().the_user_performs_a_duplicate_action();

        then().a_duplicate_of_the_element_should_be_created("Rectangle")
              .and().the_element_should_still_be_in_the_drawing("Rectangle")
              .and().the_number_of_elements_in_the_drawing_should_be(2);
    }

    @Test
    public void user_can_organize_elements_using_cut_and_paste() {
        given().the_drawing_contains_elements("Element1", "Element2", "Element3")
               .and().the_user_has_selected_an_element("Element2");

        when().the_user_performs_a_cut_action()
              .and().the_user_performs_a_paste_action();

        then().the_drawing_should_contain_a_pasted_element("Element2")
              .and().the_element_should_still_be_in_the_drawing("Element1")
              .and().the_element_should_still_be_in_the_drawing("Element3");
    }

    @Test
    public void paste_action_with_empty_clipboard() {
        given().the_clipboard_is_empty()
               .and().no_elements_are_selected();

        when().the_user_performs_a_paste_action();

        then().the_clipboard_should_be_empty();
    }
}
