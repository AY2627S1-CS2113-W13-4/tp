<!-- @@author Rixte -->
# Nutrition shortfall UML diagrams

These focused views support [the Developer Guide](../../../DeveloperGuide.md#checking-nutrition-shortfalls).

| Source | Scope |
| --- | --- |
| `classes.puml` | Current plan, request-specific targets, and planned days. |
| `execution.puml` | Per-day comparison with one representative nutrient lookup. |

Other validation and failure paths are covered by the guide's text and tables.
The SVGs are stored under `docs/images/rixte/shortfall`.

Follow the shared [course notation and review guidance](../README.md#course-notation-and-review).

## Regenerating

Use Java 25 and [PlantUML](https://plantuml.com/download) **1.2026.9**.
From the repository root in PowerShell, with `PLANTUML_JAR` set to the renderer path:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/rixte/shortfall/*.puml"
java '-Djava.awt.headless=true' -jar $env:PLANTUML_JAR -tsvg -o ../../../images/rixte/shortfall "docs/diagrams/rixte/shortfall/*.puml"
```

The output path is relative to the source directory. Sources share
`readable.iuml`, which hides class/interface spots with `hide circle`.
The older `shortfall/style.iuml` remains for other contributors' diagrams.
Class layouts use Smetana, without a separate Graphviz dependency.

Check depicted references, method names, call order, and guards against the Java code.
Sequence calls use filled arrowheads and activations; returns are dashed. Omitted and
condensed implementation details are stated in the guide captions.
Keep text readable at normal guide width, check for overlap, and update source and SVG together.
<!-- @@author -->
