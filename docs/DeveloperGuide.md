# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

### Meal-plan and grocery integration

The application creates one `Model` and passes it to `Parser(Model)`. Grocery commands retain this
shared model; existing recipe commands continue to use `Command.execute(RecipeBook)`.
The command interface also permits `MishMashException` for unavailable meal plans or grocery lists.

The meal-plan generator should receive the same model and call `model.setMealPlan(recipes)` after
successfully generating a plan. Supply a `List<Recipe>` in meal order, with one occurrence per meal.
Do not create a separate model or use the display-only `MealSummary` objects as the ingredient source.

`generate-list` flattens each recipe's ingredients, delegates aggregation to `GroceryListGenerator`,
and stores the completed result using `Model.setGroceryList`. `view-list` delegates the stored
snapshot to `GroceryListViewer`. Exact name/unit matching and `BigDecimal` arithmetic are preserved.

The model copies lists before updating state and exposes read-only snapshots. Replacing the meal
plan invalidates the grocery list. Failed validation leaves existing state intact. An absent grocery
list differs from a generated empty list. State is held in memory for one session; no storage is added.

Commands use `java.util.logging`: generation at `INFO`, viewing at `FINE`, and rejected grocery
requests at `WARNING`. Java's default console handler writes to standard error; `FINE` is hidden
at the default `INFO` level. Logs contain fixed event descriptions and counts, without recipe names,
ingredient names, or raw commands.

Run `gradlew.bat test checkstyleMain checkstyleTest` on Windows with Java 25 to verify the integration.

{Describe the design and implementation of the product. Use UML diagrams and short code snippets where applicable.}


## Product scope
### Target user profile

{Describe the target user profile}

### Value proposition

{Describe the value proposition: what problem does it solve?}

## User Stories

|Version| As a ... | I want to ... | So that I can ...|
|--------|----------|---------------|------------------|
|v1.0|new user|see usage instructions|refer to them when I forget how to use the application|
|v2.0|user|find a to-do item by name|locate a to-do without having to go through the entire list|

## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}
