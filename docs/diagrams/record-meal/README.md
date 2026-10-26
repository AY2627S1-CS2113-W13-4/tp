# Meal recording UML diagrams

These sources document the `record-meal` and `view-meals` enhancement in
[the Developer Guide](../../DeveloperGuide.md#recording-meals-and-tracking-nutrition).

| Source | View |
| --- | --- |
| `classes.puml` | Selected classes, references, and multiplicities |
| `record-meal.puml` | Parsing and execution of `record-meal`, including the invalid-index branch |
| `view-meals.puml` | Execution of `view-meals`, including the empty-log branch |

`style.iuml` contains shared visual settings. Generated SVGs are stored under
`docs/images/record-meal` and embedded directly in the guide, as for the other features.

## Regenerating

Use Java 25 and the [official PlantUML distribution](https://plantuml.com/download), or the
PlantUML Integration plugin in IntelliJ (save each preview as SVG with the same name as its
source). From the repository root, with `PLANTUML_JAR` set to the renderer path:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/record-meal/*.puml"
java -Djava.awt.headless=true -jar $env:PLANTUML_JAR -tsvg -o ../../images/record-meal "docs/diagrams/record-meal/*.puml"
```

The class diagram selects the built-in Java Smetana layout engine, so no separate Graphviz
installation is required.

After changing the code, check every depicted method, message order, association, and guard
against the implementation, then render again and inspect the images.
