# Recipe editing UML diagrams

These sources document the implemented `edit-recipe` command in
[the Developer Guide](../../DeveloperGuide.md#editing-a-recipe).

| Source | View |
| --- | --- |
| `classes.puml` | Selected classes, immutable edit data, and stored references |
| `parsing.puml` | Valid input, shared validation helpers, and command construction |
| `execution.puml` | Current recipe lookup, invalid positions, and replacement after formatting |
| `invalid-input.puml` | Rejected input, exception wrapping, and console recovery |

All diagrams use the existing shared style in `../shortfall/style.iuml`. SVGs live under
`docs/images/edit-recipe` so the guide displays without a PlantUML plugin.

## Regenerating

Use Java 25 and a local PlantUML JAR. These images use PlantUML **1.2026.9**, LGPL distribution.
From the repository root, set `PLANTUML_JAR` to the renderer path and run:

```powershell
java -version
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/edit-recipe/*.puml"
java "-Djava.awt.headless=true" -jar $env:PLANTUML_JAR -tsvg -o ../../images/edit-recipe "docs/diagrams/edit-recipe/*.puml"
```

The output path is relative to each source directory. The class diagram uses the built-in Smetana
layout engine, so no separate Graphviz installation is needed. PlantUML is a documentation tool,
not an application dependency.

Keep sources and rendered SVGs together when updating the design. Check depicted methods, message
order, guards, and relationships against the implementation, then inspect every rendered image.
Notes explain condensed validation, getter calls, formatting, and command dispatch.

The class diagram distinguishes stored references from temporary dependencies. In particular,
the command retains `RecipeChanges`, but receives `RecipeBook` only as an execution parameter.
The recipe book's ordered list can contain repeated references, while normal recipe insertion
and editing choose entries by displayed position. Recipe and changes ingredient lists are
copied and read-only; a changes list may be empty to mean unchanged, while a recipe's may not.
