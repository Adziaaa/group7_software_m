package org.jhotdraw.draw.action;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import org.jhotdraw.draw.action.AlignAction;
import org.jhotdraw.draw.figure.Figure;

import java.awt.geom.Rectangle2D;
import java.util.Set;

public class WhenStage extends Stage<WhenStage> {

    @ExpectedScenarioState
    AlignAction currentAction;

    @ExpectedScenarioState
    Set<Figure> selectedFigures;

    @ExpectedScenarioState
    Rectangle2D.Double selectionBounds;

    public WhenStage the_user_performs_the_align_action() {
        currentAction.alignFigures(selectedFigures, selectionBounds);
        return self();
    }
}