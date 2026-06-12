## 6. Verification

### Unit Tests

Unit tests were written using JUnit 4 and Mockito, located at:
`jhotdraw-core/src/test/java/org/jhotdraw/draw/action/AlignActionTest.java`

`DrawingEditor`, `DrawingView`, `Drawing`, and `Figure` are all mocked to isolate 
the alignment logic from Swing and the rest of the framework. Each test calls 
`alignFigures()` directly and uses `ArgumentCaptor` to capture the `AffineTransform` 
passed to each figure, verifying the exact translation values.

| Test | Type | What it verifies |
|---|---|---|
| `testNorth_movesLowerFigureToTopEdge` | Best case | Figure below top edge is translated to y = selectionBounds.y |
| `testNorth_alreadyAligned_translationIsZero` | Boundary | Figure already at top edge receives zero translation |
| `testSouth_movesUpperFigureToBottomEdge` | Best case | Figure above bottom edge is translated down correctly |
| `testWest_movesRightFigureToLeftEdge` | Best case | Figure to the right of left edge is translated left correctly |
| `testEast_movesLeftFigureToRightEdge` | Best case | Figure to the left of right edge is translated right correctly |
| `testHorizontal_centersFigureOnXAxis` | Best case | Figure is centered horizontally within selection bounds |
| `testVertical_centersFigureOnYAxis` | Best case | Figure is centered vertically within selection bounds |
| `testNonTransformableFigure_isNeverTransformed` | Boundary | Non-transformable figure is skipped — `transform()`, `willChange()`, `changed()` never called |

All 10 tests passed locally via:

mvn test -pl jhotdraw-core

### Verification of the Refactoring

The refactoring was verified in two ways:

**Behavioural correctness** — all unit tests pass with the same expected translation 
values as the original implementation. Since `computeTranslation()` in each subclass 
contains exactly the same arithmetic as the original `alignFigures()` override, 
behaviour is preserved by construction.

**Compilation** — `mvn clean install -DskipTests` confirmed the refactored code 
compiles cleanly across all modules with no errors.

**CI** — pushing the feature branch to GitHub triggered the Actions pipeline 
(`.github/workflows/maven.yml`), which built the project and executed the test suite 
automatically, providing an independent green confirmation.

### Acceptance Test

The acceptance criterion from the change request is: selected figures must align 
to the correct edge or center axis when an align action is triggered.

This is directly confirmed by the unit tests — each alignment direction is tested 
with figures at known positions and the resulting translations are asserted to match 
the expected geometry. The non-transformable boundary test additionally confirms 
the action does not attempt to move locked figures.

---

### Review Questions

**1. What is unit testing and why is it used?**
Unit testing verifies individual methods or classes in isolation. It is used to catch 
regressions early, document expected behaviour, and provide confidence that refactoring 
has not broken existing functionality.

**2. Difference between inspection and testing?**
Inspection is a static review of source code by a person — no code is executed. 
Testing executes the code and checks actual output against expected output. Inspection 
finds logical issues and style problems; testing finds runtime failures.

**3. What is regression testing?**
Re-running existing tests after a change to confirm that previously working behaviour 
still works.

**4. What does regression testing prevent?**
It prevents unintentional breakage of existing features when new changes are introduced.

**5. Difference between unit and functional testing?**
Unit testing tests a single method or class in isolation using mocks for dependencies. 
Functional testing tests the system end-to-end from the user's perspective — for 
example, clicking the align button in the UI and observing that figures move correctly.