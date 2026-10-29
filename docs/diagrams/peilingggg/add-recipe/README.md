# Add-recipe UML diagrams

These sources support [Adding a recipe](../../../DeveloperGuide.md#adding-a-recipe).
The feature uses one compact structural view and two focused sequence diagrams.

| Source | Figure | Scope |
| --- | --- | --- |
| `classes.puml` | 1 | Selected stored recipe references. |
| `parsing.puml` | 2 | Successful validation and recipe/command creation. |
| `execution.puml` | 3 | Format confirmation before insertion. |

The former architecture and invalid-input diagrams are retired. The responsibilities
table replaces the overlapping architecture view; errors and recovery are covered in
the guide's input table. Figure numbers in other sections are unchanged. `style.iuml`
is an unused reference; none of these sources includes it.

## Editing and regenerating

Use Java 25 and a local [PlantUML JAR](https://plantuml.com/download). From the repository
root in PowerShell:

```powershell
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/add-recipe/*.puml"
java -Djava.awt.headless=true -jar $env:PLANTUML_JAR -tsvg -o ../../images/add-recipe "docs/diagrams/add-recipe/*.puml"
```

The output directory is relative to each source file. The class diagram uses Smetana,
so it needs no separate Graphviz installation. Each source is self-contained.

The committed SVGs use explicit layouts for predictable reading size. PlantUML may
arrange the same relationships and interactions differently. Inspect regenerated
images at normal guide width before replacing them. Keep each image within about
700 pixels, use text at least 16 pixels after scaling, and commit source and SVG together.

## Course conventions and maintenance

Follow the course's [modeling chapter](https://nus-cs2113-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html).

- Class associations use open navigability heads and target multiplicities. Stored
  references are not repeated as attributes. Collection order and repeated entries
  are explained in the guide; plain associations permit sharing. Members, temporary
  dependencies, and interface realization are deliberately omitted. `hide circle`
  suppresses C/I symbols.
- Sequence calls use filled arrowheads; returns are dashed. Object names are not
  underlined. Creation arrows reach object boxes, with constructor activations below
  the boxes. Every expanded call has an activation ending at its return.
- Folded-corner notes and message labels must leave activation bars visible. The
  parsing note summarizes dispatch, ingredient construction, and validation. Its
  constructor arrows show only `Recipe` and `AddRecipeCommand` creation. The execution
  sequence omits the inherited confirmation check, formatting internals, logging,
  and application printing. These omissions are also stated in the guide captions.

Check depicted fields, multiplicities, methods, and call order against the code. In
particular, formatting must finish before `RecipeBook.add`, and parsing must not
modify the recipe book. Syntax and layout checks do not establish code accuracy.
