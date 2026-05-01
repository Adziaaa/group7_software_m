# Selection Tool - BDD Testing (JGiven + AssertJ)

## User Story

As a user, I want to select figures on the canvas using the selection tool so that I can interact with, modify, and manipulate existing drawing objects.

---

# BDD Approach

This project uses:

- **JGiven** → Given / When / Then scenario structure
- **AssertJ** → Fluent assertions in Then stage
- **Swing-based simulation** → Mouse interactions for selection behavior

---

# BDD Scenario Overview

## 1. Select a Single Figure

**Given**
- A drawing exists with a rectangle figure
- Selection tool is active

**When**
- The user clicks the figure

**Then**
- The figure is selected
- Only one figure is selected

---

## 2. Deselect Figures by Clicking Empty Space

**Given**
- A figure is selected

**When**
- The user clicks on empty canvas space

**Then**
- No figures are selected

---

## 3. Multi-Selection with Shift Click

**Given**
- Two figures exist on the canvas
- First figure is selected

**When**
- The user Shift-clicks the second figure

**Then**
- Both figures are selected

---

## 4. Drag Selection (Figure Interaction)

**Given**
- A figure exists on the canvas

**When**
- The user clicks inside the figure

**Then**
- The figure becomes selected

---

## 5. Handle Interaction

**Given**
- A figure is selected

**When**
- The user clicks on a handle

**Then**
- The figure remains selected
- Handle interaction is triggered via tracker

---

# JGiven Structure

## GivenStage

Responsible for setting up the test environment:

- Drawing creation
- Figure creation
- Selection tool activation

## WhenStage

Responsible for simulating user actions:

- Clicking figures
- Shift-click selection
- Clicking empty canvas
- Clicking inside figures

## ThenStage

Responsible for assertions using AssertJ:

- Selection state validation
- Multi-selection validation
- Deselection validation

---

# AssertJ Usage

All assertions are written using AssertJ:

```java id="assert1"
assertThat(drawingView.isFigureSelected(figure))
        .isTrue();

assertThat(drawingView.getSelectedFigures())
        .hasSize(1);

assertThat(drawingView.getSelectedFigures())
        .isEmpty();