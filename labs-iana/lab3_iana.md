# Impact Analysis — Undo / Redo

**Change Request / Feature:** Undo / Redo
**Method:** Following the impact-analysis activity diagram (Raj13, Fig. 7.9). Starting from the class located during concept location (`UndoAction`, marked CHANGED), its neighbours were marked NEXT and each was classified as UNCHANGED, PROPAGATES, or CHANGED while building the estimated impact set.

## Packages visited during impact analysis

| Package name | # of classes | Comments |
|---|---|---|
| `org.jhotdraw.action.edit` | 2 | Contains the menu-level `UndoAction` and `RedoAction`. These are the controller entry points; they do not perform undo themselves but delegate to the view's real action registered under `"edit.undo"` / `"edit.redo"`. **CHANGED** — the feature is triggered here. |
| `org.jhotdraw.action` | 1 | Holds `AbstractViewAction`, the superclass that gives the actions access to the active `View` and the `Application`. **PROPAGATES** — a change to how actions resolve the active view would ripple into the undo/redo actions. |
| `org.jhotdraw.api.app` | 2 | Defines the `Application` and `View` interfaces. The `View` owns the action map that stores the real undo/redo action under the ID `"edit.undo"`. **PROPAGATES** — the feature depends on this contract. |
| `org.jhotdraw.undo` | 1 | Contains `UndoRedoManager` (extends `javax.swing.undo.UndoManager`) and its inner `UndoAction` / `RedoAction`. This is the core of the feature: it maintains the undo/redo edit stacks, blocks incoming edits while undo/redo is in progress, and exposes the real actions to the menu. **CHANGED**. |
| `javax.swing.undo` | 2 | JDK classes `UndoManager` and `UndoableEdit`. The actual stack iteration and `undo()`/`redo()` calls happen here. **UNCHANGED** (library code) but visited because the feature depends on it. |
| `org.jhotdraw.util` | 1 | `ResourceBundleUtil`, used to configure localised menu labels (e.g. "Undo", "Redo"). **UNCHANGED** — supporting infrastructure, visited but not impacted. |


What CI is?
"CI" in English most commonly stands for Continuous Integration in software development, referring to the automated testing and merging of code.
Setup a simple CI pipeline.
in git repo maven.yml first was created
then in pom.xml extension was added

<repository>
			<id>github</id>
			<name>GitHub external Packages</name>
			<url>https://maven.pkg.github.com/sweat-tek/MavenRepository</url>
		</repository>
	</repositories>
    this one so test can be scheduled. 
    