## BDD Scenarios — Align Figures

**User Story:**
As a user, I want to align multiple selected figures to a 
common edge or center axis so that I can precisely organize elements in my drawing 
without repositioning each figure manually.

---

**Scenario 1 — Align to north edge**
- Given: the user has selected two figures at different vertical positions
- When: the user performs the align north action
- Then: both figures are translated to the top edge of the selection bounds

**Scenario 2 — Align to west edge**
- Given: the user has selected two figures at different horizontal positions
- When: the user performs the align west action
- Then: both figures are translated to the left edge of the selection bounds

**Scenario 3 — Center horizontally**
- Given: the user has selected a figure offset from the horizontal center
- When: the user performs the align horizontal center action
- Then: the figure is translated to the horizontal center of the selection bounds

**Scenario 4 — Non-transformable figure is skipped**
- Given: the user has selected a locked (non-transformable) figure
- When: the user performs any align action
- Then: the figure is not moved