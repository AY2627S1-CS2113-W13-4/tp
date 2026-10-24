# Recipe editing sequence diagram

`execution.puml` shows parsing and successful execution of a partial recipe edit. It uses the shared visual style in `../shortfall/style.iuml`.

Render with Java 25 and a local PlantUML JAR:

```text
java -jar /path/to/plantuml.jar -tsvg -o ../../images/edit-recipe docs/diagrams/edit-recipe/execution.puml
```

The output path is relative to the source directory. Keep the editable source and `docs/images/edit-recipe/execution.svg` together when updating the design. PlantUML is a documentation tool and is not an application dependency.
