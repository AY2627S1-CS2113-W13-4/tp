<!-- @@author Rixte -->
# Reset-plan UML diagrams

These sources document [resetting a meal plan](../../DeveloperGuide.md#resetting-a-meal-plan).

| Source | Purpose |
| --- | --- |
| `classes.puml` | Reset command dependencies; retained session data is described in the DG |
| `parsing.puml` | Valid parsing without changing the active plan |
| `execution.puml` | Execution after agreement, missing-plan behaviour, and recoverable failures |
| `invalid-input.puml` | Rejected arguments followed by the normal parsing and confirmation workflow |

The checked-in SVGs are stored under `docs/images/reset-plan`.

Use Java 25 and the same local PlantUML 1.2026.9 renderer as the other diagrams:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/reset-plan/*.puml"
java '-Djava.awt.headless=true' -jar $env:PLANTUML_JAR -tsvg -o ../../images/reset-plan "docs/diagrams/reset-plan/*.puml"
```

The output directory is relative to the source file. PlantUML is a documentation tool,
not an application dependency.
<!-- @@author -->
