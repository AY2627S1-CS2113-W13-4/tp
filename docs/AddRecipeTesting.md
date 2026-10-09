# Add-recipe validation tests

Run with Java 25 from the repository root:

```powershell
.\gradlew.bat test checkstyleMain checkstyleTest
```

On Linux or macOS, use `bash gradlew test checkstyleMain checkstyleTest`.
The JUnit report is in `build/reports/tests/test/index.html`.

The changes add 328 test invocations:

- 148 invalid parser cases, each checking the diagnostic, usage, unchanged recipe book,
  and successful execution of a subsequent valid command.
- The same 148 invalid commands through the MishMash CLI, checking error output,
  a single successful recipe addition, and normal exit.
- 26 valid boundary and formatting cases.
- One CLI case for invalid input immediately followed by end of input.
- Five CLI cases for empty, whitespace-only, and unknown commands.

The shared commands are in `src/test/resources/add-recipe-invalid.tsv`.
Each row contains a case name, expected diagnostic fragment, and command, separated by tabs.
JUnit reads this file automatically; it can also supply commands for manual console testing.

Coverage includes missing and blank fields, repeated fields, unknown and uppercase prefixes,
missing slashes and spaces, invalid nutrition values and integer overflow, missing ingredients,
malformed ingredient separators, missing names or units, nonpositive quantities,
and an invalid ingredient after a valid one.

Valid inputs include zero and maximum integer nutrition, leading zeros, precise decimal quantities,
letter-only units, tabs and extra spaces, reordered fields, Unicode names, and repeated ingredients.
Repeated ingredients remain supported; only the single-value nutrition and name fields reject duplicates.

Use spaces or tabs between fields. Slash (`/`) is reserved for prefixes and is rejected within values.
Nutrition must be an integer from 0 to 2147483647. Ingredient quantities must be positive,
use digits with an optional decimal fraction, and have a letter-only unit, such as `150g` or `2.5ml`.

Example manual session after the name prompt:

```text
add-recipe
add-recipe n/Rice cal/200 p/4 c/45 f/1
add-recipe n/Ricecal/200 p/4 c/45 f/1 i/Rice:150g
add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g
bye
```

The first three commands should display an error and the format. Only the fourth should add a recipe.
