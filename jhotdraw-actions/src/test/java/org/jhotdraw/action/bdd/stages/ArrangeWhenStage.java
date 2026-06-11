package org.jhotdraw.action.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * When-stage for the "arrange" BDD scenarios. Each action takes a snapshot of
 * the stacking order before changing it, so the {@code undo} step can restore it.
 */
public class ArrangeWhenStage extends Stage<ArrangeWhenStage> {

    @ExpectedScenarioState
    private List<String> drawing;

    @ExpectedScenarioState
    private List<String> selected;

    @ExpectedScenarioState
    private Deque<List<String>> history;

    public ArrangeWhenStage the_user_sends_the_selection_to_the_front() {
        saveSnapshot();
        List<String> moving = selectedInStackingOrder();
        drawing.removeAll(moving);
        drawing.addAll(moving); // append at the front (end of the list)
        return self();
    }

    public ArrangeWhenStage the_user_sends_the_selection_to_the_back() {
        saveSnapshot();
        List<String> moving = selectedInStackingOrder();
        drawing.removeAll(moving);
        drawing.addAll(0, moving); // insert at the back (start of the list)
        return self();
    }

    public ArrangeWhenStage the_user_undoes_the_last_action() {
        if (!history.isEmpty()) {
            List<String> previous = history.pop();
            drawing.clear();
            drawing.addAll(previous);
        }
        return self();
    }

    /** Returns the selected figures in their current back-to-front order. */
    private List<String> selectedInStackingOrder() {
        List<String> moving = new ArrayList<>();
        for (String figure : drawing) {
            if (selected.contains(figure)) {
                moving.add(figure);
            }
        }
        return moving;
    }

    private void saveSnapshot() {
        history.push(new ArrayList<>(drawing));
    }
}
