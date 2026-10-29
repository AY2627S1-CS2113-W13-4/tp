# Recipe listing UML diagrams

These sources document the implemented `list-recipes` command in
[the Developer Guide](../../DeveloperGuide.md#listing-recipes).

| Source | View |
| --- | --- |
| `classes.puml` | Selected classes, the nullable index, and stored references |
| `execution.puml` | Parsing and execution of `list-recipes 2`, including the error path |

Both diagrams use the shared style in `../rixte/shortfall/style.iuml`. SVGs live under
`docs/images/list-recipes` so the guide displays without a PlantUML plugin.

## Regenerating

Use Java 25 and a local PlantUML JAR (the same one used for the other diagrams, or the PlantUML
Integration plugin in IntelliJ, saving each preview as SVG with the same name as its source).
From the repository root, set `PLANTUML_JAR` to the renderer path and run:

```powershell
java -version
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/list-recipes/*.puml"
java "-Djava.awt.headless=true" -jar $env:PLANTUML_JAR -tsvg -o ../../images/list-recipes "docs/diagrams/list-recipes/*.puml"
```

The output path is relative to each source directory. The class diagram uses the built-in Smetana
layout engine, so no separate Graphviz installation is needed. PlantUML is a documentation tool,
not an application dependency.

Keep sources and rendered SVGs together when updating the design. Check depicted methods, message
order, and guards against the implementation, then inspect every rendered image. The sequence
diagram condenses command dispatch and shows only the indexed form; its closing note describes
the no-index form. The class diagram shows `RecipeBook` as passed to `execute`, not stored by
the command.
