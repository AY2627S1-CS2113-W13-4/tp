# MishMash User Guide

MishMash is a command-line meal prep planner. Add your recipes, use them to plan meals, and check the ingredients you need to buy.

**Start Here:** [Set up and launch](#quick-start) · [Find a command](#commands-at-a-glance) · [View meals](#viewing-a-meal-plan) · [Check nutrition](#checking-nutrition-shortfalls) · [View groceries](#viewing-a-grocery-list) · [Fix a problem](#troubleshooting)

This guide covers the current team version, including meal-plan viewing, grocery-list viewing, and nutrition shortfall checking.

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

> **Session Data:** Closing or restarting the current application clears the recipes, meal plan, and grocery list. There is no save or load command.

## Commands at a Glance

Click a task to jump directly to its instructions. The commands below are supported by the current team version.

| I Want To… | Enter | Before Using It |
| --- | --- | --- |
| [Add a recipe](#adding-a-recipe) | `add-recipe n/NAME cal/CALORIES p/PROTEIN c/CARBS f/FATS i/INGREDIENT:QUANTITY_UNIT` | Include all fields and at least one ingredient. |
| [List recipes](#listing-recipes) | `list-recipes [INDEX]` | Omit the index to list all recipes; supply one to see its full details. |
| [Delete a recipe](#deleting-a-recipe) | `delete-recipe INDEX` | Use an index from the latest recipe list, then confirm the deletion. |
| [Generate meals](#generating-a-meal-plan) | `generate-plan d/3` | Add at least three recipes; choose 1 to 7 days. |
| [View every day](#viewing-a-meal-plan) | `view-plan` | Generate a meal plan first. |
| [View one day](#viewing-a-meal-plan) | `view-plan d/1` | Choose a day in the active plan. |
| [Check nutrition shortfalls](#checking-nutrition-shortfalls) | `check-shortfall cal/2000 p/100` | Generate a plan first; supply at least one daily target. |
| [Generate groceries](#generating-a-grocery-list) | `generate-list` | Generate a meal plan first. |
| [View groceries](#viewing-a-grocery-list) | `view-list` | Generate a grocery list first. |
| [Set dietary preferences](#dietary-preferences-and-restrictions) | `set-diet diet/vegan avoid/peanut` | Classify recipes with `food/` before using restrictions. |
| [View dietary preferences](#viewing-and-clearing-preferences) | `view-diet` | No prerequisites. |
| [Clear dietary preferences](#viewing-and-clearing-preferences) | `clear-diet` | Removes all current dietary restrictions. |
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

## Dietary Preferences and Restrictions

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
  Use `clear-diet` to remove all restrictions.
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

### Viewing and Clearing Preferences

**Format:**

```text
view-diet
clear-diet
```

Both commands take no arguments and need no existing recipes or plan.
After the earlier `set-diet` example, `view-diet` displays:

```text
Diet: vegan
Avoided food categories: peanut, soy
Excluded ingredients: bell pepper, mushroom
```

Exclusions appear in sorted order. An empty category or ingredient list displays `none`.
After `clear-diet`, `view-diet` displays:

```text
Diet: none
Avoided food categories: none
Excluded ingredients: none
```

Clearing restrictions preserves an existing plan and its groceries. Its confirmation is
`Dietary preferences saved. Your existing meal plan already matches.` when a plan exists,
or `Dietary preferences saved. Future meal plans will use them.` otherwise.
Clearing does not restore a plan previously removed after failed repair.

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
| `view-diet extra` or `clear-diet extra` | Command does not accept arguments, followed by its format. | Enter the command alone. |
| Fewer than three compatible recipes, or no acceptable replacement found | Preferences are saved; a failed-repair explanation is shown and the old plan and groceries are cleared. | Add suitable classified recipes or adjust targets, then use `generate-plan`. |

Malformed commands preserve preferences, plans, and groceries. A **valid update with failed
repair saves the new preferences and clears generated data**. Check `view-diet` and generate
a new plan before viewing groceries again.

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
| `food/` | Complete food-category declaration when adding a recipe. |
| `diet/`, `avoid/`, `exclude/` | Named diet, avoided categories, and excluded ingredient names for `set-diet`. |

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

## Listing Recipes

<!-- OWNER: Neil. Planned command from the specification. Confirm syntax, numbered output, list order, empty-list behavior, and unexpected arguments. -->

**Purpose:** {Explain what information the recipe list displays.}

**Format:**

```text
list-recipes
```

**Before You Start:** {State prerequisites, or say there are none.}

**Example:** {Add a complete command that a user can copy.}

**Result:** {Show the actual output from the application.}

**Rules:** {Describe required values, limits, and effects on existing data.}

**Errors and recovery:** {Give invalid-input examples, their messages, and corrected commands.}

## Deleting a Recipe

<!-- OWNER: Neil. Planned command. Cover current indexes, renumbering, invalid indexes, and the effect on an existing meal plan. -->

**Purpose:** {Explain which recipe is removed and what remains.}

**Format:**

```text
delete-recipe INDEX
```

**Before You Start:** {State prerequisites, or say there are none.}

**Example:** {Add a complete command that a user can copy.}

**Result:** {Show the actual output from the application.}

**Rules:** {Describe required values, limits, and effects on existing data.}

**Errors and recovery:** {Give invalid-input examples, their messages, and corrected commands.}

## Generating a Meal Plan

<!-- OWNER: Gautham. Document the final merged behavior: minimum recipe count, day limits, target tolerance, daily meal count, replacement, and failed generation. -->

**Purpose:** {Explain how recipes become a multi-day plan.}

**Format:**

```text
generate-plan d/DAYS [cal/DAILY_CALORIES] [p/DAILY_PROTEIN] [c/DAILY_CARBS] [f/DAILY_FATS]
```

**Before You Start:** {State prerequisites, or say there are none.}

**Example:** {Add a complete command that a user can copy.}

**Result:** {Show the actual output from the application.}

**Rules:** {Describe required values, limits, and effects on existing data.}

**Errors and recovery:** {Give invalid-input examples, their messages, and corrected commands.}

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
Please enter another command, or type 'bye' to exit.
```

Retry without arguments. Invalid syntax does not change an existing grocery list.

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

Invalid viewing input displays an error or a generate-first message; you can enter another command without restarting.

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

<!-- TEAM: Confirm the release download link, actual JAR filename, feature availability, sample workflow, and storage behavior before publishing. Remove unfinished fields and owner comments from the release guide. -->



### Viewing available commands: `help`

Type `help` to see every supported command, its purpose, and its syntax. This includes
`add-recipe`, `list-recipes`, `delete-recipe`, `generate-plan`, `view-plan`,
`check-shortfall`, `generate-list`, `view-list`, `set-diet`, `view-diet`, `clear-diet`, `help`, and `bye`.
The `help` command takes no arguments.

A divider and an input prompt appear when the command session starts and after every
command, including invalid commands and cancelled actions. Type `bye` to exit.
