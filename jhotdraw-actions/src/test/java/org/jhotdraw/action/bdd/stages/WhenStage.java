package org.jhotdraw.action.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;

import java.util.List;

public class WhenStage extends Stage<WhenStage> {

    @ExpectedScenarioState
    private List<String> selectedElements;

    @ExpectedScenarioState
    private List<String> clipboard;

    @ExpectedScenarioState
    private List<String> drawingElements;

        public WhenStage the_user_performs_a_cut_action() {
        clipboard.addAll(selectedElements);
        drawingElements.removeAll(selectedElements);
        selectedElements.clear();
        return self();
    }

    public WhenStage the_user_performs_a_copy_action() {
        clipboard.addAll(selectedElements);
        return self();
    }

    public WhenStage the_user_performs_a_paste_action() {
        if (!clipboard.isEmpty()) {
            for (String item : clipboard) {
                String pastedItem = item + "_pasted";
                drawingElements.add(pastedItem);
                selectedElements.add(pastedItem);
            }
        }
        return self();
    }

    public WhenStage the_user_performs_a_delete_action() {
        drawingElements.removeAll(selectedElements);
        selectedElements.clear();
        return self();
    }

    public WhenStage the_user_performs_a_duplicate_action() {
        List<String> duplicates = new java.util.ArrayList<>();
        for (String element : selectedElements) {
            String duplicate = element + "_copy";
            duplicates.add(duplicate);
            drawingElements.add(duplicate);
        }
        selectedElements.clear();
        selectedElements.addAll(duplicates);
        return self();
    }
}
