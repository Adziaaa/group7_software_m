# JUnit 4 Unit Testing and Verification for JavaxDOMInput

1. Maven Dependency for JUnit 4

---

JUnit 4 and Mockito dependencies are included in jhotdraw-xml/pom.xml for unit testing and mocking.

2. JUnit 4 Test Class Creation

---

A test class, JavaxDOMInputTest.java, is present in src/test/java/org/jhotdraw/xml/. It uses JUnit 4 annotations (@Test, @Before) and Mockito for mocking dependencies.

3. Tested Methods and Scenarios

---

The following methods of JavaxDOMInput are directly tested:

- getElementCount() — tested for empty and non-empty child node lists.
- openElement(int) — tested for valid and out-of-bounds indices.

Each test uses a minimal valid XML InputStream and reflection to set the internal 'current' node to a mock Element, allowing isolated unit testing of the methods.

4. Best-Case Scenario Tests

---

- testGetElementCount_Empty: Verifies getElementCount() returns 0 when there are no child elements.
- testGetElementCount_WithElements: Verifies getElementCount() returns the correct count when child elements are present.
- testOpenElement_ValidIndex: Verifies openElement(0) sets the current node to the correct child when a valid index is provided.

5. Boundary Case Tests

---

- testOpenElement_IndexOutOfBounds: Verifies openElement(0) throws IllegalArgumentException when there are no child elements.

6. Use of Mocks/Stubs

---

Mockito is used to mock DOMFactory, Document, Element, and NodeList. Reflection is used to set the private 'current' field for precise test control.

7. Java Assertions for Invariants

---

JUnit assertions (assertEquals, assertSame, fail) are used to verify expected results and invariants. No Java assert statements are present in the code, but all critical conditions are checked via JUnit assertions.

8. Running Tests in Maven

---

Run all tests with:
mvn test
Results are shown in the terminal and in target/surefire-reports.

## Summary Table:

| Method            | Best-Case | Boundary | Invariants | Mocked Dependencies |
| ----------------- | --------- | -------- | ---------- | ------------------- |
| getElementCount() | Yes       | Yes      | Yes        | Yes                 |
| openElement(int)  | Yes       | Yes      | Yes        | Yes                 |

This ensures robust, maintainable, and well-documented unit testing for the tested features of JavaxDOMInput.
