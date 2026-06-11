# Lab 9: Behavior Driven Testing

## 1. Map your User Stories to BDD Given-When-Then Scenarios

**User Story:**
As a content creator, I want to perform basic editing actions such as cut, copy, paste, delete, and duplicate elements so that I can efficiently modify and organize my design.

**BDD Scenarios:**

**Cut Action**

- Given: User has selected an element
- When: User performs cut action
- Then: Element removed from drawing and stored in clipboard

**Copy Action**

- Given: User has selected an element
- When: User performs copy action
- Then: Element remains in drawing and stored in clipboard

**Paste Action**

- Given: Clipboard contains an element
- When: User performs paste action
- Then: New copy added to drawing

**Delete Action**

- Given: User has selected an element
- When: User performs delete action
- Then: Element removed from drawing

**Duplicate Action**

- Given: User has selected an element
- When: User performs duplicate action
- Then: Copy created in drawing

---

## 2. Use JGiven to automate your BDD Scenarios

Test class: `jhotdraw-actions/src/test/java/org/jhotdraw/action/bdd/EditingActionsScenarioTest.java`

Stage classes: `jhotdraw-actions/src/test/java/org/jhotdraw/action/bdd/stages`

- `GivenStage.java` - Preconditions
- `WhenStage.java` - User actions
- `ThenStage.java` - Assertions

Example test:

```java
@Test
public void user_cuts_selected_elements() {
    given().the_user_has_selected_an_element("Rectangle");
    when().the_user_performs_a_cut_action();
    then().the_element_should_be_removed_from_the_drawing("Rectangle")
          .and().the_element_should_be_stored_in_the_clipboard("Rectangle");
}
```

---

## 3. Domain Specific Assertions with AssertJ

AssertJ assertions in `ThenStage.java`:

```java
Assertions.assertThat(drawingElements)
    .as("Drawing should contain the element")
    .contains(elementName);
```

AssertJ-Swing for Swing UI automation:

```java
FrameFixture window = new FrameFixture(mainFrame);
window.button("cutButton").click();
window.label("status").requireText("Element cut");
```
