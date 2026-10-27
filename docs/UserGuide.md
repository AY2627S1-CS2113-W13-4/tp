# MishMash User Guide

MishMash is a command-line meal prep planner. Add your recipes, use them to plan meals, and check the ingredients you need to buy.

**Start Here:** [Set up and launch](#quick-start) · [Find a command](#commands-at-a-glance) · [View meals](#viewing-a-meal-plan) · [Check nutrition](#checking-nutrition-shortfalls) · [View groceries](#viewing-a-grocery-list) · [Record meals](#recording-a-meal) · [Fix a problem](#troubleshooting)

This guide covers the current team version, including meal-plan viewing, grocery-list viewing, nutrition shortfall checking, meal recording, and setting and viewing dietary preferences.

## Quick Start

### Set Up and Launch

1. **Install Java 25.** Use Java 25 for this project.
2. **Download the application's JAR file** from the team's [Releases page](https://github.com/AY2627S1-CS2113-W13-4/tp/releases), when a release is available. A JAR is the file used to run the application. The current build names it `MishMash.jar`.
3. **Run the JAR file from a terminal:**

   - Put the JAR file in an **empty folder**, such as `smoke-test/ip1`. This keeps data files from other applications or earlier tests from interfering with a version that reads or writes files.
   - Open a terminal and **navigate to that folder**. For example, if `smoke-test` is inside your current folder:

     ```text
     cd "smoke-test/ip1"
     ```

     Any relative data-file paths are resolved from the terminal's current folder. The current MishMash application keeps its data in memory and does not save it to disk.

   - In the **same terminal**, confirm that Java 25 is selected:

     ```text
     java -version
     ```

     Check that the version starts with `25`.

   - Launch the JAR using `java -jar "{file_name}"`, replacing `{file_name}` with its actual filename. For the current build:

     ```text
     java -jar "MishMash.jar"
     ```

     Keep the quotes if the filename contains spaces. Run this command instead of double-clicking the JAR so you can enter commands and read errors in the terminal.

4. **Enter your name** when MishMash asks `What is your name?`, then press Enter. The first line you type is your name, rather than an application command.
5. **Enter one command per line** and press Enter to run it. Enter `bye` to exit.

### Try Your First Command

After entering your name, add a recipe:

```text
add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15 i/Chicken Breast:200g i/White Rice:150g i/Olive Oil:10ml
```

MishMash confirms the addition and shows the recipe's nutrition and ingredients. To finish:

```text
bye
```

> **Session Data:** Closing or restarting the current application clears the recipes, meal plan, grocery list, recorded meals, and dietary preferences. There is no save or load command.

## Commands at a Glance

Click a task to jump directly to its instructions. The commands below are supported by the current team version.

| I Want To… | Enter | Before Using It |
| --- | --- | --- |
| [Add a recipe](#adding-a-recipe) | `add-recipe n/NAME cal/CALORIES p/PROTEIN c/CARBS f/FATS i/INGREDIENT:QUANTITY_UNIT` | Include all fields and at least one ingredient. |
| [List recipes](#listing-recipes) | `list-recipes [INDEX]` | Omit the index to list all recipes; supply one to see its full details. |
| [Edit a recipe](#editing-a-recipe) | `edit-recipe INDEX p/55` | Use an index from the latest recipe list and supply at least one field. |
| [Delete a recipe](#deleting-a-recipe) | `delete-recipe INDEX` | Use an index from the latest recipe list, then confirm the deletion. |
| [Set dietary preferences](#setting-dietary-preferences) | `set-diet diet/vegan avoid/peanut` | Classify recipes before generating restricted meals. |
| [View dietary preferences](#viewing-dietary-preferences) | `view-diet` | No prerequisites. |
| [Generate meals](#generating-a-meal-plan) | `generate-plan d/3` | Add at least three recipes; choose 1 to 7 days. |
| [View every day](#viewing-a-meal-plan) | `view-plan` | Generate a meal plan first. |
| [View one day](#viewing-a-meal-plan) | `view-plan d/1` | Choose a day in the active plan. |
| [Check nutrition shortfalls](#checking-nutrition-shortfalls) | `check-shortfall cal/2000 p/100` | Generate a plan first; supply at least one daily target. |
| [Generate groceries](#generating-a-grocery-list) | `generate-list` | Generate a meal plan first. |
| [View groceries](#viewing-a-grocery-list) | `view-list` | Generate a grocery list first. |
| [Record a meal I ate](#recording-a-meal) | `record-meal INDEX` | Add at least one recipe; use an index from the latest recipe list. |
| [View recorded meals](#viewing-recorded-meals) | `view-meals` | Record at least one meal to see nutrition totals. |
| [Show command help](#viewing-available-commands-help) | `help` | No prerequisites. |
| [Exit](#exiting-mishmash) | `bye` | Session data will be cleared. |

### The Meal Prep Workflow

Use this sequence to generate meals and their groceries:

1. **Add at least three recipes** with `add-recipe`.
2. **Generate meals** with `generate-plan d/3`.
3. **Review meals** with `view-plan` or `view-plan d/1`.
4. **Check daily nutrition** with `check-shortfall cal/2000 p/100`, replacing the example targets with your own. This step is optional.
5. **Generate ingredients** with `generate-list`.
6. **Check your shopping list** with `view-list`.

After generating a replacement plan, generate the grocery list again so the ingredients match the meals you intend to prepare.

To track what you actually eat, use this separate sequence at any time after adding recipes:

1. **Find the recipe** you ate with `list-recipes` and note its number.
2. **Record the meal** with `record-meal 1`, replacing `1` with that number.
3. **Review your intake** with `view-meals`, which shows each recorded meal's nutrition and the totals.

## Setting Dietary Preferences

**Purpose:** Choose a named diet, avoid food categories, or exclude complete ingredient names.
These preferences apply to future plans and can update an existing plan.

**Format:**

```text
set-diet [diet/DIET] [avoid/TAG[,TAG...]] [exclude/INGREDIENT[,INGREDIENT...]]
```

**Before You Start:** Preferences can be saved before adding recipes. To generate or repair a
restricted plan, add at least three compatible recipes with complete `food/` declarations.

**Example:**

```text
set-diet diet/vegan avoid/peanut,soy exclude/mushroom,bell pepper
```

With no active meal plan, the result is:

```text
Dietary preferences saved. Future meal plans will use them.
```

**Rules:**

- Supply at least one field. Each prefix may appear once, in any order.
- Each command **replaces the entire profile**. Omitted fields are cleared. For example,
  `set-diet avoid/peanut` removes a previously selected vegan diet and any ingredient exclusions.
  Include every restriction you want to retain.
- Command names and prefixes are case-sensitive. Diet and food-tag values ignore case and
  surrounding spaces; hyphens and underscores are accepted in multiword values.
- Separate categories and ingredient exclusions with commas. Blank entries and duplicates are rejected,
  including aliases of the same tag and ingredient names equal after normalization.
- `diet/none` removes the named diet only; any `avoid/` and `exclude/` fields in the same command still apply.
- Preferences and recipe classifications last only for the current session.

### Supported Diets and Food Categories

| Diet | Excluded categories |
| --- | --- |
| `none` (alias: `omnivore`) | No exclusions from a named diet. |
| `pescatarian` | Beef, pork, poultry, other meat, meat derivatives, and other animal products. |
| `vegetarian` (alias: `lacto-ovo-vegetarian`) | All pescatarian exclusions, plus fish, crustaceans, molluscs, and gelatin. |
| `lacto-vegetarian` | All vegetarian exclusions, plus eggs. |
| `ovo-vegetarian` | All vegetarian exclusions, plus milk and lactose. |
| `vegan` | All vegetarian exclusions, plus milk, lactose, eggs, and honey. |

Supported tags for `food/` and `avoid/` are:

```text
milk, lactose, egg, peanut, tree-nut, soy, wheat, gluten,
fish, crustacean, mollusc, sesame, mustard, celery, lupin, sulphite,
beef, pork, poultry, other-meat, meat-derivative, gelatin, honey,
other-animal-product, alcohol, coconut, corn, oat, legume, allium, mushroom
```

Aliases are `dairy` (milk), `soya` (soy), `sulfite` (sulphite), `eggs` (egg),
`peanuts` (peanut), `tree-nuts` (tree-nut), `crustaceans` (crustacean), and
`molluscs` (mollusc). Use `avoid/peanut,tree-nut` to exclude both nut categories.
Categories are matched independently: for example, declare both `milk,lactose` or
`wheat,gluten` when both apply, and include `legume` alongside specific legume categories where applicable.

### Classifying Recipes

Append the optional `food/TAG[,TAG...]` field when adding a recipe. Declare **every applicable
category**, including categories in sauces, stocks, and compound ingredients:

```text
add-recipe n/Pancakes cal/400 p/12 c/60 f/10 i/Flour:100g i/Milk:150ml i/Egg:1pc food/milk,lactose,egg,wheat,gluten
add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g food/none
```

`food/none` explicitly declares that no supported categories apply. Omitting `food/` leaves
the recipe **unclassified**. Any active restriction excludes unclassified recipes, including an
ingredient-only exclusion. `food/none` cannot be combined with tags; `avoid/none` is not supported.

MishMash uses your declarations; it does not identify undeclared allergens or cross-contact.
Category names describe food content, not halal or kosher certification.
`meat-derivative` covers land-animal derivatives such as stock and lard; also declare the source
category where known. Declare both `gelatin` and its source category where known.
Use `other-animal-product` for animal-derived ingredients without a more specific category.
The vegetarian, vegan, and pescatarian presets exclude that category conservatively.

Ingredient exclusions match **complete names**, ignoring case, surrounding spaces, and repeated
spaces. They do not infer synonyms or match substrings: `exclude/chicken` does not exclude
`Chicken Breast`; use `avoid/poultry` for the category. Commas separate exclusions and cannot
be part of an excluded name.

### Updating an Existing Plan

| Current state | Result of a valid dietary update |
| --- | --- |
| No active plan | Save preferences for future generation. |
| Every planned recipe is compatible | Save preferences; preserve all days and groceries. |
| Some days conflict and replacement succeeds | Preserve compatible days in their original positions; replace each conflicting day in full. |
| A grocery list exists when repair succeeds | Rebuild it from every meal occurrence in the updated plan. |
| No grocery list exists when repair succeeds | Keep groceries ungenerated; use `generate-list` when needed. |
| Replacement fails | Save preferences and clear the previous plan and grocery list. |

Replacement uses the original daily nutrition targets and the generator's +/-15% tolerance.
All three meals in a conflicting day may change. At least three compatible, classified recipes
are needed while restrictions are active; having three alone does not guarantee that the
nutrition targets can be met. For large recipe pools, generation checks a bounded sample of combinations.

After successful repair with an existing grocery list:

```text
Dietary preferences saved. Conflicting days were replaced.
Your grocery list has also been updated.
```

**Example: Generate a Restricted Plan**

In a fresh session, enter these commands one line at a time after entering your name:

```text
add-recipe n/Rice cal/600 p/10 c/20 f/5 i/Rice:200g food/none
add-recipe n/Beans cal/600 p/10 c/20 f/5 i/Beans:200g food/legume
add-recipe n/Tofu cal/600 p/10 c/20 f/5 i/Tofu:200g food/soy,legume
set-diet diet/vegan
generate-plan d/2 cal/1800
view-plan
generate-list
view-list
```

Each day contains these three recipes in an order that may vary. Each day's totals are
1800 kcal, 30 g protein, 60 g carbs, and 15 g fats. The grocery list contains 400 g each
of Rice, Beans, and Tofu; item order may vary. The nutrition values are example inputs.

**Errors and Recovery:**

| Input or situation | Response | What to do |
| --- | --- | --- |
| `set-diet diet/keto` | `Invalid diet 'keto'.`, followed by valid diets, aliases, and command format. | Choose a supported value, such as `set-diet diet/vegan`. |
| `set-diet vegan` | Invalid dietary input, supported prefixes, valid diets, and format. | Add a prefix: `set-diet diet/vegan`. |
| `set-diet` | Missing dietary input and guidance. | Supply at least one field. |
| `set-diet avoid/milk,dairy` | `Repeated food tag: milk.` and format. | Use `set-diet avoid/milk`. |
| `set-diet avoid/peanut,` | Blank food-tag error, valid tags, and format. | Remove the trailing comma. |
| `set-diet diet/vegan diet/vegetarian` | `Repeated field: diet/` and format. | Supply the intended diet once. |
| `set-diet exclude/Chicken, chicken` | Repeated excluded ingredient error and format. | Supply each normalized name once. |
| Fewer than three compatible recipes, or no acceptable replacement found | Preferences are saved; a failed-repair explanation is shown and the old plan and groceries are cleared. | Add suitable classified recipes or adjust targets, then use `generate-plan`. |

Malformed commands preserve preferences, plans, and groceries. A **valid update with failed
repair saves the new preferences and clears generated data**. Check `view-diet` and generate
a new plan before viewing groceries again.

## Viewing Dietary Preferences

Shows the active dietary profile without changing recipes, preferences, meals, or groceries.

**Before You Start:** No prerequisites. A fresh session has no restrictions.

**Format:**

```text
view-diet
```

**Example:** After `set-diet diet/vegan avoid/peanut,soy exclude/mushroom,bell pepper`, enter `view-diet`.

**Example Output:**

```text
Diet: vegan
Avoided food categories: peanut, soy
Excluded ingredients: bell pepper, mushroom
```

**Rules:** Exclusions appear in sorted order. Empty lists display `none`. The diet also displays
`none` in a fresh session. Viewing reads the current profile when executed and never regenerates meals.

**Errors and Recovery:** `view-diet extra` produces `view-diet does not accept arguments.`
followed by `Format: view-diet`. Retry with the command alone; session data is unchanged.

## Reading Command Formats

- Enter each command on **one line**. Long examples may wrap visually; do not insert a line break within a command.
- Command words and prefixes are **case-sensitive**. Use `view-plan`, not `VIEW-PLAN`, and `d/`, not `D/`.
- Uppercase words such as `NAME` and `DAY_NUMBER` are placeholders. Replace them with your own values.
- Square brackets indicate optional input. Do not type the brackets. For example, `view-plan [d/DAY_NUMBER]` means either `view-plan` or `view-plan d/1`.
- Separate arguments with spaces. Names may contain spaces without quotation marks, for example `n/Chicken and Rice`.
- For recipe ingredients, use `NAME:QUANTITY_UNIT`, such as `Chicken Breast:200g` or `Olive Oil:2.5ml`. Repeat `i/` for each ingredient.
- `/` is reserved for argument prefixes and is not accepted inside recipe field values.

| Prefix or Abbreviation | Meaning |
| --- | --- |
| `n/` | Recipe name. |
| `d/` | Number of days for `generate-plan`; a day number for `view-plan`. |
| `cal/`, `kcal` | Calories, measured in kilocalories. |
| `p/`, `P` | Protein, measured in grams. |
| `c/`, `C` | Carbohydrates, measured in grams. |
| `f/`, `F` | Fats, measured in grams. |
| `i/` | Ingredient name, quantity, and unit. |
| `food/` | Complete recipe food-category declaration; `none` means no supported categories apply. |
| `diet/`, `avoid/`, `exclude/` | Named diet, avoided categories, and complete ingredient exclusions. |

## Adding a Recipe

<!-- OWNER: Pei Ling. Cover required fields, nutrition limits, positive quantities, allowed units, repeated ingredients, and duplicate recipes. -->

**Purpose:** {Explain what adding a recipe lets the user do.}

**Format:**

```text
add-recipe n/NAME cal/CALORIES p/PROTEIN c/CARBS f/FATS i/INGREDIENT:QUANTITY_UNIT [i/INGREDIENT:QUANTITY_UNIT ...]
```

**Before You Start:** {State prerequisites, or say there are none.}

**Example:** {Add a complete command that a user can copy.}

**Result:** {Show the actual output from the application.}

**Rules:** {Describe required values, limits, and effects on existing data.}

**Errors and recovery:** {Give invalid-input examples, their messages, and corrected commands.}

<!-- @@author neil0618 -->
## Listing Recipes

Shows the recipes in your recipe book. Use it to find a recipe's number, which commands such as [`edit-recipe`](#editing-a-recipe) and [`delete-recipe`](#deleting-a-recipe) ask for, or to check one recipe in full. Listing never changes your recipes, meal plan, or grocery list.

**Before You Start:** Add at least one recipe with `add-recipe`.

**Format:**

```text
list-recipes [INDEX]
```

| Command | What You See |
| --- | --- |
| `list-recipes` | Every recipe, numbered from 1 in the order you added them, with its calories. |
| `list-recipes 2` | The full details of recipe 2: nutrition, ingredients, and food tags. |

**Example:**

In a fresh session, after entering your name, enter these commands one line at a time:

```text
add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15 i/Chicken Breast:200g i/White Rice:150g i/Olive Oil:10ml food/poultry
add-recipe n/Protein Oatmeal cal/450 p/35 c/60 f/8 i/Rolled Oats:50g i/Whey Protein:30g
add-recipe n/Salmon Salad cal/550 p/42 c/20 f/32 i/Salmon Fillet:200g i/Mixed Greens:150g
list-recipes
list-recipes 1
```

**Example Output:**

`list-recipes` shows:

```text
Recipes:
1. Chicken and Rice (650 kcal)
2. Protein Oatmeal (450 kcal)
3. Salmon Salad (550 kcal)
```

`list-recipes 1` shows:

```text
Chicken and Rice
650 kcal | Protein: 50g | Carbs: 70g | Fats: 15g
Ingredients:
- Chicken Breast: 200g
- White Rice: 150g
- Olive Oil: 10ml
Food tags: poultry
```

**Rules:**

- `INDEX` is optional. When given, it must be a single positive whole number, counting from 1.
- The numbers are positions in the current list, and they are the numbers other commands use. They change when a recipe is deleted, so run `list-recipes` again before using a number.
- Calories are in kcal; protein, carbs, and fats are in grams.
- `Food tags` shows `Unclassified` if the recipe was added without `food/`, `None declared` if it was added with `food/none`, and otherwise the declared tags.
- With no recipes, `list-recipes` replies `No recipes yet. Use 'add-recipe' to add one.` This is a normal message, not an error.

**Errors and Recovery:**

| Situation | Response | What to Do |
| --- | --- | --- |
| `list-recipes 1` with no recipes | `Error: There are no recipes to show. Use 'add-recipe' to add one.` | Add a recipe, then try again. |
| `list-recipes 4` with only three recipes | `Error: Recipe index must be between 1 and 3.` | Enter `list-recipes` and choose a number shown. |
| `list-recipes 0` | `Error: Recipe index must be at least 1.` | Recipe numbers start at 1. |
| `list-recipes abc`, `list-recipes -1`, `list-recipes 1.5`, or `list-recipes 1 2` | `Error: Recipe index must be a single positive whole number.` | Enter one whole number, for example `list-recipes 2`. |

The "no recipes" and "between 1 and N" errors are followed by the list of all command formats. The other errors show only `Format: list-recipes [INDEX]`. Enter the corrected command at the next prompt; errors never change your recipes.
<!-- @@author -->

## Editing a Recipe

Update a saved recipe's name, nutrition, ingredients, quantities, or food tags when you change how you prepare it.

**Before You Start:** Add a recipe, then use `list-recipes` to find its current index. Indexes start at 1. If names are duplicated, choose by index; use `list-recipes INDEX` to check the details.

**Format:**

```text
edit-recipe INDEX [n/NAME] [cal/CALORIES] [p/PROTEIN] [c/CARBS] [f/FATS] [i/INGREDIENT:QUANTITYUNIT ...] [food/TAG[,TAG...]]
```

Supply at least one field. Omitted fields keep their current values. Fields can appear in any order, separated by spaces. Repeat only `i/`; each other prefix can appear once.

**Example:** In a fresh session, enter these commands after your name:

```text
add-recipe n/Chicken Rice cal/650 p/50 c/70 f/15 i/Chicken:200g i/Rice:150g food/poultry
edit-recipe 1 cal/700 p/55 i/Chicken:220g i/Rice:150g
```

The edit produces:

```text
Recipe updated!
Chicken Rice
700 kcal | Protein: 55g | Carbs: 70g | Fats: 15g
Ingredients:
- Chicken: 220g
- Rice: 150g
Food tags: poultry
Existing meal plans and grocery lists are unchanged. Generate a new plan and grocery list to use this edit.
```

For a smaller correction, `edit-recipe 1 p/60` changes only the protein value.

**Rules:**

- Nutrition values are whole numbers from 0 to 2,147,483,647. Calories use kcal; protein, carbs, and fats use grams. MishMash uses the nutrition you enter and does not calculate it from ingredients.
- If you supply any `i/` fields, they replace the **entire ingredient list**. Include every ingredient you want to keep. To remove an ingredient, omit it from the replacement list. Every ingredient needs a name, a positive whole or decimal quantity, and a unit containing letters, such as `g` or `ml`.
- Names can contain spaces and must not be blank. `/` is reserved for prefixes.
- `food/` replaces the complete food-category declaration. Use the supported tags listed by `help`, such as `poultry`, `milk`, `egg`, or `soy`, separated by commas. Use `food/none` when none apply. Omitted `food/` preserves the old declaration, including an unclassified declaration. When changing ingredients, supply updated tags if the applicable categories change; tags are not inferred from ingredient names.
- Editing preserves recipe indexes and the number of recipes. Existing plans, nutrition shortfall results, and groceries continue to use the recipes chosen when the plan was generated. To use edited recipes, run `generate-plan` again, followed by `generate-list`. Running only `generate-list` still uses the existing plan.
- Edits are held for the current session and are lost when the app closes.

**Errors and recovery:** Invalid edits leave the recipe unchanged; correct the command and try again.

| Input or situation | Message and recovery |
| --- | --- |
| No recipes have been added | `There are no recipes to edit.` Add a recipe first. |
| `edit-recipe 1` | `Provide at least one recipe field to edit.` Add a field such as `p/55`. |
| `edit-recipe 0 p/55` | `Recipe index must be at least 1.` Use the latest numbered recipe list. |
| Index exceeds the recipe count | `Recipe index must be between 1 and N.` Here `N` is the number of recipes. |
| `edit-recipe 1 p/-5` | `Protein must be a non-negative integer.` Use a whole number such as `p/55`. |
| `edit-recipe 1 i/Rice:0g` | `Ingredient quantity must be greater than zero.` Supply a positive quantity and the complete replacement ingredient list. |
| A prefix is blank, unknown, or repeated | The error identifies the invalid field and shows edit usage. Supply a nonblank value and use each prefix once, except repeated `i/`. |

<!-- @@author neil0618 -->
## Deleting a Recipe

Removes one recipe from your recipe book, chosen by its number. Because a deleted recipe cannot be brought back, MishMash names the recipe and asks you to confirm first.

**Before You Start:** Add at least one recipe, then use `list-recipes` to find its number.

**Format:**

```text
delete-recipe INDEX
```

| Parameter | Meaning |
| --- | --- |
| `INDEX` | The number of the recipe to delete, as shown by `list-recipes`. The first recipe is number 1. |

**Example:**

In a fresh session, after entering your name, enter these commands one line at a time:

```text
add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15 i/Chicken Breast:200g i/White Rice:150g i/Olive Oil:10ml
add-recipe n/Protein Oatmeal cal/450 p/35 c/60 f/8 i/Rolled Oats:50g i/Whey Protein:30g
add-recipe n/Salmon Salad cal/550 p/42 c/20 f/32 i/Salmon Fillet:200g i/Mixed Greens:150g
delete-recipe 2
```

MishMash asks you to confirm. Enter `y` to delete the recipe.

**Example Output:**

In this transcript, `delete-recipe 2` and `y` are the lines you type; the other lines are MishMash's replies.

```text
delete-recipe 2
Are you sure you want to delete recipe 2: Protein Oatmeal? (y/n)
y
Recipe deleted!
Protein Oatmeal
Recipes remaining: 2
```

`list-recipes` now shows that the later recipe has moved up one number:

```text
Recipes:
1. Chicken and Rice (650 kcal)
2. Salmon Salad (550 kcal)
```

If you answer anything other than `y` or `yes`, nothing is deleted:

```text
delete-recipe 2
Are you sure you want to delete recipe 2: Salmon Salad? (y/n)
n
Cancelled. Nothing was changed.
```

**Rules:**

- Supply exactly one `INDEX`, written as a positive whole number such as `2`.
- Check that the recipe named in the question is the one you meant before you answer. Only `y` or `yes`, in any letter case, confirms. Any other answer, including pressing Enter on its own, cancels the deletion.
- Recipes after the deleted one move up by one number. Run `list-recipes` again before using another number.
- Deleting a recipe does not change an existing meal plan or grocery list. They keep the meals they were generated with, including the deleted recipe. To plan without it, run [`generate-plan`](#generating-a-meal-plan) again, then `generate-list`. A new plan needs at least three recipes.
- There is no undo. To get a deleted recipe back, add it again with `add-recipe`.

**Errors and Recovery:**

| Situation | Response | What to Do |
| --- | --- | --- |
| No recipes have been added | `Error: There are no recipes to delete.` | Add a recipe first. |
| `delete-recipe 5` with only two recipes | `Error: Recipe index must be between 1 and 2.` | Enter `list-recipes` and choose a number shown. |
| `delete-recipe` | `Error: Recipe index is required.` | Add a number, for example `delete-recipe 1`. |
| `delete-recipe 0` | `Error: Recipe index must be at least 1.` | Recipe numbers start at 1. |
| `delete-recipe abc`, `delete-recipe -1`, `delete-recipe 1.5`, or `delete-recipe 1 2` | `Error: Recipe index must be a single positive whole number.` | Enter one whole number, for example `delete-recipe 2`. |
| You answer something other than `y` or `yes` | `Cancelled. Nothing was changed.` | Enter `delete-recipe` again and answer `y`. |

An index problem is reported before MishMash asks you to confirm, so nothing is deleted. The "no recipes" and "between 1 and N" errors are followed by the list of all command formats. The other errors show only `Format: delete-recipe INDEX`. Enter the corrected command at the next prompt.
<!-- @@author -->

## Generating a Meal Plan

Builds a plan containing three distinct recipes per day, using recipes from your recipe book.
Recipes may appear again on different days. Current dietary preferences apply to recipe selection.

**Format:**

```text
generate-plan d/DAYS [cal/DAILY_CALORIES] [p/DAILY_PROTEIN] [c/DAILY_CARBS] [f/DAILY_FATS]
```

**Before You Start:** Add at least three recipes using `add-recipe`. If dietary restrictions are active,
at least three classified recipes must satisfy them.

**Example:**

```text
generate-plan d/2 cal/1700 p/85
```

**Result, when matching recipes are available:**

```text
Meal plan successfully generated for 2 day(s)!
Use 'view-plan' to inspect your planned meals.
```

**Rules:**

- Request between 1 and 7 days. Each optional daily target must be a positive whole number up to 2147483647.
- Calories use kcal; protein, carbohydrates, and fats use grams. Omitted nutrients have no target.
- Without targets, meals are selected randomly. With targets, every day's totals must be within ±15% of
  every supplied target. For large recipe books, generation searches a bounded sample of combinations.
- The application retains your supplied targets. `view-plan` warns whenever a daily total exceeds its exact
  target, even when that day is within the generation tolerance. These warnings are informational.
- Successful generation replaces the active plan and its targets and clears its generated grocery list.
  A replacement generated without targets has no nutrition warnings. Plans and targets last only for this session.

**Errors and Recovery:** If generation reports that it cannot find three recipes meeting your daily targets
within 15%, try different targets or add recipes. If fewer than three eligible recipes are available, add
or classify suitable recipes before retrying. Failed generation leaves the previous plan, targets, and
grocery list intact.

## Viewing a Meal Plan

Displays the meals in your active plan, with nutrition for each meal and totals for each day. Viewing a plan does not generate new meals or change the plan.

**Before You Start:** Add at least three recipes, then generate a meal plan using `generate-plan d/2` (or choose another number of days from 1 to 7).

**Format:**

```text
view-plan [d/DAY_NUMBER]
```

| Command | What You See |
| --- | --- |
| `view-plan` | Every day in the active plan, in day order. |
| `view-plan d/1` | Only Day 1. |
| `view-plan d/2` | Only Day 2, if the active plan contains that day. |

**Example:**

```text
view-plan d/1
```

**Example Output:**

```text
================ Day 1 ================
Meal 1: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)
Meal 2: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)
Meal 3: Salmon Salad (550 kcal | P: 42g, C: 20g, F: 32g)
--------------------------------------
Daily Totals: 1650 kcal | Protein: 127g | Carbs: 150g | Fats: 55g
======================================
```

This is an illustrative day. Your meals and their order depend on the generated plan. `view-plan` displays a block like this for each day, separated by a blank line. Meal numbering starts again at 1 for each day.

<!-- @@author oilybikechain -->
**Daily Nutrition Totals:** Each nutrient is added independently for the displayed day, using the
full nutrition values entered for its meals. In the example, calories are `450 + 650 + 550 = 1650`
kcal, protein is `35 + 50 + 42 = 127` g, carbohydrates are `60 + 70 + 20 = 150` g, and fats are
`8 + 15 + 32 = 55` g. Calories are measured in kcal; all three macronutrients are measured in grams.
Totals describe planned meals rather than food recorded as eaten. Values are not rounded or adjusted
to fit a target, and large daily sums can exceed a single recipe's maximum value of 2147483647.
<!-- @@author -->

**Rules:**

- Day numbers start at **1**, rather than 0. Day 1 is the first day in the plan, not a calendar date.
- Supply at most one `d/` argument, containing a positive whole number.
- If a plan has three days, valid selections are `d/1`, `d/2`, and `d/3`.
- Daily totals add the nutrition of every meal shown for that day; they are not totals for the whole plan.
- After a replacement plan is generated, viewing shows the currently active plan.

**Errors and Recovery:**

| Situation | Response | What to Do |
| --- | --- | --- |
| No active plan | `No active meal plan. Use 'generate-plan' first.` | Generate a plan, then enter `view-plan` again. |
| Day exceeds the plan's length | For a two-day plan: `Error: Day number must be between 1 and 2.` | Enter `view-plan d/1` or `view-plan d/2`. |
| Zero, negative, decimal, or overflowing day number | A day-number error and `Format: view-plan [d/DAY_NUMBER]`. | Use a positive whole number within the plan's length. |
| Unexpected or repeated arguments | A format error and `Format: view-plan [d/DAY_NUMBER]`. | Use only `view-plan` or one `d/` argument. |

For example, after generating a two-day plan, entering `view-plan d/3` displays:

```text
Error: Day number must be between 1 and 2.
Format: view-plan [d/DAY_NUMBER]
------------------------------------------------------------
Enter a command, or type 'help' for all commands or 'bye' to exit.
```

The application remains open. Enter `view-plan d/2` to select a valid day. If there is no active plan, a syntactically valid command such as `view-plan d/2` shows the no-plan message instead.

<!-- @@author oilybikechain -->
### Nutrition Target Warnings

Each day is compared with the daily targets supplied to `generate-plan`. A total strictly greater than its
target produces a warning after the daily totals, before the closing separator. Equal or lower totals and
nutrients without a target produce no warning. Warnings appear in calories, protein, carbohydrates, and fats
order and show the excess, actual total, and target. Selecting a day displays only that day's warnings.

For example, if the illustrated day above was generated with
`generate-plan d/1 cal/1500 p/120`, its daily footer would be:

```text
--------------------------------------
Daily Totals: 1650 kcal | Protein: 127g | Carbs: 150g | Fats: 55g
Warning: Calories exceed target by 150 kcal (1650 kcal actual; 1500 kcal target).
Warning: Protein exceeds target by 7 g (127 g actual; 120 g target).
======================================
```

Both totals are within the generator's ±15% tolerance but exceed the exact targets. Viewing does not change
meals or targets. To use different targets, generate a replacement plan. If `set-diet` repairs a plan,
warnings use the repaired meals and the original generation targets; if it clears the plan, no warnings remain.
`check-shortfall` uses its own explicit targets and does not change these warnings.
There is no separate excess-checking command: use `view-plan` or `view-plan d/DAY_NUMBER` after
generating a plan with targets. Generating without targets produces daily totals without warnings.
<!-- @@author -->

## Checking Nutrition Shortfalls

Use `check-shortfall` to see which days in your active meal plan fall below your supplied daily calorie, protein, carbohydrate, or fat targets, and by how much.

**Before You Start:** Add at least three recipes, then generate a meal plan. For example, use `generate-plan d/2` to create a two-day plan. You do not need to generate a grocery list before checking nutrition.

**Format:**

```text
check-shortfall [cal/DAILY_CALORIES] [p/DAILY_PROTEIN] [c/DAILY_CARBS] [f/DAILY_FATS]
```

**Rules:**

- Supply **at least one target**, even though each individual target is optional.
- Targets must be positive whole numbers from **1 to 2147483647**, with calories in kcal and macronutrients in grams. Do not include units in the command.
- Leading zeros are accepted. Signs, decimals, and separators such as commas are rejected.
- Keep each prefix and its value together: write `cal/2000`, not `cal/ 2000`.
- Each prefix can appear once, in any order. Omitted nutrients are not checked.
- The command checks **every day separately** against the same targets. There is no `d/` option; a surplus on one day does not offset another day's shortfall.
- A value equal to or above its target is not reported as a shortfall. Reported nutrients appear in calorie, protein, carbohydrate, then fat order.

**Example: Check a Two-Day Plan**

In a fresh session, enter these commands one line at a time, after entering your name:

```text
add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15 i/Chicken Breast:200g i/White Rice:150g i/Olive Oil:10ml
add-recipe n/Protein Oatmeal cal/450 p/35 c/60 f/8 i/Rolled Oats:50g i/Whey Protein:30g
add-recipe n/Salmon Salad cal/550 p/42 c/20 f/32 i/Salmon Fillet:200g i/Mixed Greens:150g
generate-plan d/2
view-plan
check-shortfall cal/2000 p/150 c/200 f/60
```

With exactly these three recipes, each day contains all three meals, although their order may vary. Each day's totals are **1650 kcal, 127 g protein, 150 g carbs, and 55 g fats**. The shortfall check displays:

```text
Daily nutrition shortfalls (compared with the supplied targets):
Day 1:
- Calories: 1650/2000 kcal (short by 350 kcal).
- Protein: 127/150 g (short by 23 g).
- Carbs: 150/200 g (short by 50 g).
- Fats: 55/60 g (short by 5 g).
Day 2:
- Calories: 1650/2000 kcal (short by 350 kcal).
- Protein: 127/150 g (short by 23 g).
- Carbs: 150/200 g (short by 50 g).
- Fats: 55/60 g (short by 5 g).
```

Each result shows `planned total/target`, followed by the difference. These targets are example inputs; replace them with the daily values you want to compare against.

**Example: Check Only Protein**

Using the same plan, enter:

```text
check-shortfall p/100
```

The result is:

```text
Daily nutrition shortfalls (compared with the supplied targets):
Day 1: No shortfalls for the supplied targets.
Day 2: No shortfalls for the supplied targets.
```

This means the planned protein meets or exceeds 100 g on each day. It does not mean every nutrient meets a target: excesses and omitted nutrients are not checked.

The check reports **any amount below the supplied target**, including amounts within the
generator's +/-15% acceptance tolerance. For example, a plan with 1900 kcal may be accepted
for a 2000 kcal generation target but will still show a 100 kcal shortfall here.

This command does not change your plan, grocery list, recipes, or generation targets.
Supply the targets each time; the check does not save them as personal settings or automatically reuse targets from `generate-plan`. It reports shortfalls without selecting replacement meals. To change the plan, use `generate-plan` again and then repeat the check.

**Errors and Recovery:**

| Situation or Input | Response | What to Do |
| --- | --- | --- |
| No active plan, with valid targets | `No active meal plan. Use 'generate-plan' first.` | Add at least three recipes and run `generate-plan d/2`, then retry the check. |
| `check-shortfall` | `Error: Supply at least one daily nutrition target.` | Add a target, for example `check-shortfall cal/2000`. |
| `check-shortfall cal/` | `Error: Missing value after cal/.` | Add a value immediately after the slash, for example `check-shortfall cal/2000`. The same rule applies to `p/`, `c/`, and `f/`. |
| `check-shortfall cal/0` | `Error: Target for cal/ must be a positive whole number from 1 to 2147483647.` | Enter a value within the supported range, for example `check-shortfall cal/2000`. The same rule applies to all four targets. |
| `check-shortfall p/100 p/120` | `Error: Parameter p/ was given more than once.` | Supply the intended target once: `check-shortfall p/120`. |
| `check-shortfall d/1` | `Error: Unrecognized parameter: d/1` | Use only `cal/`, `p/`, `c/`, or `f/`, for example `check-shortfall cal/2000`. |
| `check-shortfall cal/ 2000` | `Error: Missing value after cal/.` | Remove the space after the prefix: `check-shortfall cal/2000`. |

Invalid input also displays the command format. Enter the corrected command at the next prompt; errors leave your existing data unchanged.

<!-- @@author oilybikechain -->
## Generating a Grocery List

Combines the ingredients of every meal in the active plan into one shopping list.

**Format:**

```text
generate-list
```

**Before You Start:** Add at least three recipes and successfully run `generate-plan d/1`
(or choose another supported number of days).

**Example:**

```text
generate-list
```

**Result:**

```text
Grocery list generated. Use 'view-list' to view it.
```

**Rules:**

- This command takes no arguments and uses every day of the current meal plan.
- Each recipe contributes its ingredients every time it appears, including on different days.
- Quantities are combined only when both ingredient names and units match exactly after trimming.
  For example, `Rice:0.1g` and `Rice:0.2g` become `Rice:0.3g`, while `Rice:1kg` stays separate.
  `Rice` and `rice` are also separate. Units are not converted.
- Items follow their first appearance in the meal plan. Generation leaves recipes and planned meals unchanged.
- Running `generate-list` again replaces the stored grocery list instead of accumulating previous totals.
- A successful replacement meal plan clears the old grocery list. A failed plan generation preserves it.
- The list is kept only for the current session. Use `view-list` to inspect it.

**Errors and recovery:**

Without an active meal plan, the command displays:

```text
No active meal plan. Use 'generate-plan' first.
```

Generate a plan and then retry `generate-list`. Unexpected arguments such as `generate-list d/1` display:

```text
Error: generate-list does not accept arguments.
Format: generate-list
------------------------------------------------------------
Enter a command, or type 'help' for all commands or 'bye' to exit.
```

Retry without arguments. Invalid syntax does not change an existing grocery list.

### Ingredient Combining Example

In a fresh session, enter the following commands after supplying your name:

```text
add-recipe n/A cal/500 p/20 c/50 f/10 i/Rice:0.1g
add-recipe n/B cal/500 p/20 c/50 f/10 i/Rice:0.2g
add-recipe n/C cal/500 p/20 c/50 f/10 i/Rice:1kg i/rice:50g
generate-plan d/2
generate-list
view-list
```

There are exactly three recipes, so each appears once per day. The generated list contains these
three entries; their display order depends on the order of the meals:

| Ingredient | Unit | Quantity for two days | Reason |
| --- | --- | --- | --- |
| Rice | g | 0.6 | `(0.1 + 0.2) × 2`; decimal quantities add exactly. |
| Rice | kg | 2 | `1 × 2`; kg is kept separate from g. |
| rice | g | 100 | `50 × 2`; lowercase `rice` is a different name from `Rice`. |

Running `generate-list` again keeps these totals. It recomputes the ingredients of the current plan,
so it does not double the previously generated shopping quantities. Use consistent ingredient names
and units when you want entries combined. Nutrition warnings do not prevent grocery generation.
<!-- @@author -->

## Viewing a Grocery List

Displays the grocery list that has already been generated, including each ingredient's quantity and unit. Viewing does not generate or update the list.

**Before You Start:** Generate a meal plan and then run `generate-list`.

**Format:**

```text
view-list
```

**Example:**

```text
view-list
```

**Example Output:**

Use the three `add-recipe` commands and `generate-plan d/2` from the [two-day example](#checking-nutrition-shortfalls), then run `generate-list` and `view-list`. You can generate and view groceries without running `check-shortfall`. Each sample meal is used once per day for two days, so the list contains the following quantities. Item order may vary with the meal order.

```text
================ Grocery List ================
[ ] Chicken Breast: 400g
[ ] White Rice: 300g
[ ] Olive Oil: 20ml
[ ] Rolled Oats: 100g
[ ] Whey Protein: 60g
[ ] Salmon Fillet: 400g
[ ] Mixed Greens: 300g
=============================================
Total items to purchase: 7
```

Your items and their order depend on the generated list. The number at the bottom counts displayed entries, not the sum of their quantities. For example, `Rice: 1cups` and `Rice: 200g` are two separate entries because their units differ.

**Rules:**

- `view-list` takes **no arguments**; `view-list d/1` is not supported.
- Quantities retain their units and decimal precision. A quantity such as `2.5ml` is displayed without rounding to a whole number.
- The `[ ]` markers are visual shopping-list markers. There is currently no command to tick or untick them.
- Viewing preserves the list's order and contents. Ingredient combining is done during generation, rather than viewing.
- After generating a replacement meal plan, generate the grocery list again before relying on it for shopping.

**Errors and Recovery:**

| Situation | Response | What to Do |
| --- | --- | --- |
| No generated grocery list | `No grocery list generated. Use 'generate-list' first.` | Generate a plan and grocery list, then enter `view-list`. |
| Extra arguments, such as `view-list extra` | `Error: view-list does not accept arguments.` followed by `Format: view-list`. | Enter `view-list` on its own. |

A generated list with no entries displays:

```text
================ Grocery List ================
=============================================
Total items to purchase: 0
```

This differs from having no generated list at all, which displays the generate-first message.

## Recording a Meal

Records a recipe as a meal you have eaten, so you can keep track of your actual food intake. Each time you record a meal, MishMash shows its calories and macronutrients, how its energy divides between protein, carbohydrates, and fats, and the totals of everything you have recorded so far. Recording a meal does not change your recipes, meal plan, or grocery list.

**Before You Start:** Add at least one recipe, then use `list-recipes` to see each recipe's number.

**Format:**

```text
record-meal INDEX
```

| Parameter | Meaning |
| --- | --- |
| `INDEX` | The number of the recipe you ate, as shown by `list-recipes`. The first recipe is number 1. |

**Example:**

In a fresh session, after entering your name, enter these commands one line at a time:

```text
add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15 i/Chicken Breast:200g i/White Rice:150g i/Olive Oil:10ml
add-recipe n/Protein Oatmeal cal/450 p/35 c/60 f/8 i/Rolled Oats:50g i/Whey Protein:30g
record-meal 1
record-meal 2
```

**Example Output:**

After `record-meal 1`:

```text
Meal recorded!
Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)
    Macro split: P 32% | C 46% | F 22%
Total so far: 650 kcal | Protein: 50g | Carbs: 70g | Fats: 15g
```

After `record-meal 2`:

```text
Meal recorded!
Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)
    Macro split: P 31% | C 53% | F 16%
Total so far: 1100 kcal | Protein: 85g | Carbs: 130g | Fats: 23g
```

**Rules:**

- Supply exactly **one** index, written as a positive whole number such as `2`. Signs, decimals, and extra words are rejected.
- The index must match a recipe in your current recipe list. If you delete a recipe, the numbers of the recipes after it change, so check `list-recipes` again.
- The meal is recorded with the recipe's calories and macronutrients as stored when you record it. Deleting or editing that recipe later (for example with `edit-recipe`) does not change a meal you have already recorded.
- The `Macro split` line shows what share of the meal's macronutrient energy comes from protein (P), carbohydrates (C), and fats (F). MishMash estimates it from the grams you entered, using 4 kcal per gram of protein and carbohydrates and 9 kcal per gram of fat, so it can differ from the share of the calories you entered. The three whole-number percentages add up to 100. A meal with no protein, carbohydrates, or fats shows `Macro split: n/a`.
- You can record the same recipe more than once. Each entry counts separately toward the totals.
- Recorded meals are kept only for the current session. They are separate from the meals in a generated plan.
- MishMash does not ask for confirmation. There is currently no command to remove a recorded meal.

**Errors and Recovery:**

| Situation or Input | Response | What to Do |
| --- | --- | --- |
| No recipes have been added | `Error: There are no recipes to record. Use 'add-recipe' to add one.` | Add a recipe, then enter `record-meal 1`. |
| `record-meal` | `Error: Recipe index is required.` | Add an index, for example `record-meal 1`. |
| `record-meal 5` with only three recipes | `Error: Recipe index must be between 1 and 3.` | Enter `list-recipes` to check the numbers, then use one within that range. |
| `record-meal 0` | `Error: Recipe index must be at least 1.` | Recipe numbers start at 1, for example `record-meal 1`. |
| `record-meal two`, `record-meal -1`, or `record-meal 1 2` | `Error: Recipe index must be a single positive whole number.` | Enter one whole number, for example `record-meal 2`. |

Errors in the index also display `Format: record-meal INDEX`. The "no recipes" error and the "index too large" error display the list of all command formats instead. Enter the corrected command at the next prompt; errors leave your recorded meals unchanged.

## Viewing Recorded Meals

Displays every meal you have recorded, with the calories, macronutrients, and macro split of each meal, followed by the totals for all recorded meals. Viewing does not record or change anything.

**Before You Start:** Record at least one meal using `record-meal INDEX`.

**Format:**

```text
view-meals
```

**Example:**

After the two `record-meal` commands in [Recording a Meal](#recording-a-meal), enter:

```text
view-meals
```

**Example Output:**

```text
================ Recorded Meals ================
Meal 1: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)
    Macro split: P 32% | C 46% | F 22%
Meal 2: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)
    Macro split: P 31% | C 53% | F 16%
------------------------------------------------
Total: 1100 kcal | Protein: 85g | Carbs: 130g | Fats: 23g
================================================
```

**Rules:**

- `view-meals` takes **no arguments**.
- Meals appear in the order you recorded them, and are numbered from 1.
- Each meal's line gives its calories (kcal) and its protein (P), carbohydrates (C), and fats (F) in grams. The indented `Macro split` line below it shows how that meal's energy divides between protein, carbohydrates, and fats, as explained in [Recording a Meal](#recording-a-meal).
- The total line adds up all recorded meals. It is not a daily total: MishMash does not record dates, so everything you record in the session is counted together.
- These meals are what you recorded as eaten. They are not the meals in your meal plan, and `check-shortfall` does not use them.

**Errors and Recovery:**

| Situation or Input | Response | What to Do |
| --- | --- | --- |
| No meals recorded yet | `No meals recorded yet.` | Enter `record-meal 1` (or another recipe number), then try `view-meals` again. |
| Extra arguments, such as `view-meals 1` | `Error: view-meals does not accept arguments.` followed by `Format: view-meals`. | Enter `view-meals` on its own. |

## Exiting MishMash

Enter:

```text
bye
```

MishMash displays `Goodbye!` and closes. In the current application, all session data is cleared when the application exits.

## Troubleshooting

| Problem | What to Check |
| --- | --- |
| The terminal does not recognise `java` | Install Java 25 and ensure the terminal can find it. Open a new terminal, then run `java -version`. |
| Java reports an incompatible class version | Run `java -version` in the same terminal used to launch the JAR, and select Java 25. |
| `Unable to access jarfile` | Check the filename and the terminal's current folder. Put quotes around filenames or paths containing spaces. |
| The application greets me instead of running my first command | Enter your name first. The next line is your first command. |
| `Error: Unknown command: ...` | Use the spelling and lowercase command word from the command table, or enter `help`. |
| `Error: Enter a command.` | Enter a supported command instead of a blank line. |
| A plan or grocery list is missing | Run `generate-plan d/2` after adding at least three recipes. Run `generate-list` before `view-list`, including after replacing a plan. |
| A generated plan still shows a nutrition shortfall | Generation accepts daily totals within +/-15% of supplied targets; `check-shortfall` reports any amount below its supplied targets. See [Checking Nutrition Shortfalls](#checking-nutrition-shortfalls). |
| A generated plan shows an excess warning | A daily total is above an exact generation target, even if it is within the accepted 15% tolerance. See [Nutrition Target Warnings](#nutrition-target-warnings). |
| Ingredients that look similar appear separately | `generate-list` combines exact ingredient names and units only. Check capitalization and units; see the [ingredient combining example](#ingredient-combining-example). |
| `record-meal` reports an index error | Run `list-recipes` and use a number shown there. Numbers change after `delete-recipe`. See [Recording a Meal](#recording-a-meal). |
| `view-meals` shows `No meals recorded yet.` | Record a meal first with `record-meal INDEX`. Meals in a generated plan are not recorded automatically. |
| A meal shows `Macro split: n/a` | The recipe has 0g of protein, carbohydrates, and fats, so there is nothing to divide. Check its values with `list-recipes`. |
| `delete-recipe` answers `Cancelled. Nothing was changed.` | Only `y` or `yes` confirms. Enter the command again and answer `y`. See [Deleting a Recipe](#deleting-a-recipe). |
| A deleted recipe still appears in my meal plan or grocery list | Existing plans and lists keep the meals they were generated with. Run `generate-plan`, then `generate-list`, to build new ones. |

Invalid viewing input displays an error or a generate-first message; you can enter another command without restarting.

<!-- @@author oilybikechain -->
### Recovering from Invalid Plan or Recipe Arguments

For `generate-plan`, `list-recipes`, and `delete-recipe`, invalid arguments include the relevant
`Format:` line with the error. For example:

```text
generate-plan d/0
```

produces:

```text
Error: DAYS must be a whole number from 1 to 7.
Format: generate-plan d/DAYS [cal/DAILY_CALORIES] [p/DAILY_PROTEIN] [c/DAILY_CARBS] [f/DAILY_FATS]
------------------------------------------------------------
Enter a command, or type 'help' for all commands or 'bye' to exit.
```

Correct the value and retry, for example `generate-plan d/1` after adding enough eligible recipes.
This invalid request does not change your existing plan or grocery list. If reporting a problem,
include the command, displayed error, and relevant diagnostic log; the application retains the
underlying validation cause for troubleshooting.
<!-- @@author -->

<!-- TEAM: Add feature-specific troubleshooting once listing, deletion, and generation behavior is confirmed. -->

## Frequently Asked Questions

**Do I need to read the whole guide to get started?**

No. Follow [Quick Start](#quick-start), then use [Commands at a Glance](#commands-at-a-glance) to jump to the task you need.

**Will my recipes still be here next time?**

No. The current application keeps data only for the running session. Restarting starts a fresh session.

**Does viewing generate new meals or ingredients?**

No. `view-plan` and `view-list` display existing data. Use the corresponding generation command first.

**Can I tick items off the grocery list?**

The current `[ ]` markers are display-only. No shopping-item marking command is available.

**What is the difference between my meal plan and my recorded meals?**

The meal plan, created by `generate-plan`, is what you intend to eat. Recorded meals, created by `record-meal`, are what you have actually eaten. They are separate: recording a meal does not change the plan, and `view-meals` does not show planned meals.

**Can I remove a meal I recorded by mistake?**

Not yet. There is currently no command to remove or edit a recorded meal. Restarting MishMash clears all recorded meals.

<!-- TEAM: Confirm the release download link, actual JAR filename, feature availability, sample workflow, and storage behavior before publishing. Remove unfinished fields and owner comments from the release guide. -->



### Viewing available commands: `help`

Type `help` to see every supported command, its purpose, and its syntax. This includes
`add-recipe`, `list-recipes`, `edit-recipe`, `delete-recipe`, `generate-plan`, `view-plan`,
`check-shortfall`, `generate-list`, `view-list`, `set-diet`, `view-diet`, `record-meal`, `view-meals`, `help`, and `bye`.
The `help` command takes no arguments.

A divider and an input prompt appear when the command session starts and after every
command, including invalid commands and cancelled actions. Type `bye` to exit.

