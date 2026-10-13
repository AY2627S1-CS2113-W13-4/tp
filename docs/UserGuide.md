# MishMash User Guide

MishMash is a command-line meal prep planner. Add your recipes, use them to plan meals, and check the ingredients you need to buy.

**Start Here:** [Set up and launch](#quick-start) · [Find a command](#commands-at-a-glance) · [View meals](#viewing-a-meal-plan) · [View groceries](#viewing-a-grocery-list) · [Fix a problem](#troubleshooting)

<!-- TEAM: Update availability during development. Before publishing, replace this note with the release version and its supported features. -->

## Quick Start

### Set Up and Launch

1. **Install Java 25.** Use Java 25 for this project.
2. **Download the application's JAR file** from the team's [Releases page](https://github.com/AY2627S1-CS2113-W13-4/tp/releases), when a release is available. A JAR is the file used to run the application. The current build names it `duke.jar`, even though the application is called MishMash.
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
     java -jar "duke.jar"
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

Click a task to jump directly to its instructions. Commands marked **pending** are part of the planned workflow and may be unavailable in your build.

| I Want To… | Enter | Before Using It |
| --- | --- | --- |
| [Add a recipe](#adding-a-recipe) | `add-recipe n/NAME cal/CALORIES p/PROTEIN c/CARBS f/FATS i/INGREDIENT:QUANTITY_UNIT` | Include all fields and at least one ingredient. |
| [List recipes](#listing-recipes) | `list-recipes` | Pending. |
| [Delete a recipe](#deleting-a-recipe) | `delete-recipe INDEX` | Pending; use an index from the latest recipe list. |
| [Generate meals](#generating-a-meal-plan) | `generate-plan d/3` | Pending; add at least three recipes. |
| [View every day](#viewing-a-meal-plan) | `view-plan` | Generate a meal plan first. |
| [View one day](#viewing-a-meal-plan) | `view-plan d/1` | Choose a day in the active plan. |
| [Generate groceries](#generating-a-grocery-list) | `generate-list` | Generate a meal plan first. |
| [View groceries](#viewing-a-grocery-list) | `view-list` | Generate a grocery list first. |
| [Exit](#exiting-mishmash) | `bye` | Session data will be cleared. |

### The Meal Prep Workflow

Use this sequence to generate meals and their groceries:

1. **Add at least three recipes** with `add-recipe`.
2. **Generate meals** with `generate-plan d/3`.
3. **Review meals** with `view-plan` or `view-plan d/1`.
4. **Generate ingredients** with `generate-list`.
5. **Check your shopping list** with `view-list`.

After generating a replacement plan, generate the grocery list again so the ingredients match the meals you intend to prepare.

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

<!-- OWNER: Samantha. Completed viewing documentation. Recheck availability after generation is integrated. -->

Displays the meals in your active plan, with nutrition for each meal and totals for each day. Viewing a plan does not generate new meals or change the plan.

**Before You Start:** Generate a meal plan using `generate-plan`. Your build must support meal-plan generation connected to viewing.

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
Please enter another command, or type 'bye' to exit.
```

The application remains open. Enter `view-plan d/2` to select a valid day. If there is no active plan, a syntactically valid command such as `view-plan d/2` shows the no-plan message instead.

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

The following list is illustrative: each of the three sample meals is used once per day for two days.

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
| `Error: Unknown command: ...` | Use the spelling and lowercase command word from the command table. The command may also be unavailable in your build. |
| `Error: Enter a command.` | Enter a supported command instead of a blank line. |
| A plan or grocery list is missing | Generate the required data first. Check the draft availability note if generation is not supported in your build. |

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
