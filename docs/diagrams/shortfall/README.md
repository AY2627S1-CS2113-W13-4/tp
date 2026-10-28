# Shortfall UML diagrams

These sources document the implemented `check-shortfall` enhancement in
[the Developer Guide](../../DeveloperGuide.md#checking-nutrition-shortfalls).

| Source | View |
| --- | --- |
| `architecture.puml` | Feature-level architecture and dependencies |
| `classes.puml` | Command dependencies and retained references |
| `models.puml` | Plan, day, and recipe associations and multiplicities |
| `parsing.puml` | Successful command parsing and object creation |
| `execution.puml` | Current-plan lookup, traversal, and missing-plan branches |
| `comparison.puml` | One day's nutrient comparison, expanded separately |
| `invalid-input.puml` | Validation failure, exception wrapping, and CLI recovery |

`readable.iuml` contains shared visual settings. Generated SVGs are stored under
`docs/images/shortfall` and embedded directly in the guide. They are checked in so the
guide renders on GitHub and a Markdown website without a PlantUML plugin or remote server.

## Regenerating

Use Java 25 and the [official PlantUML distribution](https://plantuml.com/download).
The checked-in images were generated with PlantUML **1.2026.9**, LGPL distribution.
The renderer is a documentation tool and is not an application dependency.

From the repository root, set `PLANTUML_JAR` to the local renderer path, then run:

```powershell
java -version
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/shortfall/*.puml"
java "-Djava.awt.headless=true" -jar $env:PLANTUML_JAR -tsvg -o ../../images/shortfall "docs/diagrams/shortfall/*.puml"
```

The output directory is relative to each source file. Architecture and class diagrams
select the built-in Java Smetana layout engine, so no separate Graphviz installation is
required. See the official [CLI documentation](https://plantuml.com/command-line) and
[layout-engine documentation](https://plantuml.com/layout-engines).

After changing code, check every depicted method, constructor, message order, association,
and guard against the implementation. Render the diagrams and inspect their images;
syntax checking alone cannot detect a diagram that misrepresents the code. Keep the
abstraction notes in the guide when omitting repeated getters or formatting details.

The class diagram uses a typed private attribute for the parser's shared holder and
navigable association ends with private role names and target-end multiplicities for
the remaining stored references. No reference is shown in both forms. Ordered, nonunique
ends represent the model's lists; normal generation
applies stricter day and meal-count rules. Static constants and unrelated members are
omitted rather than represented as instance members.

<!-- @@author Rixte -->
Readability: separate command/model views and traversal/comparison sequences keep each image narrow.
The shared style uses 16-point text; explanatory detail is kept beside the diagrams in the DG.
<!-- @@author -->
