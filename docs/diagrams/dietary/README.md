# Dietary restriction UML diagrams

These sources support [Applying dietary restrictions](../../DeveloperGuide.md#applying-dietary-restrictions).
The separate [Viewing dietary preferences](../../DeveloperGuide.md#viewing-dietary-preferences)
section continues to use its existing `viewing.puml` and `viewing.svg` unchanged.

| Source | Embedded figure | Scope |
| --- | --- | --- |
| `classes.puml` | Figure 6 | Selected stored references used by the update command. |
| `repair.puml` | Figure 7 | Construct replacements without changing the active plan. |
| `execution.puml` | Figure 8 | Rebuild existing groceries and publish the repaired state. |
| `viewing.puml` | Figure 11 | Existing read-only view command. |

The update section uses one structural view and two focused sequence excerpts. Parsing,
confirmation, and errors are explained in prose and tables. The former architecture,
parsing, and invalid-input diagrams are retired because they repeated those explanations.
Figure numbers outside this section are left unchanged.

## Editing and regenerating

Use Java 25 and a local [PlantUML JAR](https://plantuml.com/download).
From the repository root in PowerShell:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/dietary/*.puml"
java -Djava.awt.headless=true -jar $env:PLANTUML_JAR -tsvg -o ../../images/dietary "docs/diagrams/dietary/*.puml"
```

Output paths are relative to the source directory. Commit both source and corresponding SVG.
The committed update SVGs use explicit layouts for predictable reading size; PlantUML
regeneration may arrange the same interactions differently. Inspect the result at the
guide's normal reading width and adjust spacing or line breaks before replacing an SVG.

Keep the update diagrams within approximately 700 pixels of width with text at least 16 pixels
after scaling. Omit irrelevant calls and use `...` for abbreviated arguments. Solid arrows
are calls, dashed arrows are returns, and a new object's lifeline starts at its constructor.
Class references use navigable associations and target multiplicities; the class view
uses `hide circle` to suppress PlantUML's C/I symbols. Do not add those symbols to SVGs.

Check calls, order, guards, and stored references against the implementation. The repair
and publication sequences are consecutive excerpts of a successful confirmed update with
existing groceries. Compatibility checks, Optional operations, and formatting are condensed.
Other execution branches remain in the guide's outcome table.
