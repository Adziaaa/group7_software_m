package org.jhotdraw.action.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;

import java.util.ArrayList;
import java.util.List;

public class GivenStage extends Stage<GivenStage> {

    @ProvidedScenarioState
    private List<String> selectedElements = new ArrayList<>();

    @ProvidedScenarioState
    private List<String> clipboard = new ArrayList<>();

    @ProvidedScenarioState
    private List<String> drawingElements = new ArrayList<>();

    public GivenStage the_user_has_selected_an_element(@Quoted String elementName) {
        selectedElements.add(elementName);
        if (!drawingElements.contains(elementName)) {
            drawingElements.add(elementName);
        }
        return self();
    }

    public GivenStage the_user_has_selected_multiple_elements(@Quoted String... elements) {
        for (String element : elements) {
            selectedElements.add(element);
            if (!drawingElements.contains(element)) {
                drawingElements.add(element);
            }
        }
        return self();
    }

    public GivenStage the_clipboard_is_empty() {
        clipboard.clear();
        return self();
    }

    public GivenStage the_drawing_contains_elements(@Quoted String... elements) {
        for (String element : elements) {
            drawingElements.add(element);
        }
        return self();
    }

    public GivenStage no_elements_are_selected() {
        selectedElements.clear();
        return self();
    }
}
