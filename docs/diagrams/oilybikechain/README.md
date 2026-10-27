# Grocery and nutrition contribution UML

These six diagrams document oilybikechain's implementation contributions in the
[Developer Guide](../../DeveloperGuide.md#contribution-history-oilybikechain).
The guide's commit table links each contribution to its original non-merge commit.
Diagrams describe the current implementation; teammates' classes appear only as
integration context. Earlier implementations on other branches are not additional features.

| Source | UML view | Contribution |
| --- | --- | --- |
| `grocery-classes.puml` | Class diagram | Ingredient aggregation and grocery command integration (`667d642`, `6b960ad`). |
| `nutrition-classes.puml` | Class diagram | Combined totals and target-aware viewing (`4c82e81`, `f81dc10`). |
| `grocery-execution.puml` | Sequence diagram | Parsing, execution-time plan lookup, aggregation, and snapshot publication (`6b960ad`). |
| `daily-totals.puml` | Sequence diagram | Four daily sums and construction of the combined result (`4c82e81`). |
| `excess-warnings.puml` | Sequence diagram | Saved targets, selected days, and exact excess warnings (`f81dc10`). |
| `diagnostic-recovery.puml` | Sequence diagram | Exception cause preservation and recoverable invalid input (`ba80853`). |

`c3f6e95` supplies grocery aggregation tests and Javadoc, rather than another runtime
component. Commit `995af37` supplies the existing contribution documentation.

## Rendering

Use Java 25 and PlantUML **1.2026.9**, LGPL distribution, matching the existing
shortfall diagrams. Set `PLANTUML_JAR` to the local renderer path, then run from
the repository root:

```powershell
java -version
java -jar $env:PLANTUML_JAR -version
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/oilybikechain/*.puml"
java "-Djava.awt.headless=true" -jar $env:PLANTUML_JAR -tsvg -o ../../images/oilybikechain "docs/diagrams/oilybikechain/*.puml"
```

Class diagrams select PlantUML's built-in Smetana layout engine, so Graphviz is
not required. All sources include `../shortfall/style.iuml` for the existing
font, colors, and sequence styling; keep that relative path when copying sources.
Output directories are relative to the source files. Commit SVGs alongside their
sources so the guide renders without a PlantUML plugin or remote service.
The renderer belongs in a local tooling or ignored build directory, not in the
application dependencies.

## Modeling and maintenance

- Solid navigable associations represent stored references; dashed dependencies
  represent temporary use, creation, or supplier access. Interface realization
  uses a dashed line with an unfilled triangle. Plain associations avoid implying
  exclusive ownership of shared recipe, ingredient, and day objects.
- Reference roles have target multiplicities. Ordered, nonunique collection ends
  describe the model API; ordinary generated plans additionally enforce the
  supported day count and three distinct recipe entries per day. Optional grocery
  snapshots distinguish absence from a present list with zero elements.
- Supplier bindings represent the existing Java generic interface, not new
  interfaces. Their dependencies on `ActiveMealPlan` describe the lambdas wired
  by the default shared-state parser; custom suppliers need not read that state.
- Sequence diagrams use synchronous calls, dashed returns, activation bars, and
  creation messages starting new lifelines. Constructor returns are omitted.
  Underlined `MishMash` labels denote class-level application behavior. Notes
  explain omitted dispatch, loops, standard-library operations, and formatting.
- `DayPlan`'s combined API returns a new `NutritionTotals` after four separate
  recipe sums. The viewer independently sums `MealSummary` values. Preserve this
  distinction; there is no viewer-to-`DayPlan` call for warning calculation.
- Keep strict target comparisons separate from the generator's tolerance. Resolve
  supplier values at execution time, check an invalid selected day before reading
  targets, and publish grocery snapshots only after aggregation completes.

After implementation changes, compare all depicted fields, method names, call
orders, guards, exception causes, and multiplicities with source. Run syntax
checks, regenerate SVGs, and inspect layout and text readability. Syntax checks
alone cannot establish behavioral accuracy. No Java implementation changes are
needed to maintain these documentation assets.
