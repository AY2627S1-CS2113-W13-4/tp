# MishMash Console UI Test Plan

## Test configuration

- **Java:** Java 25
- **Build command:** `gradlew.bat shadowJar`
- **Run command:** `java -jar build/libs/MishMash.jar`
- **Comparison:** Compare standard output exactly after normalizing CRLF to LF. The expected blocks contain
  literal ANSI escape characters because MishMash always emits color.

## Case 1: Open and close the kitchen

**Aim** — Verifies the multi-color kitchen graphic, common-command card, personalized greeting, first prompt,
and kitchen-themed sign-off.

**Inputs**

```text
Taylor
bye
```

**Expected output**

```text
[2m            (       )     (       )[0m
[2m             )     (       )     ([0m
[2m            (       )     (       )[0m
[1;95m        .-~~~~~~~~~~~~~~~~~~~~~~~~~~~-.[0m
[1;95m       /                               \[0m
[1;93m      /          M I S H M A S H        \[0m
[1;96m     |       PLAN  |  PREP  |  ENJOY     |[0m
[1;95m      \_________________________________/[0m
[1;95m        \_____________________________/[0m
[1;95m               \_____________/[0m
[1;95mWho's cooking today?[0m
[1;93mHello Taylor - apron on, let's get cooking![0m
[1;92m[KITCHEN OPEN][0m Welcome to MishMash!
[2mPick a command to start:[0m
[1;95m+-- QUICK COMMANDS ------------------------------------+[0m
|  [1;96mhelp                [0m Full kitchen manual            |
|  [1;96mlist-recipes        [0m Browse saved recipes           |
|  [1;96mgenerate-plan d/3   [0m Build a three-day plan         |
|  [1;96mview-plan           [0m Review the active plan         |
[1;95m+------------------------------------------------------+[0m
[1;96mmishmash >[0m
[1;95m[KITCHEN CLOSED][0m Goodbye!
```

## Case 2: Add a recipe

**Aim** — Verifies the opening graphics, common commands, success label, recipe details, and repeated prompt.

**Inputs**

```text
Taylor
add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g
bye
```

**Expected output**

```text
[2m            (       )     (       )[0m
[2m             )     (       )     ([0m
[2m            (       )     (       )[0m
[1;95m        .-~~~~~~~~~~~~~~~~~~~~~~~~~~~-.[0m
[1;95m       /                               \[0m
[1;93m      /          M I S H M A S H        \[0m
[1;96m     |       PLAN  |  PREP  |  ENJOY     |[0m
[1;95m      \_________________________________/[0m
[1;95m        \_____________________________/[0m
[1;95m               \_____________/[0m
[1;95mWho's cooking today?[0m
[1;93mHello Taylor - apron on, let's get cooking![0m
[1;92m[KITCHEN OPEN][0m Welcome to MishMash!
[2mPick a command to start:[0m
[1;95m+-- QUICK COMMANDS ------------------------------------+[0m
|  [1;96mhelp                [0m Full kitchen manual            |
|  [1;96mlist-recipes        [0m Browse saved recipes           |
|  [1;96mgenerate-plan d/3   [0m Build a three-day plan         |
|  [1;96mview-plan           [0m Review the active plan         |
[1;95m+------------------------------------------------------+[0m
[1;96mmishmash >[0m
[1;92m[DONE][0m New recipe added!
Rice
200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g
Ingredients:
- Rice: 150g
Food tags: Unclassified
[1;96mmishmash >[0m
[1;95m[KITCHEN CLOSED][0m Goodbye!
```

## Latest test session

- **Status:** Passed
- **Java version:** Java 25.0.4
- **Result:** Both redesigned startup cases matched exactly after CRLF-to-LF normalization.
