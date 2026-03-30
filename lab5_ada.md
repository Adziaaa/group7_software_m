1. Provide examples of the SOLID principles in context of the CASE study.
2. Explain Clean Architecture in context of the CASE Study.


What is Clean Architecture?
Clean Architecture is a design approach introduced by Robert C. Martin that organizes software into layers with strict dependency rules.
👉 The key idea:
Inner layers (business logic) should not depend on outer layers (frameworks, UI, databases).

What is a Case Study?
A case study is:
A real or simulated system/project that you analyze, design, and improve using software engineering principles.

Here is a **short, direct, submission-ready version** 👇

---

# **1. Provide examples of the SOLID principles in the CASE study (JHotDraw)**

* **SRP (Single Responsibility Principle)**

  * `Figure` → represents only drawing objects (no UI logic)
  * `DrawingView` → handles display and selection
  * `DrawingEditor` → coordinates tools and views

* **OCP (Open/Closed Principle)**

  * `Figure` is extended by `RectangleFigure`, `EllipseFigure`
  * New shapes can be added without modifying existing code

* **LSP (Liskov Substitution Principle)**

  * `RectangleFigure`, `EllipseFigure`, `AbstractCompositeFigure` can all be used as `Figure`
  * Code works with `Figure` without knowing concrete type

* **ISP (Interface Segregation Principle)**

  * `URIChooser` is a small interface
  * `Application` depends only on this, not full `JFileChooser`

* **DIP (Dependency Inversion Principle)**

  * `Application` depends on `URIChooser` (abstraction)
  * `JFileURIChooser` is a concrete implementation
  * Core logic depends on interfaces like `Figure`, `Tool`

---

# **2. Explain Clean Architecture in the CASE study (JHotDraw)**

* JHotDraw is **not pure Clean Architecture**, but follows similar ideas

* **Layer-like structure (modules):**

  * `jhotdraw-core` → core logic (`Figure`, `Tool`, `DrawingEditor`)
  * `jhotdraw-api` → interfaces (`Application`, `URIChooser`)
  * `jhotdraw-gui` → UI (Swing implementations)

* **Separation of concerns:**

  * Model → `Figure`, `Drawing`
  * View → `DrawingView`
  * Controller → `DrawingEditor`

* **Dependency direction:**

  * High-level code depends on abstractions
  * Example: `Application` → `URIChooser` (not Swing directly)

* **Key Clean Architecture idea:**

  * Core logic is independent from UI
  * Example: `Figure` must NOT depend on `DrawingView` or `Tool`

---

**Conclusion (short):**
JHotDraw uses interfaces and modular structure to separate core logic from UI. This reduces coupling and supports safe changes during actualization.
