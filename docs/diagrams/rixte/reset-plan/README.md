<!-- @@author Rixte -->
# Reset-plan UML diagrams

These focused views support [the Developer Guide](../../../DeveloperGuide.md#resetting-a-meal-plan).

| Source | Scope |
| --- | --- |
| `execution.puml` | Clear plan-specific state after agreement and verify absence. |
| `lifecycle.puml` | Stable states for absent plans, plans without groceries, and plans with generated groceries. |

Other validation and failure paths are covered by the guide's text and tables.
The SVGs are stored under `docs/images/rixte/reset-plan`.

Follow the shared [course notation and review guidance](../README.md#course-notation-and-review).

## Regenerating

Use Java 25 and [PlantUML](https://plantuml.com/download) **1.2026.9**.
From the repository root in PowerShell, with `PLANTUML_JAR` set to the renderer path:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/rixte/reset-plan/*.puml"
java '-Djava.awt.headless=true' -jar $env:PLANTUML_JAR -tsvg -o ../../../images/rixte/reset-plan "docs/diagrams/rixte/reset-plan/*.puml"
```

The output path is relative to the source directory. Sources share
`../shortfall/readable.iuml`, which hides class/interface spots with `hide circle`.
The older `shortfall/style.iuml` remains for other contributors' diagrams.
Class layouts use Smetana, without a separate Graphviz dependency.

Check depicted references, method names, call order, and guards against the Java code.
Sequence calls use filled arrowheads and activations; returns are dashed. Omitted and
condensed implementation details are stated in the guide captions.
Keep text readable at normal guide width, check for overlap, and update source and SVG together.
<!-- @@author -->
