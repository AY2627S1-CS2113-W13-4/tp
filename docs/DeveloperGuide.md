# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

### Grocery command integration

`Parser()` shares one `ActiveMealPlan` between meal-plan generation, grocery generation, and viewing.
For deterministic tests or an externally managed plan, use `Parser(MealPlanGenerator, ActiveMealPlan)`.
The supplier-based constructors retain their caller-provided viewing sources; their grocery viewers
do not automatically read the internally generated grocery snapshot.

The parser accepts `generate-list` without arguments and returns `GenerateListCommand` without
reading or modifying the plan. During execution, the command traverses `MealPlan.getDays()`,
`DayPlan.getMeals()`, and each recipe's ingredients. Every meal occurrence contributes its quantities.
The existing `GroceryListGenerator` combines exact name/unit matches with `BigDecimal` arithmetic,
preserving first-appearance order and keeping incompatible units separate.

`ActiveMealPlan.setGroceryList` copies the completed result into a read-only snapshot.
`getGroceryList` returns an optional: absence means no list has been generated for the current plan,
whereas a present empty list is valid. `ViewListCommand` reads this snapshot during execution and
delegates formatting to `GroceryListViewer`; viewing never regenerates data.

Successfully storing a replacement plan clears its old groceries. Failed plan generation or invalid
command syntax leaves the previous state intact. All state is kept in memory for one session.

Grocery parsing and viewing use `FINE` logs, successful generation logs counts at `INFO`, and rejected
generation requests use `WARNING`. These loggers inherit the existing application configuration;
no additional handlers are installed. New diagnostics exclude ingredient names and raw input.

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
