package org.jhotdraw.action.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import org.assertj.core.api.Assertions;

import java.util.List;

public class ThenStage extends Stage<ThenStage> {

    @ExpectedScenarioState
    private List<String> selectedElements;

    @ExpectedScenarioState
    private List<String> clipboard;

    @ExpectedScenarioState
    private List<String> drawingElements;

    public ThenStage the_element_should_be_removed_from_the_drawing(@Quoted String elementName) {
        Assertions.assertThat(drawingElements)
                .as("Drawing should not contain the removed element")
                .doesNotContain(elementName);
        return self();
    }

    public ThenStage the_element_should_be_stored_in_the_clipboard(@Quoted String elementName) {
        Assertions.assertThat(clipboard)
                .as("Clipboard should contain the element")
                .contains(elementName);
        return self();
    }

    public ThenStage the_element_should_still_be_in_the_drawing(@Quoted String elementName) {
        Assertions.assertThat(drawingElements)
                .as("Drawing should still contain the element")
                .contains(elementName);
        return self();
    }

    public ThenStage the_clipboard_should_contain_the_element(@Quoted String elementName) {
        Assertions.assertThat(clipboard)
                .as("Clipboard should contain the copied element")
                .contains(elementName);
        return self();
    }

    public ThenStage a_duplicate_of_the_element_should_be_created(@Quoted String elementName) {
        String expectedDuplicate = elementName + "_copy";
        Assertions.assertThat(drawingElements)
                .as("Drawing should contain the duplicated element")
                .contains(expectedDuplicate);
        return self();
    }

    public ThenStage the_clipboard_should_be_empty() {
        Assertions.assertThat(clipboard)
                .as("Clipboard should be empty")
                .isEmpty();
        return self();
    }

    public ThenStage the_selection_should_be_cleared() {
        Assertions.assertThat(selectedElements)
                .as("Selected elements should be cleared")
                .isEmpty();
        return self();
    }

    public ThenStage the_drawing_should_contain_a_pasted_element(@Quoted String elementName) {
        String pastedElement = elementName + "_pasted";
        Assertions.assertThat(drawingElements)
                .as("Drawing should contain the pasted element")
                .contains(pastedElement);
        return self();
    }

    public ThenStage the_number_of_elements_in_the_drawing_should_be(int expectedCount) {
        Assertions.assertThat(drawingElements)
                .as("Drawing should contain the expected number of elements")
                .hasSize(expectedCount);
        return self();
    }
}
