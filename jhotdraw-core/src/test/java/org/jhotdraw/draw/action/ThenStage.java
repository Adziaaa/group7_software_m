package org.jhotdraw.draw.action;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import org.assertj.core.api.Assertions;
import org.jhotdraw.draw.figure.Figure;
import org.mockito.ArgumentCaptor;

import java.awt.geom.AffineTransform;
import java.util.Set;

import static org.mockito.Mockito.*;

public class ThenStage extends Stage<ThenStage> {

    @ExpectedScenarioState
    Set<Figure> selectedFigures;

    public ThenStage all_transformable_figures_are_translated() {
        for (Figure f : selectedFigures) {
            if (f.isTransformable()) {
                verify(f, atLeastOnce()).transform(any(AffineTransform.class));
            }
        }
        return self();
    }

    public ThenStage the_figure_is_translated_to_the_top_edge(double expectedY) {
        for (Figure f : selectedFigures) {
            ArgumentCaptor<AffineTransform> cap =
                ArgumentCaptor.forClass(AffineTransform.class);
            verify(f).transform(cap.capture());
            Assertions.assertThat(cap.getValue().getTranslateY())
                .as("Figure should be translated to top edge")
                .isEqualTo(expectedY, Assertions.within(0.001));
        }
        return self();
    }

    public ThenStage no_figure_is_transformed() {
        for (Figure f : selectedFigures) {
            verify(f, never()).transform(any());
            verify(f, never()).willChange();
        }
        return self();
    }
}