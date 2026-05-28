# Behavior Driven Testing — Undo / Redo

**Feature:** Undo / Redo
**Class under test:** `org.jhotdraw.undo.UndoRedoManager`
**Tools:** JGiven (BDD scenarios), AssertJ (domain assertions)

## User story

> As a user editing a drawing, I want to undo and redo my recent actions so that I can recover from mistakes without starting over.

## Mapping the user story to BDD scenarios

Each capability in the story maps to a Given-When-Then scenario describing observable behavior rather than implementation.

### Scenario 1 — Undo reverses the last action

```
Given a drawing with one recorded edit
When the user undoes the last action
Then the edit is reversed
And the action can be redone
```

### Scenario 2 — Redo re-applies an undone action

```
Given an edit that has been undone
When the user redoes the action
Then the edit is re-applied
And the action can be undone again
```

### Scenario 3 — Nothing to undo

```
Given a drawing with no recorded edits
Then the undo action is disabled
```

### Scenario 4 — Edits are undone in reverse order

```
Given a drawing with two recorded edits
When the user undoes twice
Then the most recent edit is reversed first
And the earlier edit is reversed second
```

## Automating the scenarios with JGiven

JGiven structures each scenario into three stage classes — Given, When, Then — whose methods read as the steps of the scenario. The example below uses a lightweight `StubEdit` so the scenarios exercise the undo/redo behavior without depending on the real `Drawing`/`Figure` model, and AssertJ for the assertions.

### Maven dependencies

```xml
<dependency>
    <groupId>com.tngtech.jgiven</groupId>
    <artifactId>jgiven-junit</artifactId>
    <version>1.3.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

### Shared test edit

```java
package org.jhotdraw.undo;

import javax.swing.undo.AbstractUndoableEdit;

/** Test double that records whether it is currently done or undone. */
public class StubEdit extends AbstractUndoableEdit {
    private static final long serialVersionUID = 1L;
    public boolean done = true;

    @Override
    public void undo() {
        super.undo();
        done = false;
    }

    @Override
    public void redo() {
        super.redo();
        done = true;
    }
}
```

### Given / When / Then stages

```java
package org.jhotdraw.undo;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.integration.junit.JGivenStage;

import static org.assertj.core.api.Assertions.assertThat;

@JGivenStage
class GivenDrawing extends Stage<GivenDrawing> {

    @ProvidedScenarioState
    UndoRedoManager manager;
    @ProvidedScenarioState
    StubEdit lastEdit;
    @ProvidedScenarioState
    StubEdit firstEdit;

    GivenDrawing a_drawing_with_no_recorded_edits() {
        manager = new UndoRedoManager();
        return self();
    }

    GivenDrawing a_drawing_with_one_recorded_edit() {
        manager = new UndoRedoManager();
        lastEdit = new StubEdit();
        manager.addEdit(lastEdit);
        return self();
    }

    GivenDrawing a_drawing_with_two_recorded_edits() {
        manager = new UndoRedoManager();
        firstEdit = new StubEdit();
        lastEdit = new StubEdit();
        manager.addEdit(firstEdit);
        manager.addEdit(lastEdit);
        return self();
    }

    GivenDrawing an_edit_that_has_been_undone() {
        a_drawing_with_one_recorded_edit();
        manager.undo();
        return self();
    }
}

@JGivenStage
class WhenUser extends Stage<WhenUser> {

    @ProvidedScenarioState
    UndoRedoManager manager;

    WhenUser the_user_undoes_the_last_action() {
        manager.undo();
        return self();
    }

    WhenUser the_user_redoes_the_action() {
        manager.redo();
        return self();
    }

    WhenUser the_user_undoes_twice() {
        manager.undo();
        manager.undo();
        return self();
    }
}

@JGivenStage
class ThenOutcome extends Stage<ThenOutcome> {

    @ProvidedScenarioState
    UndoRedoManager manager;
    @ProvidedScenarioState
    StubEdit lastEdit;
    @ProvidedScenarioState
    StubEdit firstEdit;

    ThenOutcome the_edit_is_reversed() {
        assertThat(lastEdit.done).isFalse();
        return self();
    }

    ThenOutcome the_edit_is_re_applied() {
        assertThat(lastEdit.done).isTrue();
        return self();
    }

    ThenOutcome the_action_can_be_redone() {
        assertThat(manager.canRedo()).isTrue();
        return self();
    }

    ThenOutcome the_action_can_be_undone_again() {
        assertThat(manager.canUndo()).isTrue();
        return self();
    }

    ThenOutcome the_undo_action_is_disabled() {
        assertThat(manager.getUndoAction().isEnabled()).isFalse();
        return self();
    }

    ThenOutcome the_most_recent_edit_is_reversed_first() {
        assertThat(lastEdit.done).isFalse();
        return self();
    }

    ThenOutcome the_earlier_edit_is_reversed_second() {
        assertThat(firstEdit.done).isFalse();
        return self();
    }
}
```

### Scenario test class

```java
package org.jhotdraw.undo;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

public class UndoRedoBddTest
        extends ScenarioTest<GivenDrawing, WhenUser, ThenOutcome> {

    @Test
    public void undo_reverses_the_last_action() {
        given().a_drawing_with_one_recorded_edit();
        when().the_user_undoes_the_last_action();
        then().the_edit_is_reversed()
              .and().the_action_can_be_redone();
    }

    @Test
    public void redo_reapplies_an_undone_action() {
        given().an_edit_that_has_been_undone();
        when().the_user_redoes_the_action();
        then().the_edit_is_re_applied()
              .and().the_action_can_be_undone_again();
    }

    @Test
    public void nothing_to_undo_disables_the_action() {
        given().a_drawing_with_no_recorded_edits();
        then().the_undo_action_is_disabled();
    }

    @Test
    public void edits_are_undone_in_reverse_order() {
        given().a_drawing_with_two_recorded_edits();
        when().the_user_undoes_twice();
        then().the_most_recent_edit_is_reversed_first()
              .and().the_earlier_edit_is_reversed_second();
    }
}
```


## How the feature was verified

The user story is decomposed into four Given-When-Then scenarios describing the externally observable behavior of undo and redo. Each scenario is automated with JGiven, using reusable Given/When/Then stages and AssertJ assertions, and a test double in place of the real model so each scenario isolates the behavior under description. The scenarios confirm that undo reverses an action, redo re-applies it, the undo action is disabled when there is nothing to undo, and that multiple edits are undone in reverse order — matching the capabilities promised by the user story.
