<!-- @@author Rixte -->
# Plan confirmation UML diagrams

These focused views support [the Developer Guide](../../../DeveloperGuide.md#confirming-plan-changes).

| Source | Scope |
| --- | --- |
| `execution.puml` | Shared acceptance, cancellation, and no-question flow. |

Other validation and failure paths are covered by the guide's text and tables.
The SVGs are stored under `docs/images/rixte/confirm-plan`.

Follow the shared [course notation and review guidance](../README.md#course-notation-and-review).

## Regenerating

Use Java 25 and [PlantUML](https://plantuml.com/download) **1.2026.9**.
From the repository root in PowerShell, with `PLANTUML_JAR` set to the renderer path:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/rixte/confirm-plan/*.puml"
java '-Djava.awt.headless=true' -jar $env:PLANTUML_JAR -tsvg -o ../../../images/rixte/confirm-plan "docs/diagrams/rixte/confirm-plan/*.puml"
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
