# Lab — Behavior Driven Testing

**Feature (CASE study):** Arrange — *Send to Front* / *Send to Back*

**User story**

> As a content creator, I want to arrange overlapping figures by sending them to the front or to the back so that I can control their stacking order and achieve the visual layering I need for my design.

**Automation**

- BDD runner: **JGiven** (`com.tngtech.jgiven.junit.ScenarioTest`)
- Domain assertions: **AssertJ** (`org.assertj.core.api.Assertions`)
- Scenario class: `jhotdraw-actions/src/test/java/org/jhotdraw/action/bdd/ArrangeScenarioTest.java`
- Stages: `ArrangeGivenStage`, `ArrangeWhenStage`, `ArrangeThenStage` (package `org.jhotdraw.action.bdd.stages`)

The drawing is modelled as an ordered list representing the stacking order (z-order): index `0` is the back-most figure and the last element is the front-most. This mirrors the lightweight string-list style already used by the existing `EditingActionsScenarioTest`, and reuses the JGiven + AssertJ dependencies already configured in the `jhotdraw-actions` module — so no `pom.xml` change was needed and there is no merge risk with teammates.

---

## 1. Mapping the user story to BDD scenarios

The single user story decomposes into the two capabilities it names ("to the front" / "to the back"), plus the invariants a content creator relies on (relative order is preserved, the action can be undone, and a no-op is harmless).

| User story aspect | BDD scenario (Given → When → Then) |
| ----------------- | ---------------------------------- |
| Send a figure to the front | **Given** the drawing contains "Rectangle", "Circle", "Triangle" from back to front, **and** the user has selected "Rectangle" — **When** the user sends the selection to the front — **Then** "Rectangle" should be at the front **and** the stacking order should be "Circle", "Triangle", "Rectangle". |
| Send a figure to the back | **Given** the drawing contains "Rectangle", "Circle", "Triangle", **and** the user has selected "Triangle" — **When** the user sends the selection to the back — **Then** "Triangle" should be at the back **and** the order should be "Triangle", "Rectangle", "Circle". |
| "Control their stacking order" — relative order of a multi-figure selection is kept | **Given** the drawing contains "A", "B", "C", "D", **and** the user has selected "A" and "B" — **When** the user sends the selection to the front — **Then** the order should be "C", "D", "A", "B". |
| The arrangement can be taken back (Undo) | **Given** the drawing contains "Rectangle", "Circle", "Triangle", **and** the user has selected "Rectangle" — **When** the user sends it to the front **and** undoes the action — **Then** the order should be back to "Rectangle", "Circle", "Triangle". |
| Harmless no-op (boundary) | **Given** the drawing contains "Rectangle", "Circle", **and** the user has selected "Circle" (already at the front) — **When** the user sends the selection to the front — **Then** the order should be unchanged: "Rectangle", "Circle". |

## 2. How JGiven automates the scenarios

Each scenario is a JUnit test in `ArrangeScenarioTest`, written in the fluent `given() … when() … then()` form. JGiven turns the method names into a readable specification and shares state between the three stages:

- **`ArrangeGivenStage`** sets up the scenario state with `@ProvidedScenarioState` fields (`drawing`, `selected`, and a `history` stack for undo). Steps such as `the_drawing_contains_figures_from_back_to_front(...)` and `the_user_has_selected(...)` build the starting situation.
- **`ArrangeWhenStage`** consumes that state with `@ExpectedScenarioState` and performs the action: `the_user_sends_the_selection_to_the_front()`, `the_user_sends_the_selection_to_the_back()`, and `the_user_undoes_the_last_action()`. Each mutating step first pushes a snapshot onto `history`, which is what makes the undo step verifiable.
- **`ArrangeThenStage`** asserts the outcome.

Parameters are annotated with `@Quoted` so they appear in quotes in the generated report, e.g. *Then the figure should be at the front "Rectangle"*.

## 3. Domain assertions with AssertJ

The Then-stage expresses the expectations with AssertJ's fluent, domain-readable assertions, each given a descriptive label via `.as(...)`:

```java
Assertions.assertThat(drawing)
        .as("Stacking order from back to front")
        .containsExactly(expected);
```

`containsExactly(...)` checks both the contents and their order, which is exactly the property that matters for a z-order feature; `isEqualTo(...)` on the first/last element pins the back-most and front-most figure. AssertJ's labelled messages make a failure read in domain terms ("Stacking order from back to front …") rather than as a bare list mismatch.

## 4. What the scenarios verify

Read together, the five scenarios cover the full behaviour promised by the user story: the two directions (front / back), the stacking-order control a user depends on (relative order preserved across a multi-figure selection), the ability to undo an arrangement, and safe behaviour when the action changes nothing. Because they are written from the user's perspective — "send the selection to the front", "should be at the back" — they double as living documentation of how the arrange feature is meant to behave.
