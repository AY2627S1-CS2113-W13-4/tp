# Samantha Sam - Project Portfolio Page

**GitHub:** [Rixte](https://github.com/Rixte) | **Draft updated:** 25 October 2026 (To be continued)

## Overview

MishMash is a Java command-line application for meal-prep planning. Users can manage recipes, generate meal plans and grocery lists, and compare planned nutrition with daily targets.

## Summary of Contributions

**Code contributed:** [My individual tP Code Dashboard report](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=rixte&breakdown=true).

### Enhancements implemented

- **Daily nutrition shortfall checking - merged.** I implemented `check-shortfall` to report each day's calorie and macronutrient deficits against user-supplied targets. This helps users identify meals that need adjustment. The command reuses existing nutrition models, checks days independently, and uses long arithmetic to prevent integer overflow. It preserves existing plans and groceries, supports omitted targets, and provides recoverable validation errors. Evidence: [#65](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/65), [#68](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/68).
- **Recipe editing - implemented, awaiting merge.** I implemented `edit-recipe` so users can update selected fields without re-entering a whole recipe. Supplied ingredients replace the complete ingredient list. The design uses immutable changes, execution-time recipe lookup, and replacement after validation and confirmation formatting. Existing plans retain their original recipes; regeneration uses the edits. This required checking interactions with dietary filtering, nutrition warnings, and grocery snapshots. Code, tests, and documentation are submitted in [#76](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/76).
- **Meal-plan and grocery-list viewing - merged.** I added the viewers and command handling for `view-plan`, optional day selection, and `view-list`. They display meals, daily nutrition totals, or generated groceries, with guidance for missing data. Viewing reads the latest supplied data without generating or modifying it. Evidence: [#15](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/15), [#23](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/23).

### Testing and code quality

I added regression tests for viewing, validation boundaries, large nutrition totals, repeated meals, command reuse, and recovery after errors. This includes 144 additional shortfall test cases in [#68](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/68) and 57 additional editing cases in [#76](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/76). I also improved null checks, exception-cause preservation, assertions, and unexpected-failure logging in [#35](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/35).

### Contributions to documentation

- **User Guide:** I created the shared guide structure and contributed setup instructions, command navigation, and viewing documentation in [#27](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/27). I documented shortfall syntax, limits, error recovery, and verified examples in [#65](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/65) and [#66](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/66). Recipe-editing instructions are included in [#76](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/76).
- **Developer Guide:** I documented shortfall responsibilities, design alternatives, and verification, with [five UML diagrams](../diagrams/shortfall/README.md): architecture, classes, parsing, execution, and invalid-input recovery. For editing, [#76](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/76) adds design explanations and four diagrams: classes, parsing, execution, and invalid-input recovery. Diagram sources and rendered SVGs are supplied together.

### Team tasks and review contributions

- I introduced application logging and package organisation in [#31](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/31), then standardised Java conventions, Javadocs, helper methods, and Checkstyle enforcement in [#33](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/33).
- I restored teammates' original RepoSense attribution after refactoring, using bounded author annotations checked against the original commits: [#48](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/48).
- I reviewed and merged teammates' nutrition-warning and guide updates. My reviews checked test results, documented examples, and attribution: [#74 review](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/74#pullrequestreview-5637936219), [#75 review](https://github.com/AY2627S1-CS2113-W13-4/tp/pull/75#pullrequestreview-5637959754). These are review and integration contributions; the feature implementations belong to their authors.

<!-- Before final submission: refresh pending PR status and add any verified beyond-team contributions. -->
