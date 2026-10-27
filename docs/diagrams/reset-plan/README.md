# Reset-plan UML diagrams

These sources document [resetting a meal plan](../../DeveloperGuide.md#resetting-a-meal-plan).

| Source | Purpose |
| --- | --- |
| `classes.puml` | Ownership, command dependencies, and the session data retained by reset |
| `parsing.puml` | Valid parsing without changing the active plan |
| `execution.puml` | Reset execution and missing-plan behaviour |
| `invalid-input.puml` | Rejected arguments followed by a successful retry |

The checked-in SVGs are stored under `docs/images/reset-plan`.

Use Java 25 and the same local PlantUML 1.2026.9 renderer as the other diagrams:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/reset-plan/*.puml"
java -Djava.awt.headless=true -jar $env:PLANTUML_JAR -tsvg -o ../../images/reset-plan "docs/diagrams/reset-plan/*.puml"
```

The output directory is relative to the source file. PlantUML is a documentation tool,
not an application dependency.
