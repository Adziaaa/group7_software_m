package org.jhotdraw.action.bdd.stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Given-stage for the "arrange" (Send to Front / Send to Back) BDD scenarios.
 * <p>
 * The drawing is modelled as an ordered list representing the stacking order
 * (z-order): index {@code 0} is the back-most figure and the last element is the
 * front-most figure.
 */
public class ArrangeGivenStage extends Stage<ArrangeGivenStage> {

    /** Stacking order, back (index 0) to front (last index). */
    @ProvidedScenarioState
    private List<String> drawing = new ArrayList<>();

    @ProvidedScenarioState
    private List<String> selected = new ArrayList<>();

    /** Snapshots of the stacking order, used to support undo. */
    @ProvidedScenarioState
    private Deque<List<String>> history = new ArrayDeque<>();

    public ArrangeGivenStage the_drawing_contains_figures_from_back_to_front(@Quoted String... figures) {
        drawing.addAll(Arrays.asList(figures));
        return self();
    }

    public ArrangeGivenStage the_user_has_selected(@Quoted String figure) {
        selected.add(figure);
        if (!drawing.contains(figure)) {
            drawing.add(figure);
        }
        return self();
    }

    public ArrangeGivenStage the_user_has_selected_figures(@Quoted String... figures) {
        for (String figure : figures) {
            selected.add(figure);
            if (!drawing.contains(figure)) {
                drawing.add(figure);
            }
        }
        return self();
    }
}
