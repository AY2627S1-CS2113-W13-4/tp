<!-- @@author Rixte -->
# Plan confirmation sequences

The shared confirmation workflow is split into three small sequence diagrams:

| Source | Purpose |
| --- | --- |
| `decision.puml` | Syntax validation, current-state lookup, and whether a question is needed |
| `execution.puml` | An accepted answer followed by command execution |
| `cancellation.puml` | A rejected answer or EOF without command execution |

The matching rendered SVGs are stored in `docs/images/confirm-plan`.

The sources use `../shortfall/readable.iuml` for consistent text sizing.

Render with Java 25 and PlantUML 1.2026.9:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/confirm-plan/*.puml"
java '-Djava.awt.headless=true' -jar $env:PLANTUML_JAR -tsvg -o ../../images/confirm-plan "docs/diagrams/confirm-plan/*.puml"
```

The existing reset execution diagram shows the operation after agreement. These diagrams supply
the preceding interaction, including cancellation and commands requiring no answer.
<!-- @@author -->
