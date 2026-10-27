# Dietary restriction UML diagrams

These sources support [Applying dietary restrictions](../../DeveloperGuide.md#applying-dietary-restrictions).

| Source | View |
| --- | --- |
| `architecture.puml` | Feature-level dependencies |
| `classes.puml` | Selected classes and stored references |
| `parsing.puml` | Valid profile parsing and command creation |
| `execution.puml` | Successful partial repair and grocery rebuilding |
| `viewing.puml` | Execution-time profile viewing without mutation |
| `invalid-input.puml` | Unsupported diet, error wrapping, and recovery |

The diagrams follow the existing add-recipe sources, without `!include style.iuml`.
Class and architecture diagrams use Smetana. They do not require Graphviz.

## Regenerating

Use Java 25 and a local [PlantUML JAR](https://plantuml.com/download).
From the repository root in PowerShell:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/dietary/*.puml"
java -Djava.awt.headless=true -jar $env:PLANTUML_JAR -tsvg -o ../../images/dietary "docs/diagrams/dietary/*.puml"
```

Output paths are relative to the source directory. The expected images are
`architecture.svg`, `classes.svg`, `parsing.svg`, `execution.svg`, `viewing.svg`, and `invalid-input.svg`
under `docs/images/dietary`.

Inspect all rendered diagrams, then replace the corresponding PlantUML source link in the Developer Guide
with a Markdown image pointing to `images/dietary/NAME.svg`. No SVG files are included in this update.
Commit the inspected SVGs and guide links together.

The sequences deliberately condense constructor internals and library calls. The execution
sequence covers successful repair with existing groceries; other branches are explained in
the guide. Check every depicted method, guard, relationship, and message order against code
before publishing. Syntax validation alone cannot establish implementation accuracy.

