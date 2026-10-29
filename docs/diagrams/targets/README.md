# Daily targets UML diagrams

These sources document the implemented `set-targets` and `view-targets` commands and the way
`generate-plan` uses the saved targets, in
[the Developer Guide](../../DeveloperGuide.md#setting-and-viewing-daily-targets).

| Source | View |
| --- | --- |
| `classes.puml` | Selected classes, including the shared `TargetSettings` |
| `set-targets.puml` | Parsing `set-targets` and updating the saved targets |
| `generate-plan.puml` | How `generate-plan` merges typed and saved targets |

All diagrams use the shared style in `../rixte/shortfall/style.iuml`. SVGs live under
`docs/images/targets` so the guide displays without a PlantUML plugin.

## Regenerating

Use Java 25 and a local PlantUML JAR (the same one used for the other diagrams, or the PlantUML
Integration plugin in IntelliJ, saving each preview as SVG with the same name as its source).
From the repository root, set `PLANTUML_JAR` to the renderer path and run:

```powershell
java -version
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/targets/*.puml"
java "-Djava.awt.headless=true" -jar $env:PLANTUML_JAR -tsvg -o ../../images/targets "docs/diagrams/targets/*.puml"
```

The output path is relative to each source directory. The class diagram uses the built-in Smetana
layout engine, so no separate Graphviz installation is needed.

Keep sources and rendered SVGs together when updating the design. Check depicted methods, message
order, and decision branches against the implementation, then inspect every rendered image.
