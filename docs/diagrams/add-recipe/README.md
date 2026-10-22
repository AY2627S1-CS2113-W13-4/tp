# Add-recipe UML diagrams

These sources document the implemented `add-recipe` feature in
[the Developer Guide](../../DeveloperGuide.md#adding-a-recipe).

| Source | View |
| --- | --- |
| `architecture.puml` | Feature-level architecture and dependencies |
| `classes.puml` | Selected classes, references, and multiplicities |
| `parsing.puml` | Successful validation, parsing, and object creation |
| `execution.puml` | Confirmation preparation, recipe insertion, and the next prompt |
| `invalid-input.puml` | Validation failure, exception wrapping, and CLI recovery |

The five `.puml` files are self-contained and use PlantUML's default styling.
They do not include `style.iuml`, so each complete source can be pasted into an
online renderer without uploading additional files. `style.iuml` is retained
as a reference copy of the shortfall settings; it is not loaded by these diagrams.

Generated SVGs belong under `docs/images/add-recipe` and are embedded directly
in the guide so the images display without a website plugin or browser extension.

## Regenerating

Use Java 25 and the [official PlantUML distribution](https://plantuml.com/download).
The shortfall diagrams use PlantUML **1.2026.9**, LGPL distribution; use the
same renderer version for consistent output. PlantUML is a documentation tool,
not an application dependency.

From the repository root, set `PLANTUML_JAR` to the local renderer path, then run:

```powershell
java -version
java -jar $env:PLANTUML_JAR -checkonly "docs/diagrams/add-recipe/*.puml"
java -Djava.awt.headless=true -jar $env:PLANTUML_JAR -tsvg -o ../../images/add-recipe "docs/diagrams/add-recipe/*.puml"
```

The output directory is relative to each source file. Architecture and class
diagrams use the built-in Smetana layout engine, so a separate Graphviz
installation is not required. For an online preview, paste the entire `.puml`
file from `@startuml` to `@enduml`; no include file is needed.

Commit the generated SVGs alongside their sources after inspecting all five
images. Rendering has not been run in the preparation environment.

## Modeling conventions and maintenance

Follow the course's
[Modeling (Using UML) chapter](https://nus-cs2113-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html).
The architecture diagram groups existing responsibilities; it does not
introduce Java components or new interfaces.

The class diagram uses navigable associations for stored references and
dependencies for temporary objects and parameter use. Interface implementation
is shown as realization. Association ends carry private role names and
target-end multiplicities; reverse multiplicities are left unspecified.
Ordered, nonunique list ends allow repeated references. Stored references are
shown once, without duplicating association ends as attributes. Plain
associations avoid claiming exclusive ownership of shared objects.

Sequence diagrams show synchronous calls, dashed returns, nested activations,
and constructor messages starting new lifelines. Constructor returns are
consistently omitted. The underlined MishMash participant and explicitly shown
static self-calls denote class-level application behavior. Exception messages
are labelled explicitly. Notes identify condensed dispatch, formatting,
logging, and standard-library details.

After changing code, check every depicted method, constructor, call order,
reference, multiplicity, and failure path against the implementation. Render
the diagrams and inspect the images; structural and syntax checks alone do
not establish that a diagram accurately represents the code. Update sources
and generated images together.
