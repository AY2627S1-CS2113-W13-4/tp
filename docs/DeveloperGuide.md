# MishMash The Meal Prep Planner Developer Guide

MishMash is a command-line meal prep planner for managing recipes, generating meal plans, and preparing grocery lists. This guide describes the current design, project practices, requirements, use cases, and manual tests.

## Acknowledgements

- The project originates from the [NUS CS2113 team-project template](https://github.com/NUS-CS2113-AY2627-S1/tp). The team's shared repository is [AY2627S1-CS2113-W13-4/tp](https://github.com/AY2627S1-CS2113-W13-4/tp).
- [Gradle](https://gradle.org/) provides build automation and dependency management.
- [JUnit Jupiter](https://junit.org/junit5/) provides the automated testing framework.
- [Checkstyle](https://checkstyle.org/) checks Java coding conventions.
- [Shadow](https://gradleup.com/shadow/) packages the application JAR.
- [PlantUML](https://plantuml.com/) generates the UML diagrams from the editable sources under `docs/diagrams/shortfall`.

<!-- TEAM: Add any further sources of reused or adapted code, ideas, and documentation before submission. -->

## Design

### Architecture

The application separates terminal interaction, command handling, application data, and output formatting. The main application coordinates these components throughout a single command session.

| Area | Current responsibility |
| --- | --- |
| Application and user interaction | Display the greeting, read commands, obtain confirmation when required, and show results or errors. |
| Command handling | Recognise commands, validate their arguments, and perform the requested action. |
| Model | Hold recipes, nutrition information, meal plans, and generated groceries. |
| Generation logic | Produce meal plans and consolidate grocery quantities. |
| Display formatting | Present recipes, meal plans, and grocery lists consistently as text. |

Command names and prefixes are case-sensitive. Invalid commands normally produce an error and allow the user to continue. The application accepts one command per line and ends when the user enters `bye` or console input ends.

### Logic Component

The logic covers command parsing, application actions, and generation. It supports the following commands:

| Command | Purpose |
| --- | --- |
| `add-recipe` | Add a recipe with nutrition information and ingredients. |
| `list-recipes` | List stored recipes or display one recipe in full. |
| `delete-recipe` | Remove a recipe by its displayed index after confirmation. |
| `generate-plan` | Generate one to seven days of meals, optionally using daily nutrition targets. |
| `view-plan` | Display the active meal plan or one selected day. |
| `check-shortfall` | Compare each planned day's nutrition with explicit daily targets and report deficits. |
| `generate-list` | Generate groceries for the active meal plan. |
| `view-list` | Display the generated grocery list. |
| `help` | Display the supported commands and their formats. |
| `bye` | End the session. |

Commands validate their arguments and relevant application state. Requests that fail validation leave the existing data unchanged. Generation and viewing are separate actions: viewing does not create a new plan or grocery list.

### Model Component

| Class | Represents |
| --- | --- |
| `Recipe` | A recipe name, calorie and macronutrient values, and ingredient entries. |
| `RecipeBook` | The session's ordered collection of recipes. |
| `GroceryItem` | An ingredient name, decimal quantity, and unit. |
| `DayPlan` | The meals selected for one day and their nutrition totals. |
| `MealPlan` | The ordered days of a generated plan. |
| `NutritionTargets` | Optional daily calorie, protein, carbohydrate, and fat targets. |
| `GeneratePlanRequest` | The requested number of days and nutrition targets. |
| `ActiveMealPlan` | The current meal plan and its generated grocery list, if available. |

There are no separate `Ingredient` or `GroceryList` classes in the current model. Recipe ingredients and generated grocery entries use `GroceryItem`.

Recipes remain in insertion order. Their displayed indexes start at 1 and can change after deletion. Duplicate names are permitted. Existing meal plans remain available when a recipe is subsequently added to or removed from the recipe book.

Only one meal plan is active at a time. Successfully generating a replacement plan clears the groceries associated with the previous plan. Failed generation preserves the previous plan and groceries.

### Storage Component

Persistent application-data storage is not implemented. Recipes, meal plans, and grocery lists are held in memory and are cleared when the application closes. There is no save or load command, save-file format, or application-data recovery process.

Diagnostic logs are written to disk, but they do not save or restore the recipe book or generated data.

## Implementation

<!-- @@author peilingggg -->
### Adding a recipe

#### Purpose and implemented scope

This feature supports the following user story: **As a first-time user, I want to add a recipe with its ingredients and quantities so that I can create a personalised meal.**

The command accepts all five recipe fields and at least one ingredient:

```text
add-recipe n/DISHNAME cal/CALORIES p/PROTEIN c/CARBS f/FATS i/INGREDIENT:QUANTITYUNIT [i/INGREDIENT:QUANTITYUNIT ...]
```

For example, `add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g` adds one
recipe. Calories use kcal; protein, carbohydrates, and fats use grams.
Single-value fields may appear in any order, but each must appear exactly once.
Each ingredient uses a separate `i/` field. Recipes are stored in memory for the
current session; adding a recipe does not regenerate an existing plan or grocery list.

#### Architecture-level view

Figure 1 places the feature within the existing command workflow. `MishMash`
reads input and asks `Parser` for a `Command`. Parsing validates the request and
constructs the recipe; execution adds that recipe to the session's `RecipeBook`.
`RecipeViewer` formats the successful addition for display.

![Figure 1. Architecture context for adding a recipe.](images/add-recipe/architecture.svg)

*Figure 1: Dependencies between existing responsibilities. The boxes group the
named implementation classes; arrows do not represent runtime message order.*

The parser does not receive or modify the recipe book when parsing this command.
`MishMash` supplies the book later through `Command.execute(recipeBook)`.

#### Class-level responsibilities and relationships

Figure 2 shows the selected classes used for recipe addition.

![Figure 2. Selected classes and relationships used by recipe addition.](images/add-recipe/classes.svg)

*Figure 2: Only relevant members and relationships are shown; some parameter
types are abbreviated. Solid arrows show stored references with private role
names and target-end multiplicities. Dashed arrows show dependencies or interface
realization. Reverse multiplicities are unspecified.*

`Parser` collects single-value fields in a local map and ingredients in a local
list. `AddRecipeCommand` implements `Command` and retains one validated `Recipe`.
The recipe book is an execution parameter, and the viewer is created locally;
neither is stored in the command.

`Recipe` holds the name, four integer nutrition values, and an ordered,
non-empty list of `GroceryItem` references. It validates its constructor arguments
and uses `List.copyOf` to protect its ingredient list from external changes.
`GroceryItem` represents an ingredient's name, `BigDecimal` quantity, and unit;
there is no separate `Ingredient` class.

Plain associations are used because these objects can be shared. A recipe can
also appear in a meal plan, and copying an ingredient list does not copy its
items. The list ends are ordered and nonunique: duplicate recipe names and
repeated ingredients are allowed. Stored references are shown as association
ends rather than duplicated as attributes.

#### Successful command parsing

Figure 3 follows the example command through validation and object creation.
Dispatch through `parseNamedCommand` and `parseAddRecipeCommand` is condensed.

![Figure 3. Sequence for parsing a valid add-recipe command.](images/add-recipe/parsing.svg)

*Figure 3: Dashed arrows represent returns, and constructor messages begin new
lifelines. Constructor internals, library operations, and logging are condensed.
The underlined `MishMash` participant represents the class containing the static
application methods.*

`parseAddRecipe` creates the local collections and calls
`collectRecipeArguments`. The prefix matcher identifies field boundaries,
including unknown prefixes, so an unsupported field cannot silently become part
of a dish name. `readArgument` rejects blank values, embedded slashes, unknown
prefixes, and repeated single-value fields. Repeated `i/` fields are parsed by
`parseIngredient` and appended in input order.

`validateRequiredFields` checks for `n/`, `cal/`, `p/`, `c/`, `f/`, and at least
one ingredient. `parseNutrition` then converts the four nutrition values in the
order calories, protein, carbohydrates, and fats. Each must contain ASCII digits
and fit within a non-negative Java `int`.

Only after these checks succeed does the parser construct `Recipe` and
`AddRecipeCommand`. The `Recipe` constructor independently checks the model's
constraints, protecting callers that bypass command parsing. Parsing returns a
command without changing application data, even if some local ingredient
objects were created before a later validation failure.

#### Execution and confirmation output

Figure 4 starts after parsing. The inherited `confirmationPrompt` method returns
an empty `Optional`, so the addition proceeds without a confirmation question.

![Figure 4. Sequence for formatting and executing a recipe addition.](images/add-recipe/execution.svg)

*Figure 4: Formatting is treated as a black box. Diagnostic logging, including
the recipe-count read, is omitted. The next command prompt belongs to the
application's read loop.*

`AddRecipeCommand.execute` rejects a null recipe book, then creates a
`RecipeViewer` and prepares the complete confirmation before calling
`RecipeBook.add(recipe)`. This ordering prevents a formatting failure from
leaving an addition whose confirmation could not be prepared. `RecipeBook.add`
rejects null and appends the recipe reference without replacing existing entries.

The command returns:

```text
New recipe added!
Rice
200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g
Ingredients:
- Rice: 150g
```

`MishMash` prints this result and displays the next command prompt. Ingredient
quantities use plain decimal notation without unnecessary trailing zeros.
Repeated ingredients remain separate entries; consolidation belongs to grocery
generation. The parser and addition command record counts at `FINE` level
without including the full recipe contents in their success messages.

#### Invalid input and recovery

Figure 5 shows the example with `i/Rice:0g`. `parseIngredient` rejects the
non-positive quantity before constructing a `GroceryItem`.

![Figure 5. Sequence for rejecting an invalid ingredient and recovering.](images/add-recipe/invalid-input.svg)

*Figure 5: Dashed messages labelled as exceptions show exceptional control
flow. No recipe or addition command is constructed for this request, and the
recipe book is not accessed.*

`parseAddRecipeCommand` catches `IllegalArgumentException`, appends
`ADD_RECIPE_USAGE`, and wraps it in `MishMashException` while preserving the
cause. `MishMash.executeCommand` catches this recoverable exception and calls
`displayError`. Because the message already contains `Format: `, general command
usage is not printed again. The read loop then prompts for another command.

| Boundary or invalid condition | Implemented behavior |
| --- | --- |
| Missing arguments, unprefixed leading text, missing required field, or no ingredient | Reject with an error and the add-recipe format. |
| Blank value, unknown prefix, repeated `n/`, `cal/`, `p/`, `c/`, or `f/` | Reject; repeated `i/` fields are allowed. |
| Nutrition value from 0 to `Integer.MAX_VALUE` | Accept; leading zeros are allowed. |
| Signed, negative, decimal, non-numeric, unit-suffixed, or overflowing nutrition value | Reject. |
| Positive integer or decimal ingredient quantity | Accept using `BigDecimal`, preserving decimal precision. |
| Zero or negative quantity, missing unit, malformed decimal, or extra colon | Reject. Quantities require digits before the decimal point and, when present, after it. |
| Ingredient unit containing ASCII letters only | Accept without converting units or checking a fixed unit vocabulary. |
| Spaces within names or around ingredient separators | Accept and trim surrounding name whitespace. Names and field values cannot contain `/`. |
| Duplicate recipe name or repeated ingredient | Accept and preserve insertion order. |
| Null recipe, book, or required model argument supplied by Java code | Reject with `NullPointerException`; this is a programming contract violation, not normal CLI input recovery. |

An invalid command leaves existing recipes unchanged. Nutrition values of zero
are valid, but ingredient quantities must be strictly positive. Although
`GroceryItem` itself permits zero, the parser and `Recipe` apply the stricter
recipe constraint.

#### Rationale and alternatives

| Decision | Alternative | Reason for the current design |
| --- | --- | --- |
| Validate before executing the addition | Insert a partially populated recipe and validate afterwards | Invalid input cannot leave an incomplete recipe in the book. |
| Keep input validation and model validation | Rely only on the parser | The parser enforces command syntax; the model also protects direct Java callers. |
| Use `BigDecimal` for ingredient quantities | Use floating-point values | Decimal quantities retain their precision and match the existing grocery model. |
| Reuse `RecipeViewer` | Build the same output separately in each command | Addition confirmations and detailed recipe listings use consistent formatting. |
| Allow repeated names and ingredients | Reject duplicates or merge automatically | The current book identifies recipes by position; ingredient consolidation is handled during grocery generation. |

#### Verification and diagram maintenance

`AddRecipeBoundaryTest` covers nutrition limits, leading zeros, decimal
precision, units, whitespace, field ordering, and repeated ingredients.
`AddRecipeEdgeCaseTest` uses `add-recipe-invalid.tsv` to check invalid input,
unchanged existing data, and a successful addition after rejection.
`AddRecipeExceptionTest` checks usage messages and preserved exception causes.
`AddRecipeCommandTest`, `RecipeTest`, and `RecipeBookTest` cover insertion,
output, constructor constraints, null arguments, and protection against external
list changes. `MishMashTest` covers console output and continued interaction.

Run the full verification tasks from the repository root using Java 25:

```powershell
.\gradlew.bat test checkstyleMain checkstyleTest
```

Editable diagram sources are under
[`docs/diagrams/add-recipe`](diagrams/add-recipe/README.md). Generate their SVGs
under `docs/images/add-recipe` and commit both sources and images so the guide
displays without a PlantUML plugin. After implementation changes, check the
depicted references, multiplicities, constructors, method order, and failure
paths against the code. Keep the abstraction notes when omitting implementation
details, and visually inspect regenerated diagrams.
<!-- @@author -->
<!-- @@author oilybikechain -->
### Contribution history: oilybikechain

The following non-merge commits reachable from the current branch establish the scope of these
contributions. Merge authorship does not imply ownership of the merged feature; the original
meal-plan generator, plan viewer, grocery formatter, dietary repair, and shortfall command have
separate contributors. The sections below describe the current integration with those components.

| Commit | Contribution documented here |
| --- | --- |
| `667d642` | Ingredient quantity model and grocery aggregation by exact name and unit. |
| `c3f6e95` | Grocery aggregation regression tests and API documentation. |
| `6b960ad` | `generate-list` parsing, command execution, and shared grocery snapshot integration. |
| `ba80853` | Preserved exception causes, generator assertions, and generation diagnostics. |
| `4c82e81` | Combined daily nutrition totals through `DayPlan` and `NutritionTotals`. |
| `f81dc10` | Daily warnings when planned nutrition exceeds exact generation targets. |

Earlier implementations on other branches are not additional features: `5ff6c3f` is the earlier
grocery integration, and `b814afb` is the pre-update nutrition-warning implementation. The current
guide describes their integrated replacements above. Profile and README edits are not product features.

### Combined daily nutrition totals

`DayPlan.calculateDailyNutritionTotals()` returns one immutable `NutritionTotals` record containing
`calories()`, `protein()`, `carbs()`, and `fats()`. Calories are measured in kcal; the macronutrients
are measured in grams. It delegates to the existing individual total getters, giving callers a
single result without changing the plan, recipes, or the individual-getter API.

Every meal occurrence contributes its full nutrition, including repeated recipes. Each day is
calculated independently; these are not totals across the entire plan. An empty day returns four
zeros. All four record components use `long`, so sums of valid integer recipe values can exceed
`Integer.MAX_VALUE` without wrapping.

For example, a day containing the User Guide's Protein Oatmeal, Chicken and Rice, and Salmon Salad
has `new NutritionTotals(1650, 127, 150, 55)`. The record represents calculated data; it does not
apply target tolerance, validate a nutrition goal, or introduce a new console command. The existing
plan viewer computes its display totals from `MealSummary` values separately.

`DayPlanTest` covers ordinary meals, one meal, zero nutrition, an empty day, repeated recipes,
independent days and repeated calls, unchanged meals, and totals above the integer range.

### Defensive checks and generation diagnostics

`GeneratePlanCommand` retains an `IllegalArgumentException` as the cause when wrapping a generation
failure in `MishMashException`. Parser validation for `generate-plan`, `delete-recipe`, and
`list-recipes` likewise preserves the cause while adding the relevant command usage. Users receive
the existing recoverable error and can retry; developers can inspect the original exception through
`getCause()` or the application's rejection log. This does not add new command syntax.

`MealPlanGenerator` uses assertions to check two internal invariants: the returned number of days
matches the request, and candidate construction starts with enough recipes. Normal argument validation
still handles user mistakes; assertions diagnose programming errors. Tests enable assertions through
the existing Gradle configuration. Enable them in a direct application run with
`java -ea -jar build/libs/MishMash.jar` when investigating invariants.

Generation logs at `FINE` record the requested day count, recipe count, whether targets were supplied,
and how many candidate combinations matched. They use the existing logger configuration and do not
record recipe contents. `ParserTest.parseCommand_invalidArguments_preservesCauseAndUsage` checks
the cause and usage for all three parsing paths; `GeneratePlanCommandTest` verifies the original
generation cause is retained. These changes strengthen diagnostics around teammates' commands rather
than implementing those commands.

### Detecting exceeded nutrition targets

`ActiveMealPlan` already retains the original `GeneratePlanRequest` alongside a generated plan for dietary
repair. Exceedance warnings reuse this request's immutable `NutritionTargets` rather than storing another
copy in `MealPlan`. Successful generation replaces both meals and targets through `setGenerated`; failed
generation preserves them. Dietary repair retains the request, and clearing a plan clears its request.
Legacy plans published with `set(MealPlan)` have no request and therefore no targets for warnings.

`NutritionTargets.calculateExcesses(NutritionTotals)` returns each nutrient's positive excess over its exact
target, or zero if the target is omitted or not exceeded. It uses `long` arithmetic, matching daily totals,
so sums beyond the integer range remain valid. This comparison does not use `TOLERANCE`: generation still
accepts days within ±15%, while viewing warns for any total strictly above the supplied target.

`Parser` supplies the current plan's meals and request targets to `ViewPlanCommand` at execution time.
Commands parsed before generation or replacement therefore display the latest meals and targets.
Legacy supplier-based parser and command constructors default to no targets. The new command constructor
accepts meal and target suppliers plus a nullable day number (null selects every day). The suppliers must
return corresponding meals and targets; omitted targets are represented by `NutritionTargets.none()`.

`MealPlanViewer` provides target-aware overloads for all days and one selected day; its original overloads
delegate with no targets. It sums each day's meals, requests excesses, and places warnings before the day's
closing separator in calories, protein, carbohydrates, and fats order. Warnings include the excess, actual
total, and target, using kcal for calories and g for the macronutrients. They do not change application state.
`check-shortfall` continues to use its explicitly supplied targets independently of this display.

Tests cover exact comparison boundaries, omitted targets, simultaneous excesses, large totals, exact output,
and day selection. Integration tests verify the generation tolerance, replacement after success or failure,
legacy plans without requests, dietary repair and clearing, and independence from `check-shortfall`.
Run `gradlew.bat test checkstyleMain checkstyleTest` with Java 25.

### Grocery command integration

#### Ingredient aggregation

`GroceryItem` holds a trimmed, non-blank ingredient name and unit plus a nonnegative `BigDecimal`
quantity. Recipe input separately requires strictly positive ingredient quantities; zero is supported
by the grocery model and aggregation API. `toString()` uses plain decimal notation and removes
insignificant trailing zeros, for example `Oil: 0.3ml`.

`GroceryListGenerator.generate(List<GroceryItem>)` uses a `LinkedHashMap` keyed by an `ItemKey`
record containing the ingredient name and unit. Each occurrence is merged with `BigDecimal::add`.
Exact decimal addition avoids floating-point rounding, and insertion order preserves the first
appearance of each key. `Rice:200g` and `Rice:100g` combine; `Rice:1kg`, `rice:100g`, and
`Rice:1cups` remain separate. No unit conversion, spelling normalization, or portion scaling occurs.

The method rejects a null list or item, returns an empty list for empty input, and publishes an
unmodifiable result with `List.copyOf`. Inputs remain unchanged, and successive calls do not reuse
previous totals. `GroceryListGeneratorTest` checks exact matching, incompatible units, different
names, decimal precision, zero quantities, ordering, immutable output, repeated calls, and null inputs.

#### Command parsing and shared state

`Parser()` shares one `ActiveMealPlan` between meal-plan generation, grocery generation, and viewing. For deterministic tests or an externally managed plan, use `Parser(MealPlanGenerator, ActiveMealPlan)`. The supplier-based constructors retain their caller-provided viewing sources; their grocery viewers do not automatically read the internally generated grocery snapshot.

The parser accepts `generate-list` without arguments and returns `GenerateListCommand` without reading or modifying the plan. During execution, the command traverses `MealPlan.getDays()`, `DayPlan.getMeals()`, and each recipe's ingredients. Every meal occurrence contributes its quantities. The existing `GroceryListGenerator` combines exact name/unit matches with `BigDecimal` arithmetic, preserving first-appearance order and keeping incompatible units separate.

`ActiveMealPlan.setGroceryList` copies the completed result into a read-only snapshot. `getGroceryList` returns an optional: absence means no list has been generated for the current plan, whereas a present empty list is valid. `ViewListCommand` reads this snapshot during execution and delegates formatting to `GroceryListViewer`; viewing never regenerates data.

Successfully storing a replacement plan clears its old groceries. Failed plan generation or invalid command syntax leaves the previous state intact. All state is kept in memory for one session.

The parse step rejects arguments with `generate-list does not accept arguments.` and
`Format: generate-list`; it does not inspect the active plan. The command reads the latest plan
when executed, so a command parsed before generation can use a subsequently generated plan. Without
a plan it returns `No active meal plan. Use 'generate-plan' first.` without publishing a snapshot.
Successful generation returns `Grocery list generated. Use 'view-list' to view it.` Only the finished
result is stored, preserving any previous snapshot if generation fails.

`GenerateListCommandTest`, `ParserGenerateListTest`, and `ActiveMealPlanTest` cover missing plans,
execution-time plan lookup, every recipe occurrence, repeated generation, syntax rejection, copying,
and snapshot replacement. `MishMashGeneratePlanTest` exercises the generation-to-viewing connection
through the console. Grocery-list formatting is supplied by the existing viewer rather than this
aggregation contribution.

Grocery parsing and viewing use `FINE` logs, successful generation logs counts at `INFO`, and rejected generation requests use `WARNING`. These loggers inherit the existing application configuration; no additional handlers are installed. New diagnostics exclude ingredient names and raw input.

Run `gradlew.bat test checkstyleMain checkstyleTest` on Windows with Java 25 to verify the integration.
<!-- @@author -->

<!-- @@author Rixte -->
### Checking nutrition shortfalls

#### Purpose and implemented scope

This V2.0 enhancement supports the following user story: **As a user tracking nutrition,
I want to detect when my planned meals fall short of my calorie or macronutrient targets
so that I can add or adjust meals accordingly.** It reports differences for the user to
act on; it does not automatically rebalance or change meals.

The command accepts one or more explicit daily targets:

```text
check-shortfall [cal/DAILY_CALORIES] [p/DAILY_PROTEIN] [c/DAILY_CARBS] [f/DAILY_FATS]
```

For example, `check-shortfall cal/2000 p/100` checks calories and protein for every day in
the active meal plan. Each target is optional individually, but the request must contain
at least one. Calories use kcal; protein, carbohydrates, and fat use grams. The feature
checks planned meals, not meals recorded as eaten. Targets belong to this request and are
not saved as personal settings or inferred from a previous generation command.

#### Architecture-level view

Figure 6 shows the enhancement in the existing architecture. `MishMash` reads the input,
asks `Parser` for a `Command`, executes it, and displays its returned text. The shortfall
feature follows that command workflow. Parsing validates syntax and creates the action;
execution reads the current session models and performs the comparison.

![Figure 6. Architecture context for shortfall checking.](images/shortfall/architecture.svg)

*Figure 6: Dependencies between existing responsibilities. Each box groups the named
implementation classes; it does not introduce a new Java component or service.*

The normal `Parser()` constructor shares its `ActiveMealPlan` between plan generation,
grocery generation, viewing, and shortfall checking. Tests can supply that holder through
`Parser(MealPlanGenerator, ActiveMealPlan)` to control the plan and generator. The
supplier-based parser constructors have separate caller-provided viewing sources; those
sources do not become the shortfall command's data source. Shortfall checks always use
the parser's `ActiveMealPlan`.

#### Class-level responsibilities and relationships

Figure 7 expands the implementation classes and the model references used by the check.

![Figure 7. Selected classes and relationships used by shortfall checking.](images/shortfall/classes.svg)

*Figure 7: Only members relevant to this enhancement are shown. Solid arrows are
navigable associations with private role names; dashed arrows show dependencies or
interface realization. Each reference is shown either as a typed attribute or as an
association end, without duplication. The `addShortfall` parameter list is abbreviated.*

`CheckShortfallParser` converts arguments into `NutritionTargets`. Its `parse` method
coordinates the request while `readTarget` validates and records one token. Named prefix
constants are shared by validation and target construction to keep those rules consistent.
The local map detects duplicate prefixes and keeps the values separate; omitted nutrients
remain `null` in the result. Token syntax is checked before integer conversion, rejecting signs,
decimals, units, and non-ASCII digits consistently. Conversion then detects values beyond
`Integer.MAX_VALUE`. The existing `NutritionTargets` constructor additionally rejects
non-positive values.

`CheckShortfallCommand` implements `Command` and retains two references: the shared
`ActiveMealPlan` holder and the request's `NutritionTargets`. It does not retain or read
the `RecipeBook` passed to `execute`; that parameter comes from the common command
interface. The constructor rejects null dependencies and a target object containing no
targets, protecting callers that construct commands without using the parser.

`MealPlan` supplies ordered `DayPlan` objects. Each day calculates its totals from its
`Recipe` references through four existing getters. The class diagram uses plain
associations rather than composition: the lists copy references, and recipes can occur
in multiple days. Association roles name the actual private fields; the parser's shared
holder is represented as a typed private attribute instead. Target-end
multiplicities are shown; reverse ends are left unspecified. The ordered, nonunique list
ends allow the model to hold repeated object references. An active plan is optional,
and the model permits empty day lists and
empty meal lists. Normal generation applies the stricter limits of one to seven days and
three distinct recipe entries per day.

#### Successful command parsing

Figure 8 follows `check-shortfall cal/2000 p/100` through object creation. The dispatch
from `parseCommand` through `parseNamedCommand` to `parseCheckShortfall` is condensed into
the parser lifeline. A new target parser is created for the request. Its `parse` method
collects values by calling `readTarget` for each supplied token, then constructs
`NutritionTargets(2000, 100, null, null)`.

![Figure 8. Sequence for parsing a valid shortfall command.](images/shortfall/parsing.svg)

*Figure 8: Parsing returns a command without reading any plan. Dashed return arrows
represent returned values; constructor messages start the new objects' lifelines.*

The outer parser creates `CheckShortfallCommand` only after argument validation succeeds.
The command's `hasAnyTarget` check ensures the request contains something to compare.
Separating parsing from execution allows the application to validate input without
altering its data and lets tests inspect or defer a command before running it.

#### Execution and daily comparison

Figure 9 starts when `MishMash` calls `execute(recipeBook)` on the parsed command. The
inherited confirmation method returns an empty `Optional`, so this read-only action
requires no confirmation. That common interface step is omitted from the diagram.

![Figure 9. Sequence for checking the active plan and its daily totals.](images/shortfall/execution.svg)

*Figure 9: The calorie getter/comparison calls are expanded; the equivalent calls for
protein, carbohydrates, and fat are summarized in the note. Repeated day-count reads,
list indexing, logging, and string-building details are condensed.*

The command looks up the plan during execution, rather than capturing the plan while
parsing. Consequently, a command parsed before generation can inspect a plan created
later. Replacing the active plan before execution also causes the replacement to be
checked. Failed generation preserves the old plan and grocery snapshot, so checks after
such a failure still inspect the previous plan.

If no plan exists, or its day list is empty, execution returns the generate-first message.
Otherwise it visits each day in order. `findShortfalls(day)` obtains the four totals and
corresponding optional targets. `addShortfall` appends a line only when
`dailyTarget != null && plannedTotal < dailyTarget`; the reported deficit is
`dailyTarget - plannedTotal`.
For a day with 1800 kcal and 80 g protein, targets of 2000 kcal and 100 g protein produce:

```text
Day 1:
- Calories: 1800/2000 kcal (short by 200 kcal).
- Protein: 80/100 g (short by 20 g).
```

Each day is compared separately. A surplus on Day 2 therefore cannot hide a deficit on
Day 1. Exact matches and surpluses produce no shortfall lines. A day with no deficits
receives an explicit `No shortfalls for the supplied targets.` message; this does not
claim that excesses or omitted nutrients meet any constraint.

The existing total getters sum into `long`, and the comparison and subtraction keep
that type. Three large recipe values cannot wrap around into a false deficit as they
could with `int` summation. Totals are recomputed from the plan rather than cached.
For D days containing M meal occurrences in total, comparison work is O(D + M), since
each meal is visited a fixed four times. The result contains at most four deficit lines
per day, so the additional output space is O(D).

No setter, recipe mutation, generator, or storage operation is called during this check.
Repeated checks leave the plan, recipe book, and existing grocery snapshot unchanged.
The command's `FINE` log records the number of checked days; it installs no log handler.

#### Invalid input and recovery

Figure 10 shows a rejected zero target. `CheckShortfallParser.parseTarget` throws
`IllegalArgumentException`; `Parser.parseCheckShortfall` wraps it as `MishMashException`,
preserves its cause, and appends `CHECK_SHORTFALL_USAGE`. `MishMash.executeCommand` catches
that recoverable exception and displays the error. The read loop then prints another
command prompt instead of ending the session.

![Figure 10. Sequence for rejecting an invalid target and recovering.](images/shortfall/invalid-input.svg)

*Figure 10: Exception returns are labeled explicitly. For this invalid request, neither
the target object nor the shortfall command is constructed, and the plan is not accessed.*

An invalid request is an input error, whereas a valid request with no active plan returns
normal guidance from `execute`. Neither path changes application data.

| Boundary or invalid condition | Implemented behavior |
| --- | --- |
| No targets, including blank arguments | Reject with a request for at least one daily target and the command format. |
| Supported prefix with no attached value, such as `cal/` or `cal/ 2000` | Reject with `Missing value after cal/.` and the command format. Each supported prefix is named in its own missing-value message. Unknown and repeated prefixes retain their respective errors. |
| Zero, negative, signed, decimal, non-numeric, unit-suffixed, or overflowing target | Reject. Accepted values are digit strings representing 1 to `Integer.MAX_VALUE`; leading zeros are allowed. |
| Unknown prefix, repeated prefix, extra token, or space after a prefix | Reject without accessing the plan; each supported prefix is allowed once, in any order. |
| Target omitted | Ignore that nutrient, even when its total is zero. |
| Total equal to or above target | Do not report a shortfall for that nutrient. |
| Non-empty plan containing an empty day | Its totals are zero; report the full amount of each supplied target as a deficit. |
| Large daily total exceeding the integer range | Keep the total as `long`; do not wrap around. |
| Missing plan or plan with no days | Return `No active meal plan. Use 'generate-plan' first.` |

#### Rationale and alternatives considered

| Decision | Alternative considered | Reason for the current choice |
| --- | --- | --- |
| Report any amount strictly below a target | Reuse the generator's +/-15% satisfaction test | Generation accepts approximate matches; this feature explains the exact gap. A 1900 kcal plan can be accepted for a 2000 kcal generation target and still have a 100 kcal shortfall. `isSatisfiedBy` also returns only a boolean and cannot explain which nutrients are deficient. |
| Require explicit targets for each check | Read saved personal targets or remember the previous generation request | Saved personal settings are a separate enhancement. Explicit arguments make the current behavior predictable and allow checks against different targets without changing the plan. |
| Reuse the existing per-day `long` totals | Duplicate summation, use `int`, or compare an average across all days | Reuse keeps calculations consistent with the model; `long` prevents integer wraparound, and per-day checks avoid masking deficits through averaging. |
| Report deficits without modifying meals | Automatically regenerate or rebalance the plan | Automatic changes would introduce selection rules and could invalidate groceries. Reporting is sufficient for this user story and keeps the action read-only. |
| Return formatted text through `Command.execute` | Add a new formatter class and a separate shortfall-result model | The output currently has one consumer and four fixed nutrients. The existing command contract is sufficient; a structured result would become useful if another interface or export feature needed to consume it. |

#### Verification and diagram maintenance

`CheckShortfallCommandTest` covers daily comparison boundaries, all 15 non-empty target
combinations, different deficits on different days, a shortfall on the seventh day,
missing and empty plans, empty days between populated days, varied and repeated meals,
large totals, repeated execution, and current-plan lookup. It also checks preservation
of absent, empty, and non-empty grocery snapshots and the recipe book.
`CheckShortfallParserTest` exercises all 24 target orders, every nutrient prefix,
numerical limits, leading zeros, whitespace, duplicate prefixes, malformed values,
specific missing-value messages, and independence between parsing attempts.
`ParserCheckShortfallTest` covers dispatch, wrapped errors and their causes, unchanged
data after rejection, output ordering, explicit targets independent of generation,
checks after recipe deletion, default and injected dependencies, and failed generation.
`MishMashTest` covers the complete console workflow, repeated checks and recovery,
continued use of viewing and help commands, and end-of-input after an invalid check.
Run these with the full Java 25 verification tasks documented under [Testing](#testing).

Maintenance follows the course's [Java conventions](https://se-education.org/guides/conventions/java/index.html),
[code-quality guidance](https://se-education.org/se-book/codeQuality/index.html), and
[Week 8 developer-testing and documentation guidance](https://nus-cs2113-ay2627-s1.github.io/website/schedule/week8/topics.html).
Keep request coordination separate from token validation, use descriptive names and the
shared command/prefix constants, and document method contracts in multiline Javadoc.
Behavioral tests should verify independent expected results, exceptions, and unchanged
data; diagram checks should verify relationships and call order against the implementation.

Editable sources for all five figures are under
[`docs/diagrams/shortfall`](diagrams/shortfall/README.md). Rendered SVGs are checked in so
the diagrams display without a website plugin. The source README records the renderer
version and regeneration commands. Sources and images must be updated together after
changes to methods, references, or control flow. Syntax checks verify that the diagrams
render; accuracy also requires checking every depicted call and branch against the code.

<!-- @@author -->

<!-- @@author GauthamNandi -->
### Recording meals and tracking nutrition

#### Purpose and scope

This V2.0 enhancement supports two user stories:

* **As a returning user, I want to record meals that I have eaten so that I can keep track of my
  actual food intake.**
* **As a user tracking nutrition, I want to calculate the total calories and macronutrients for each
  meal so that I know the nutritional content of what I am eating.**

It adds two commands:

```text
record-meal INDEX
view-meals
```

`INDEX` is the position of a recipe in the `list-recipes` output, counting from 1, as for
`delete-recipe`. `view-meals` lists every recorded meal with its calories (kcal) and protein, carbs and
fats (g), followed by the totals for all recorded meals. Unlike `check-shortfall`, which examines the
*planned* meals of the active plan, this feature records meals the user *has eaten*. The recorded meals
are kept in memory for the session only, matching the product's current data boundary.

#### Architecture-level view

The feature follows the existing command workflow. `MishMash` reads input, asks `Parser` for a
`Command`, executes it, and prints the returned text. Parsing validates syntax and creates the action;
execution reads or changes session data. A new model class, `MealLog`, holds the recorded meals, in the
same way `ActiveMealPlan` holds the active plan.

![Figure 11. Selected classes and relationships used by meal recording.](images/record-meal/classes.svg)

*Figure 11: Only members relevant to this enhancement are shown. Solid arrows are navigable
associations with private role names; dashed arrows show dependencies or interface realization.
`NutritionTotals` is a value record created by `MealLog`; its values are `long` so that adding many
recipes cannot overflow.*

* `MealLog` stores the recorded meals in the order they were recorded. It offers `record(Recipe)`,
  a read-only `getMeals()`, and `getTotals()`, which sums calories, protein, carbs and fats into a
  `NutritionTotals`. The commands never add up nutrition values themselves.
* `RecordMealCommand` and `ViewMealsCommand` implement `Command`. Each retains the shared `MealLog`.
* `MealLogViewer` (ui) turns the meals and totals into text, in the same way `MealPlanViewer` formats a
  meal plan. Keeping formatting out of the model and commands lets them be tested without comparing
  layout text.
* `Parser` creates one `MealLog` in its normal constructor and passes it to the commands it builds,
  because `Command.execute` only receives the `RecipeBook`.

#### Recording a meal

Figure 12 follows `record-meal 2`. The dispatch from `parseCommand` through `parseNamedCommand` to
`parseRecordMeal` is condensed into the parser lifeline.

![Figure 12. Sequence for recording a meal.](images/record-meal/record-meal.svg)

*Figure 12: Parsing returns a command without reading any data. The inherited confirmation step is
omitted because the command returns an empty `Optional`. The invalid-index branch is shown as an
`alt` fragment.*

`Parser` checks that the argument is a single positive whole number. A missing, non-numeric, signed,
zero or extra argument is rejected with the usage line `Format: record-meal INDEX`; nothing is read or
changed. The command is created only after validation succeeds.

During execution the command converts the index to a zero-based position, as `DeleteRecipeCommand`
does, and looks the recipe up in the `RecipeBook` passed to `execute`. If the book is empty or the index
exceeds the number of recipes, it throws a `MishMashException` and nothing is recorded. Otherwise it calls
`MealLog.record(recipe)` and `MealLog.getTotals()`, then asks `MealLogViewer.viewRecorded` to format the
recorded meal's calories and macros with the running totals, so the user sees the effect immediately. Recording a meal needs no confirmation:
it is quick to repeat, and it changes only the log.

#### Viewing recorded meals and totals

![Figure 13. Sequence for viewing recorded meals.](images/record-meal/view-meals.svg)

*Figure 13: Parsing mirrors `record-meal` and is omitted. The empty-log case is shown as an `alt`
fragment.*

`view-meals` takes no arguments; any argument is rejected with its usage line. `ViewMealsCommand` reads
the meals from the `MealLog`. An empty log is a valid state, not an error, so the command returns
`No meals recorded yet.` Otherwise it asks the log for the totals and delegates formatting to
`MealLogViewer`. Each meal appears with its own calories and macros, which gives the per-meal nutrition
required by the second user story, followed by one totals line. Viewing never changes the log.

#### Invalid input and recovery

The recoverable errors reuse the existing path: `Parser` or the command throws `MishMashException`,
`MishMash.executeCommand` catches it, displays the message, and prompts for another command.

| Boundary or invalid condition | Behavior |
| --- | --- |
| `record-meal` with no argument, a non-numeric, signed, zero or decimal index, or an extra token | Reject with the command format; no data is read or changed. |
| Index larger than the number of recipes | Reject with `Recipe index must be between 1 and N.`; nothing is recorded. |
| `record-meal` while the recipe book is empty | Reject with a message that there are no recipes to record; nothing is recorded. |
| `view-meals` with any argument | Reject with the command format. |
| `view-meals` before any meal is recorded | Return `No meals recorded yet.` |
| Recipe deleted or recipe book changed after a meal was recorded | The recorded meal keeps its original calories and macros. |
| Same recipe recorded twice | Two entries are kept, and both count toward the totals. |

#### Rationale and alternatives considered

| Decision | Alternative considered | Reason for the current choice |
| --- | --- | --- |
| Store the `Recipe` itself in the log | Store the recipe's index | `Recipe` is immutable, so the log keeps the nutrition as it was when the meal was eaten. Indexes shift whenever `delete-recipe` runs, so an old entry could silently point to a different recipe. |
| `MealLog.getTotals()` sums the meals on every call | Keep running totals updated by `record` | The meal list is the single source of truth, so totals cannot drift out of step with it. Sessions are small, so the repeated sum costs nothing noticeable. Running totals would need extra care if removing meals is added later. |
| `Parser` owns the `MealLog` and passes it to the commands | Widen `Command.execute` to take a shared context object | This follows how `ActiveMealPlan` is already shared and leaves the `Command` interface and every existing command unchanged. A context object becomes worthwhile if many more data holders are added. |
| Pick a meal by recipe index | Type nutrition values in directly | Reusing stored recipes avoids duplicating the input and validation of `add-recipe` and keeps unchecked numbers out of the log. Free-text entry would help when eating out, so it is a possible extension. |
| Return formatted text through `Command.execute`, using a small viewer | Print from the command or the model | The viewer matches `MealPlanViewer` and keeps layout out of the logic, so the model and commands can be tested without comparing layout text. |

#### Limitations and possible extensions

* The log lives in memory for one session and is lost when the application exits.
* All meals form one list. A later version could add `d/DATE` to record meals against a day and show
  daily totals, or compare the totals with the targets given to `check-shortfall`.
* There is no command to remove or correct a recorded meal yet.
* Each meal counts as one whole recipe; portion sizes are not supported, as for meal plans.

#### Diagram maintenance

Editable sources for the three figures are under
[`docs/diagrams/record-meal`](diagrams/record-meal/README.md). Rendered SVGs are checked in under
`docs/images/record-meal` so the diagrams display without a plugin. The source README gives the
regeneration commands. Check every depicted call, branch and message against the code after any change, and update the
sources and images together.

<!-- @@author -->

## Documentation Logging Testing Configuration and DevOps

### Documentation

The project documentation is maintained in Markdown under `docs`. The main documents are `DeveloperGuide.md` and `UserGuide.md`

Keep command formats, examples, prerequisites, and expected results consistent with the code. Document planned behaviour separately from available behaviour.

#### Claiming and correcting authorship with RepoSense

RepoSense normally uses Git history to identify contributions. Use `@@author` tags when necessary to credit your work or restore the original author's credit after moving or refactoring content. These tags change RepoSense attribution, not Git commit history.

Use the comment syntax appropriate to the file:

| File type | Start tag | End tag |
| --- | --- | --- |
| Java | `// @@author oilybikechain` | `// @@author` |
| Markdown | `<!-- @@author oilybikechain -->` | `<!-- @@author -->` |

1. Identify the code or documentation belonging to the contributor. Replace `oilybikechain` with the appropriate RepoSense author identifier when crediting someone else.
2. Put the start tag on its own line immediately before that contribution, including its heading or explanatory comments.
3. Put the end tag on its own line immediately after it. Without an end tag, attribution continues to the next start tag or the end of the file.
4. To fix incorrect credit, correct the existing start tag or add a correctly scoped pair around the original contribution. Keep unrelated contributions outside the pair.
5. Commit and push the change to the branch analysed by the tracker, then check the next refreshed report.

Do not add an end tag without a corresponding start tag; it can leave subsequent content unattributed. Markdown HTML comments remain hidden in the rendered guide.

Reference: [RepoSense authorship-tag guide](https://reposense.org/ug/usingAuthorTags.html).

### Logging

The application uses `java.util.logging`. Diagnostics are written to rotating files using the pattern `mishmash.%g.log`, with an approximate limit of 1,000,000 bytes per file and three files retained.

Levels include `FINE` for command diagnostics, `INFO` for lifecycle events and successful grocery-generation counts, `WARNING` for selected rejected operations or logging problems, and `SEVERE` for unexpected application failures. If the log file cannot be opened because of an I/O error, the application continues with its existing logging configuration.

### Testing

The project uses JUnit Jupiter. Tests are under `src/test/java/mishmash` and mirror the production packages. They cover validation, commands, models, generation, display formatting, and full console interactions.

Run verification from the repository root:

```powershell
.\gradlew.bat test checkstyleMain checkstyleTest
```

On Linux or macOS:

```text
./gradlew test checkstyleMain checkstyleTest
```

Use Java 25. JUnit reports appear at `build/reports/tests/test/index.html`, and Checkstyle reports are under `build/reports/checkstyle`. The add-recipe invalid-input dataset is `src/test/resources/add-recipe-invalid.tsv`.

### Configuration

The current application has no user configuration file. Users supply recipe values, plan length, and optional daily nutrition targets through commands.

The supported plan length is 1 to 7 days. Each generated day contains exactly three different stored recipe entries. Generation uses a fixed target tolerance of 15%; users cannot change that tolerance through a command. In contrast, `check-shortfall` reports any amount strictly below the explicit targets supplied to that check.

### DevOps

GitHub Actions runs `./gradlew check` for pushes and pull requests on Ubuntu, macOS, and Windows using Java 25. Check the resulting reports before merging changes.

The Gradle build packages the application as `build/libs/MishMash.jar`. Release documentation should use that filename and describe the features included in the actual release.

## Appendix Requirements

### Product Scope

**Target user profile:** Users who prepare meals in advance, want to organise recipes and review calorie or macronutrient totals, and are comfortable entering text commands. Users provide their own recipes and nutrition information.

**Value proposition:** MishMash connects recipe management, meal selection, and grocery preparation in one workflow. It generates plans from user-entered recipes and combines ingredient quantities for the selected meals.

**Current boundaries:** Data lasts for one session. Nutrition is entered by the user rather than calculated from an ingredient database. Plans use whole recipes without portion scaling. Ingredient names and units are matched exactly; automatic unit conversion and grocery-item ticking are not supported.

### User Stories for Current Functionality

| As a ... | I want to ... | So that I can ... |
| --- | --- | --- |
| New user | View available commands and formats | Learn how to use the application |
| Meal prep user | Add a recipe with nutrition, ingredients, and quantities | Make it available for planning |
| User managing recipes | List recipes and inspect a selected recipe | Find and check its stored details |
| User managing recipes | Confirm deletion of a selected recipe | Check that I am removing the intended entry |
| Meal prep user | Generate a random meal plan | Reduce manual meal selection |
| User tracking nutrition | Supply daily calorie and macronutrient targets | Generate meals within the supported tolerance |
| User tracking nutrition | See daily excesses above my generation targets | Identify days that exceed an exact target despite the generation tolerance |
| User tracking nutrition | Detect when planned meals fall short of calorie or macronutrient targets | Add or adjust meals accordingly |
| Meal prep user | View all planned days or a selected day | Review meals and daily nutrition totals |
| Shopper | Generate groceries from the active plan | Obtain combined ingredient quantities |
| Shopper | View the generated grocery list | See what to purchase |
| User who makes a typing mistake | Receive an error and enter another command | Correct the mistake without restarting |

### User Stories for Future Consideration

The following ideas are not implemented and are not commitments for a particular release:

- Edit or search recipes.
- Apply dietary restrictions or allergy preferences.
- Record meals eaten (`record-meal`) and see the calories and macronutrients of each meal and their
  totals (`view-meals`). The design is described in
  [Recording meals and tracking nutrition](#recording-meals-and-tracking-nutrition).
- Save and restore recipes or meal plans between sessions.
- Estimate meal or grocery costs.

Daily nutrition targets and daily nutrition totals are already supported and are therefore included in the current-functionality stories.

### Use Cases

The actor in each use case is the user. A command session has already started and the user has entered a name. MSS means Main Success Scenario.

#### UC01 Add a recipe

**MSS**

1. The user enters `add-recipe` with a name, nutrition values, and at least one ingredient.
2. MishMash validates the input.
3. MishMash adds the recipe, displays its details, and prompts for another command.

**Extensions**

- 2a. A field is missing, blank, repeated, or invalid: MishMash displays an error and the add-recipe format. Existing recipes remain unchanged. The use case ends.

#### UC02 List recipes

**MSS**

1. The user enters `list-recipes`, optionally selecting an index.
2. MishMash displays the numbered names and calories of all recipes, or the full details of the selected recipe.

**Extensions**

- 1a. The index syntax is invalid: MishMash displays an error and the command format.
- 2a. The book is empty: MishMash displays guidance to add a recipe.
- 2b. The selected index does not exist: MishMash displays an index-range error.

#### UC03 Delete a recipe

**MSS**

1. The user enters `delete-recipe INDEX`.
2. MishMash checks the index and asks for confirmation, naming the selected recipe.
3. The user enters `y` or `yes`.
4. MishMash deletes the entry and displays its name and the remaining recipe count.

**Extensions**

- 1a. The index syntax is invalid: MishMash displays an error.
- 2a. The book is empty or the index does not exist: MishMash displays an error without asking for confirmation.
- 3a. The response is anything other than `y` or `yes`, ignoring case and surrounding whitespace, or input ends: the deletion is cancelled and the book remains unchanged.

#### UC04 Generate a meal plan

**Precondition:** At least three recipe entries are available.

**MSS**

1. The user enters `generate-plan d/DAYS`, optionally including daily nutrition targets.
2. MishMash validates the requested days and targets.
3. MishMash generates exactly three different recipe entries per day, meeting every supplied target within 15%.
4. MishMash makes the new plan active, clears the previous groceries, and displays a confirmation.

**Extensions**

- 2a. Days are outside 1 to 7, a target is not a positive integer, or arguments are malformed: MishMash rejects the command.
- 3a. Fewer than three recipe entries are available, or no acceptable plan is found: MishMash displays an error. The previous plan and groceries remain available.

Recipes may be repeated on different days. With no targets, there are no nutrition constraints on the selection.

#### UC05 View a meal plan

**MSS**

1. The user enters `view-plan`, optionally selecting `d/DAY_NUMBER`.
2. MishMash displays all days or the selected day, including each meal's nutrition and daily totals.

**Extensions**

- 1a. The day argument is malformed: MishMash displays an error and the command format.
- 2a. No plan is active: MishMash asks the user to generate one first.
- 2b. The selected day exceeds the active plan's length: MishMash displays a range error.

#### UC06 Generate a grocery list

**Precondition:** A meal plan is active.

**MSS**

1. The user enters `generate-list` without arguments.
2. MishMash combines ingredients from every meal occurrence in the active plan.
3. MishMash stores the completed groceries and confirms that they can be viewed using `view-list`.

**Extensions**

- 1a. Arguments are supplied: MishMash rejects the command without changing the stored list.
- 2a. There is no active plan: MishMash asks the user to generate one first.

Only exact ingredient-name and unit matches are combined. Different units or letter case remain separate entries.

#### UC07 View a grocery list

**MSS**

1. The user enters `view-list` without arguments.
2. MishMash displays the stored entries, their quantities and units, and the total number of entries.

**Extensions**

- 1a. Arguments are supplied: MishMash displays an error.
- 2a. No grocery list has been generated: MishMash asks the user to generate one first.

Viewing does not regenerate groceries. The displayed `[ ]` markers cannot be ticked through a command.

#### UC08 View help or exit

- Entering `help` displays command purposes and formats. Additional arguments produce an error.
- Entering `bye` displays the goodbye message and ends the session. Session data is discarded.

<!-- @@author Rixte -->
#### UC09 Check planned nutrition shortfalls

**MSS**

1. The user enters `check-shortfall` with at least one daily nutrition target.
2. MishMash validates the target arguments and obtains the active plan.
3. MishMash compares each day's planned totals with the supplied targets.
4. MishMash displays each day's deficient nutrients and amounts, or a no-shortfalls
   message for that day, then prompts for another command.

**Extensions**

- 2a. Targets are missing, repeated, unknown, or invalid: MishMash displays an error and
  the check-shortfall format. The use case ends without changing application data.
- 2b. No plan exists or the plan has no days: MishMash asks the user to generate a plan.
  The use case ends.

Omitted nutrients and excesses are not checked. All plan, recipe, and grocery data remains
unchanged. A shortfall is any amount below a target, even within generation's tolerance.
<!-- @@author -->

<!-- @@author oilybikechain -->
#### UC10 Detect exceeded nutrition targets

**Precondition:** An active plan was generated with at least one daily nutrition target.

**MSS**

1. The user enters `view-plan`, optionally selecting a day.
2. MishMash calculates each displayed day's nutrition totals and compares them with the saved generation targets.
3. For each exceeded target, MishMash displays a warning with the excess, actual total, and target below that day's totals.

**Extensions**

- 2a. A target is omitted, equal to the total, or above it: no warning is shown for that nutrient.
- 2b. No generation request is retained, or the plan was generated without targets: only the normal plan display is shown.
- 2c. No plan exists or a selected day is invalid: the existing `view-plan` messages apply.

The comparison is strict and does not apply the generator's 15% tolerance. Viewing leaves the meals,
targets, and groceries unchanged. A successful replacement uses its new targets; failed generation
preserves the previous warnings. Dietary repair recalculates warnings using the repaired meals and
the retained generation targets. Clearing the plan removes the warnings.
<!-- @@author -->

### Non Functional Requirements

- The application requires Java 25 and provides a command-line interface.
- The project targets Windows, macOS, and Linux, with verification configured on all three operating systems.
- Ordinary invalid command input must allow the session to continue and preserve existing data.
- Decimal ingredient quantities must retain their numeric precision.
- Generating meals and groceries must leave the stored recipe entries unchanged.
- The normal application workflow does not require a network connection or an external database.

### Glossary

| Term | Meaning |
| --- | --- |
| Recipe | A named entry with calorie and macronutrient values and required ingredients. |
| Recipe book | The ordered recipes available in the current session. |
| Recipe index | A recipe's current displayed position, starting at 1. |
| Meal plan | Recipes assigned to one or more numbered days. |
| Active plan | The single plan currently available for viewing and grocery generation. |
| Macronutrient | Protein, carbohydrate, or fat, measured in grams. |
| Daily target | An optional desired daily total for calories or a macronutrient. |
| Grocery list | The combined ingredient quantities generated from the active plan. |
| Session | One running instance of the application; application data is not retained after it ends. |

## Appendix Instructions for Manual Testing

These checks cover current behaviour. Testers may explore additional inputs. Each subsection is independent: start a fresh session and load the sample recipes where needed so earlier tests do not change later preconditions.

### Launch

1. Use Java 25 and open a terminal in the project root containing the built JAR.
2. Run `java -ea -jar build/libs/MishMash.jar`.
3. Confirm that the banner and name prompt appear. Enter a name and check that a command prompt follows.

### Sample Recipes

Enter each command on one line:

```text
add-recipe n/Chicken Rice cal/600 p/40 c/70 f/15 i/Chicken:200g i/Rice:150g
add-recipe n/Tofu Rice cal/500 p/25 c/60 f/12 i/Tofu:200g i/Rice:100g
add-recipe n/Oats cal/400 p/15 c/50 f/10 i/Oats:80g i/Milk:200ml
```

With exactly these three entries, each generated day contains all three. Meal order may vary. Daily totals are 1500 kcal, 80g protein, 180g carbs, and 37g fats.

### Add a Recipe

- **Valid input:** Enter the first sample recipe. Expect one addition, its nutrition and ingredients, and another prompt.
- **Missing ingredient:** Omit both `i/` fields. Expect an error requiring at least one ingredient and no addition.
- **Invalid nutrition:** Replace `cal/600` with `cal/abc` or `cal/2147483648`. Expect an integer or range error.
- **Boundary value:** Replace `cal/600` with `cal/0`. Expect successful addition; zero nutrition is allowed.
- **Invalid quantity:** Use `i/Chicken:0g`. Expect rejection because recipe ingredient quantities must be positive.
- **Decimal quantity:** Use `i/Chicken:200.5g`. Expect the decimal quantity to be preserved.
- **Repeated field:** Add a second `n/` field. Expect rejection. Repeated `i/` fields remain valid.

For rejected commands, check that existing recipes remain unchanged and a subsequent valid addition succeeds.

### List Recipes

After loading the samples:

- Enter `list-recipes`. Expect three numbered entries in insertion order, showing names and calories.
- Enter `list-recipes 2`. Expect Tofu Rice's full nutrition and ingredients.
- Enter `list-recipes 4`. Expect an out-of-range error.
- In a fresh empty session, enter `list-recipes`. Expect guidance to add a recipe.

### Delete a Recipe

After loading the samples:

- Enter `delete-recipe 2`. Expect a confirmation naming Tofu Rice. Answer `n`; verify that nothing changed.
- Repeat and answer `yes`. Expect deletion, a remaining count of 2, and renumbering of the later recipe.
- Enter `delete-recipe 0` or an index larger than the current book size. Expect an error without deletion.

### Generate a Meal Plan

Use a fresh session containing exactly the three sample recipes:

- Enter `generate-plan d/3`. Expect confirmation of a three-day plan. Check with `view-plan` that each day has exactly three meals.
- Enter `generate-plan d/2 cal/1500 p/80 c/180 f/37`. Expect success. Each day's totals match the sample totals exactly.
- Enter `generate-plan d/8`. Expect rejection because the day limit is 1 to 7.
- Enter `generate-plan d/1 cal/1`. Expect failure to find acceptable meals. Verify that the previous plan remains available.
- In a separate session containing fewer than three recipes, try `generate-plan d/1`. Expect a minimum-recipe error.

### View a Meal Plan

After generating a two-day plan:

- Enter `view-plan`. Expect both days, meal nutrition, and daily totals.
- Enter `view-plan d/1`. Expect only Day 1.
- Enter `view-plan d/3`. Expect a day-range error and another prompt.
- In a fresh session with no plan, enter `view-plan`. Expect the generate-first message.

<!-- @@author oilybikechain -->
### Combined Daily Nutrition Totals

Using exactly the three [sample recipes](#sample-recipes), run `generate-plan d/2` and `view-plan`.
Both days must show `Daily Totals: 1500 kcal | Protein: 80g | Carbs: 180g | Fats: 37g` regardless
of meal order. Run `view-plan d/2`: its totals must match the second day's previous display, rather
than adding the two days together. Repeating the command must not change the plan or totals.

The combined `NutritionTotals` API's empty-day, repeated-meal, and integer-overflow cases are tested
by `DayPlanTest`; ordinary generation always supplies three distinct recipes per day.

### Nutrition Target Warnings

In a fresh session with no dietary restrictions, enter:

```text
add-recipe n/A cal/500 p/20 c/50 f/10 i/Rice:100g
add-recipe n/B cal/600 p/30 c/60 f/15 i/Rice:100g
add-recipe n/C cal/700 p/40 c/70 f/20 i/Rice:100g
generate-plan d/2 cal/1700 p/85 c/180 f/45
view-plan
view-plan d/2
```

Each day must total 1800 kcal, 90 g protein, 180 g carbohydrates, and 45 g fats, regardless of meal order.
Expect exactly these warnings below each displayed day's totals:

```text
Warning: Calories exceed target by 100 kcal (1800 kcal actual; 1700 kcal target).
Warning: Protein exceeds target by 5 g (90 g actual; 85 g target).
```

`view-plan d/2` must show only day 2. Then try `generate-plan d/1 cal/1`: generation must fail and
`view-plan` must still display the previous two days and their warnings. Run `check-shortfall cal/1900`
and verify that a subsequent `view-plan` retains those warnings. Finally run `generate-plan d/1` and
`view-plan`: the replacement must show one day with no warnings because no targets were supplied.

<!-- @@author -->
<!-- @@author Rixte -->
### Check Nutrition Shortfalls

1. In a fresh session, enter `check-shortfall cal/2000` before generating a plan. Expect
   `No active meal plan. Use 'generate-plan' first.` and another command prompt.
2. Load exactly the three [sample recipes](#sample-recipes), enter `generate-plan d/2`,
   and then `generate-list`. Each day's planned totals are 1500 kcal, 80 g protein,
   180 g carbohydrates, and 37 g fat.
3. Enter `check-shortfall cal/2000 p/100 c/200 f/60`. Both Day 1 and Day 2 should report
   deficits of 500 kcal, 20 g protein, 20 g carbohydrates, and 23 g fat. For each day,
   verify that the displayed total and target are included alongside the deficit.
4. Enter `check-shortfall cal/1500 p/80 c/100`. Both days should display
   `No shortfalls for the supplied targets.` Calories and protein exactly match,
   carbohydrates exceed the target, and fat is omitted.
5. Enter `check-shortfall cal/1501`. Expect a 1 kcal deficit for each day even though
   that difference is within the generation tolerance.
6. Try `check-shortfall`, `check-shortfall cal/0`, `check-shortfall p/1.5`,
   `check-shortfall cal/2000 cal/2100`, and `check-shortfall d/1`. Each should produce a
   recoverable error with the check-shortfall format. Retry a valid check afterwards.
7. Enter `view-plan` and `view-list`. The two-day plan and all five grocery entries should
   be unchanged. Their quantities should match the table in
   [Generate a Grocery List](#generate-a-grocery-list). Repeat a check to verify that
   results are stable, and enter `help` to confirm the new command is listed.

The CLI cannot generate an empty day or an empty plan; those defensive cases, null
dependencies, and integer-overflow boundaries are covered by the automated tests.
<!-- @@author -->

### Generate a Grocery List

With exactly the sample recipes, generate a two-day plan and enter `generate-list`. Expect:

```text
Grocery list generated. Use 'view-list' to view it.
```

Then enter `view-list` and verify:

| Ingredient | Quantity for two days |
| --- | --- |
| Chicken | 400g |
| Rice | 500g |
| Tofu | 400g |
| Oats | 160g |
| Milk | 400ml |

There should be five displayed entries. Their order follows the meals' first appearances and may vary. The generation confirmation itself does not display an item count.

- Run `generate-list` again. Expect unchanged quantities rather than doubled totals.
- Enter `generate-list extra`. Expect rejection without changing the stored list.
- In a fresh session without a plan, enter `generate-list`. Expect guidance to generate a plan first.

<!-- @@author oilybikechain -->
For aggregation edge cases, use the User Guide's [ingredient combining example](UserGuide.md#ingredient-combining-example)
in a fresh session. Expect exactly `Rice: 0.6g`, `Rice: 2kg`, and `rice: 100g`, with no rounding,
unit conversion, or case normalization. Repeat `generate-list` and check that quantities stay the same.
Then enter `generate-list extra` and `view-list`: syntax rejection must leave those entries unchanged.
Generate a replacement plan and confirm that groceries must be regenerated before viewing.

### Defensive Diagnostics and Recovery

With a plan and grocery list already generated, enter `generate-plan d/0`, `list-recipes 0`, and
`delete-recipe 0`. Each must display its own command format, then allow another command. Verify
with valid viewing commands that no recipes, meals, or groceries changed. Attempt
`generate-plan d/1 cal/1` with the standard sample recipes and confirm that generation fails while
the previous plan and grocery list remain available.

Exception causes and assertion invariants are developer diagnostics, not extra console output.
Verify cause preservation with `ParserTest` and `GeneratePlanCommandTest` under Java 25 with
assertions enabled. Inspect `FINE` generation records only when that level is enabled in the
existing logging configuration; absence of a `FINE` record at a higher log level is expected.
<!-- @@author -->

### View a Grocery List

- Enter `view-list` after grocery generation. Expect ingredient quantities, units, `[ ]` markers, and the total entry count.
- Enter `view-list extra`. Expect a format error.
- Successfully generate a replacement meal plan, then enter `view-list`. Expect a generate-list-first message because the previous groceries were cleared.
- Generate groceries again, then attempt `generate-plan d/1 cal/1`. After that failure, verify that the previous grocery list is still available.

### Help and Recovery

- Enter `help`. Expect all supported command names, purposes, and formats.
- Enter `help extra`, a blank line, or an unknown command. Expect an error followed by another prompt.
- After an invalid command, enter a valid command and verify that it executes normally.

### Session Data

Add recipes, generate a plan and groceries, then enter `bye`. Restart and enter a name. Verify that the recipe book is empty and no plan or grocery list is available. Recipes from the earlier session should not reappear.

There is no application-data save file to delete or corrupt for a storage-recovery test.
