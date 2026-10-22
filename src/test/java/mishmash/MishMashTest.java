package mishmash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.parser.Parser;
import mishmash.testutil.AddRecipeTestData;

/**
 * Checks CLI output and recovery after an invalid command.
 */
class MishMashTest {
    private static final String EXPECTED_BANNER = """
              __  __ _     _     __  __           _
             |  \\/  (_)___| |__ |  \\/  | __ _ ___| |__
             | |\\/| | / __| '_ \\| |\\/| |/ _` / __| '_ \\
             | |  | | \\__ \\ | | | |  | | (_| \\__ \\ | | |
             |_|  |_|_|___/_| |_|_|  |_|\\__,_|___/_| |_|

                          .-~~~~~~~~~-.
                       .-' . .. .. . '-.
                      / . .. .. .. .. . \\
                     /___________________\\
                     \\                   /
                      \\                 /
                       \\_______________/
                            \\_____/
            """;

    private static final String ADD_RICE = AddRecipeTestData.VALID_COMMAND;
    private static final String ADD_OATS = "add-recipe n/Oats cal/150 p/5 c/27 f/3 i/Oats:40g";
    private static final String CONTINUE_PROMPT = "Enter a command, or type 'help' for all commands or 'bye' to exit.";

    @Test
    void main_helpAndRecovery_promptAfterEveryCommand() {
        String output = runCli(session("help", "list-recipes", "view-plan", "view-list",
                "generate-list", "generate-plan d/1", "unknown", "", "help extra", "bye"));

        for (String word : List.of("add-recipe", "list-recipes", "delete-recipe", "generate-plan",
                "view-plan", "generate-list", "view-list", "help", "bye")) {
            assertTrue(output.contains("Format: " + word), output);
        }
        assertTrue(output.contains("help does not accept arguments."), output);
        assertEquals(10, countOccurrences(output, CONTINUE_PROMPT), output);
        assertEquals(10, countOccurrences(output,
                "------------------------------------------------------------"), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_checkShortfall_handlesMissingPlanInvalidTargetsAndSuccessfulCheck() {
        String output = runCli(session("check-shortfall cal/2000", "check-shortfall cal/0",
                "add-recipe n/A cal/600 p/30 c/50 f/10 i/Rice:100g",
                "add-recipe n/B cal/600 p/30 c/50 f/10 i/Rice:100g",
                "add-recipe n/C cal/600 p/30 c/50 f/10 i/Rice:100g",
                "generate-plan d/1", "check-shortfall cal/2000 p/100", "view-plan", "help", "bye"));

        assertTrue(output.contains("No active meal plan. Use 'generate-plan' first."), output);
        assertEquals(1, countOccurrences(output, "Error: "), output);
        assertTrue(output.contains("- Calories: 1800/2000 kcal (short by 200 kcal)."), output);
        assertTrue(output.contains("- Protein: 90/100 g (short by 10 g)."), output);
        assertTrue(output.contains("Daily Totals: 1800 kcal | Protein: 90g | Carbs: 150g | Fats: 30g"), output);
        assertTrue(output.contains("check-shortfall - Check each day's planned nutrition"), output);
        assertEquals(10, countOccurrences(output, CONTINUE_PROMPT), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_cancelledDelete_promptsOnceAfterCancellation() {
        String output = runCli(session(ADD_RICE, "delete-recipe 1", "n", "list-recipes", "bye"));
        assertEquals(4, countOccurrences(output, CONTINUE_PROMPT), output);
        assertTrue(output.contains("Cancelled. Nothing was changed." + System.lineSeparator()
                + "------------------------------------------------------------" + System.lineSeparator()
                + CONTINUE_PROMPT), output);
        assertTrue(output.contains("1. Rice (200 kcal)"), output);
    }

    //@@author peilingggg
    @TestFactory
    Stream<DynamicTest> main_invalidRecipe_recoversForNextCommand() throws IOException {
        return AddRecipeTestData.invalidCases().stream().map(testCase ->
                DynamicTest.dynamicTest(testCase[0], () -> {
                    String output = runCli("Tester" + System.lineSeparator()
                            + testCase[2] + System.lineSeparator()
                            + AddRecipeTestData.VALID_COMMAND + System.lineSeparator()
                            + "bye" + System.lineSeparator());
                    assertTrue(output.contains(testCase[1]), output);
                    assertTrue(output.contains(Parser.ADD_RECIPE_USAGE), output);
                    assertTrue(output.contains("Enter a command, or type 'help' for all commands or 'bye' to exit."),
                            output);
                    assertEquals(1, countOccurrences(output, "Error: "), output);
                    assertEquals(1, countOccurrences(output, "New recipe added!"), output);
                    assertTrue(output.contains("Rice" + System.lineSeparator()
                            + "200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g"), output);
                    assertTrue(output.contains("- Rice: 150g"), output);
                    assertTrue(output.indexOf("Error: ") < output.indexOf("New recipe added!"), output);
                    assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
                }));
    }

    @TestFactory
    Stream<DynamicTest> main_emptyOrUnknownInput_recoversForNextCommand() {
        return List.of("", "   ", "\t", "unknown", "add-recipes").stream()
                .map(input -> DynamicTest.dynamicTest("Input: [" + input + "]", () -> {
                    String output = runCli("Tester" + System.lineSeparator()
                            + input + System.lineSeparator()
                            + AddRecipeTestData.VALID_COMMAND + System.lineSeparator()
                            + "bye" + System.lineSeparator());
                    String diagnostic = input.isBlank() ? "Enter a command." : "Unknown command: " + input;
                    assertTrue(output.contains("Error: " + diagnostic), output);
                    assertEquals(1, countOccurrences(output, "Format: add-recipe"), output);
                    assertTrue(output.contains("Enter a command, or type 'help' for all commands or 'bye' to exit."),
                            output);
                    assertEquals(1, countOccurrences(output, "New recipe added!"), output);
                    assertTrue(output.indexOf("Error: ") < output.indexOf("New recipe added!"), output);
                    assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
                }));
    }

    @Test
    void main_invalidRecipeAtEndOfInput_exitsWithoutCrashing() {
        String output = runCli("Tester" + System.lineSeparator() + "add-recipe");
        assertTrue(output.contains("Error: Expected prefixed recipe arguments."), output);
        assertTrue(output.contains(Parser.ADD_RECIPE_USAGE), output);
        assertEquals(0, countOccurrences(output, "New recipe added!"), output);
    }
    //@@author

    @Test
    void main_viewingWithoutGeneratedData_displaysPromptsAndContinues() {
        String output = runCli(String.join(System.lineSeparator(),
                "Tester", "view-plan", "view-plan d/2", "view-list", "bye"));

        assertEquals(2, countOccurrences(output, "No active meal plan."), output);
        assertTrue(output.contains("No grocery list generated. Use 'generate-list' first."), output);
        assertEquals(0, countOccurrences(output, "Error: "), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_invalidViewingArguments_recoversWithRelevantUsage() {
        String output = runCli(String.join(System.lineSeparator(), "Tester",
                "view-list extra", "view-plan d/0", "view-list",
                AddRecipeTestData.VALID_COMMAND, "bye"));

        assertEquals(2, countOccurrences(output, "Error: "), output);
        assertEquals(1, countOccurrences(output, "Format: view-list"), output);
        assertEquals(1, countOccurrences(output, "Format: view-plan"), output);
        assertEquals(0, countOccurrences(output, "Format: add-recipe"), output);
        assertTrue(output.contains("No grocery list generated. Use 'generate-list' first."), output);
        assertTrue(output.contains("New recipe added!"), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_invalidThenValid_displaysExpectedOutput() {
        String input = String.join(System.lineSeparator(),
                "Tester",
                "add-recipe",
                "add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g",
                "bye") + System.lineSeparator();

        String output = runCli(input);

        String expected = EXPECTED_BANNER + System.lineSeparator()
                + String.join(System.lineSeparator(),
                        "What is your name?",
                        "Hello Tester",
                        "Welcome to MishMash!",
                        "Type 'help' to view all commands and what they do, or 'bye' to exit.",
                        "------------------------------------------------------------",
                        CONTINUE_PROMPT,
                        "Error: Expected prefixed recipe arguments.",
                        Parser.ADD_RECIPE_USAGE,
                        "------------------------------------------------------------",
                        CONTINUE_PROMPT,
                        "New recipe added!",
                        "Rice",
                        "200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
                        "Ingredients:",
                        "- Rice: 150g",
                        "------------------------------------------------------------",
                        CONTINUE_PROMPT,
                        "Goodbye!") + System.lineSeparator();

        assertEquals(expected, output);
    }

    @Test
    void main_repeatedSessions_logsSubpackagesAndRestoresConfiguration() {
        Logger appLogger = Logger.getLogger("mishmash");
        Level originalLevel = appLogger.getLevel();
        boolean wasUsingParentHandlers = appLogger.getUseParentHandlers();
        List<Handler> originalHandlers = Arrays.asList(appLogger.getHandlers());
        RecordingHandler recordingHandler = new RecordingHandler();
        recordingHandler.setLevel(Level.ALL);
        appLogger.addHandler(recordingHandler);
        // A non-default level checks that application setup restores the caller's configuration.
        appLogger.setLevel(Level.WARNING);
        try {
            for (int session = 0; session < 2; session++) {
                String output = runCli(String.join(System.lineSeparator(),
                        "Tester", "add-recipe", AddRecipeTestData.VALID_COMMAND, "view-list", "bye"));
                assertTrue(output.contains("New recipe added!"), output);
                assertEquals(Level.WARNING, appLogger.getLevel());
                assertEquals(wasUsingParentHandlers, appLogger.getUseParentHandlers());
                List<Handler> expectedHandlers = new ArrayList<>(originalHandlers);
                expectedHandlers.add(recordingHandler);
                assertEquals(expectedHandlers, Arrays.asList(appLogger.getHandlers()));
            }

            List<LogRecord> records = recordingHandler.records;
            assertEquals(2, records.stream().filter(record -> record.getLevel() == Level.INFO
                    && record.getMessage().equals("MishMash started")).count());
            assertEquals(2, records.stream().filter(record -> record.getLevel() == Level.INFO
                    && record.getMessage().equals("MishMash stopped")).count());
            assertEquals(2, records.stream().filter(record -> record.getMessage().equals("Command rejected")
                    && record.getThrown() != null).count());
            assertEquals(2, records.stream().filter(record -> record.getLoggerName().equals(Parser.class.getName())
                    && record.getLevel() == Level.FINE
                    && record.getMessage().startsWith("Parsed add-recipe with ")).count());
            assertEquals(2, records.stream().filter(record -> record.getLoggerName()
                    .equals("mishmash.command.ViewListCommand")).count());
        } finally {
            appLogger.removeHandler(recordingHandler);
            appLogger.setLevel(originalLevel);
            appLogger.setUseParentHandlers(wasUsingParentHandlers);
        }
    }

    @Test
    void main_runtimeFailure_logsCauseAndRethrowsAfterCleanup() {
        IllegalStateException failure = new IllegalStateException("Broken application dependency");
        InputStream failingInput = new InputStream() {
            @Override
            public int read() {
                throw failure;
            }
        };

        assertFailureIsLoggedAndRethrown(failingInput, failure);
    }

    @Test
    void main_assertionFailure_logsCauseAndRethrowsAfterCleanup() {
        AssertionError failure = new AssertionError("Broken internal invariant");
        InputStream failingInput = new InputStream() {
            @Override
            public int read() {
                throw failure;
            }
        };

        assertFailureIsLoggedAndRethrown(failingInput, failure);
    }

    /**
     * Checks that an unexpected failure is logged once and rethrown with logging configuration restored.
     *
     * @param failingInput The console stream that throws the expected failure when read.
     * @param expectedFailure The original exception or assertion error that must reach the caller.
     */
    private void assertFailureIsLoggedAndRethrown(InputStream failingInput, Throwable expectedFailure) {
        Logger appLogger = Logger.getLogger("mishmash");
        Level originalLevel = appLogger.getLevel();
        boolean wasUsingParentHandlers = appLogger.getUseParentHandlers();
        List<Handler> expectedHandlers = new ArrayList<>(Arrays.asList(appLogger.getHandlers()));
        RecordingHandler recordingHandler = new RecordingHandler();
        expectedHandlers.add(recordingHandler);
        appLogger.addHandler(recordingHandler);

        try {
            Throwable thrown = assertThrows(expectedFailure.getClass(), () -> runCli(failingInput));
            assertSame(expectedFailure, thrown);
            List<LogRecord> failures = recordingHandler.records.stream()
                    .filter(record -> record.getLevel() == Level.SEVERE).toList();
            assertEquals(1, failures.size());
            assertSame(expectedFailure, failures.get(0).getThrown());
            assertEquals(originalLevel, appLogger.getLevel());
            assertEquals(wasUsingParentHandlers, appLogger.getUseParentHandlers());
            assertEquals(expectedHandlers, Arrays.asList(appLogger.getHandlers()));
        } finally {
            appLogger.removeHandler(recordingHandler);
            appLogger.setLevel(originalLevel);
            appLogger.setUseParentHandlers(wasUsingParentHandlers);
        }
    }

    @Test
    void greet_nullScanner_rejectsBeforeDisplayingPrompt() {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            NullPointerException exception = assertThrows(NullPointerException.class, () -> MishMash.greet(null));
            assertEquals("scanner", exception.getMessage());
            assertEquals(0, output.size());
        } finally {
            System.setOut(originalOutput);
        }
    }

    /**
     * Captures records propagated from application subpackages for logging integration checks.
     */
    private static class RecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
            // Records are held in memory, so no flushing is needed.
        }

        @Override
        public void close() {
            // This handler owns no external resources.
        }
    }

    /**
     * Runs a CLI session and restores the global streams even if execution fails.
     *
     * @param input The simulated console input.
     * @return The captured output.
     */
    private String runCli(String input) {
        return runCli(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    //@@author peilingggg
    /**
     * Runs a CLI session with a supplied input stream, including one that simulates a failure.
     *
     * @param input The simulated console stream.
     * @return The captured output.
     */
    private String runCli(InputStream input) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(input);
            System.setOut(capturedOutput);
            MishMash.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
    //@@author

    /**
     * Counts literal message occurrences without interpreting punctuation as regular expressions.
     *
     * @param output The captured console text.
     * @param message The non-empty message to count.
     * @return The number of occurrences.
     */
    private int countOccurrences(String output, String message) {
        return output.split(Pattern.quote(message), -1).length - 1;
    }

    @Test
    void main_addThenConfirmedDelete_displaysExpectedTranscript() {
        String output = runCli(session(ADD_RICE, "delete-recipe 1", "y", "bye"));

        String expected = String.join(System.lineSeparator(),
                "New recipe added!",
                "Rice",
                "200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
                "Ingredients:",
                "- Rice: 150g",
                "------------------------------------------------------------",
                CONTINUE_PROMPT,
                "Are you sure you want to delete recipe 1: Rice? (y/n)",
                "Recipe deleted!",
                "Rice",
                "Recipes remaining: 0",
                "------------------------------------------------------------",
                CONTINUE_PROMPT,
                "Goodbye!") + System.lineSeparator();
        assertTrue(output.endsWith(expected), output);
    }

    @TestFactory
    Stream<DynamicTest> main_deleteConfirmedWithYes_removesTheRecipe() {
        return List.of("y", "Y", "yes", "YES", "Yes", "  yes  ").stream()
                .map(answer -> DynamicTest.dynamicTest("Answer: [" + answer + "]", () -> {
                    // After Rice is deleted only Oats remains, so index 2 must no longer exist.
                    String output = runCli(session(ADD_RICE, ADD_OATS, "delete-recipe 1", answer,
                            "delete-recipe 2", "bye"));

                    assertEquals(1, countOccurrences(output, "Recipe deleted!"), output);
                    assertTrue(output.contains("Recipes remaining: 1"), output);
                    assertTrue(output.contains("Error: Recipe index must be between 1 and 1."), output);
                    assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
                }));
    }

    @TestFactory
    Stream<DynamicTest> main_deleteNotConfirmed_keepsTheRecipe() {
        // "bye" at the prompt only cancels the delete; it does not exit the program.
        return List.of("n", "N", "no", "", "   ", "maybe", "yes please", "yy", "bye").stream()
                .map(answer -> DynamicTest.dynamicTest("Answer: [" + answer + "]", () -> {
                    // The second delete only works if the first one left the recipe in place.
                    String output = runCli(session(ADD_RICE, "delete-recipe 1", answer,
                            "delete-recipe 1", "y", "bye"));

                    assertEquals(2, countOccurrences(output,
                            "Are you sure you want to delete recipe 1: Rice? (y/n)"), output);
                    assertEquals(1, countOccurrences(output, "Cancelled. Nothing was changed."), output);
                    assertEquals(1, countOccurrences(output, "Recipe deleted!"), output);
                    assertTrue(output.indexOf("Cancelled.") < output.indexOf("Recipe deleted!"), output);
                    assertEquals(1, countOccurrences(output, "Goodbye!"), output);
                    assertEquals(0, countOccurrences(output, "Error: "), output);
                }));
    }

    @Test
    void main_deleteThenEndOfInput_cancelsInsteadOfDeleting() {
        String output = runCli(session(ADD_RICE, "delete-recipe 1"));

        assertTrue(output.contains("Are you sure you want to delete recipe 1: Rice? (y/n)"), output);
        assertTrue(output.contains("Cancelled. Nothing was changed."), output);
        assertEquals(0, countOccurrences(output, "Recipe deleted!"), output);
    }

    @Test
    void main_deleteMissingRecipe_showsErrorWithoutAsking() {
        String output = runCli(session("delete-recipe 1", ADD_RICE, "delete-recipe 2", "bye"));

        assertEquals(0, countOccurrences(output, "Are you sure"), output);
        assertEquals(2, countOccurrences(output, "Error: "), output);
        assertEquals(4, countOccurrences(output, CONTINUE_PROMPT), output);
        assertTrue(output.contains("Error: There are no recipes to delete."), output);
        assertTrue(output.contains("Error: Recipe index must be between 1 and 1."), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @TestFactory
    Stream<DynamicTest> main_invalidDeleteArguments_showsOnlyDeleteUsageAndRecovers() {
        return List.of("delete-recipe", "delete-recipe 0", "delete-recipe -1", "delete-recipe abc",
                        "delete-recipe 1 2", "delete-recipe 2147483648").stream()
                .map(badCommand -> DynamicTest.dynamicTest(badCommand, () -> {
                    String output = runCli(session(ADD_RICE, badCommand, "delete-recipe 1", "y", "bye"));

                    assertEquals(1, countOccurrences(output, "Error: "), output);
                    assertEquals(1, countOccurrences(output, "Format: delete-recipe INDEX"), output);
                    assertEquals(0, countOccurrences(output, "Format: add-recipe"), output);
                    // Only the later, valid command reaches the confirmation question.
                    assertEquals(1, countOccurrences(output, "Are you sure"), output);
                    assertEquals(1, countOccurrences(output, "Recipe deleted!"), output);
                    assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
                }));
    }

    @Test
    void main_unknownCommand_listsEveryCommandUsageOnce() {
        String output = runCli(session("unknown", "bye"));

        assertTrue(output.contains("Error: Unknown command: unknown"), output);
        assertEquals(1, countOccurrences(output, Parser.ADD_RECIPE_USAGE), output);
        assertEquals(1, countOccurrences(output, Parser.DELETE_RECIPE_USAGE), output);
    }

    @Test
    void main_listRecipes_showsListThenFullRecipeWithoutAsking() {
        String output = runCli(session(ADD_RICE, ADD_OATS, "list-recipes", "list-recipes 2", "bye"));

        String expected = String.join(System.lineSeparator(),
                "Recipes:",
                "1. Rice (200 kcal)",
                "2. Oats (150 kcal)",
                "------------------------------------------------------------",
                CONTINUE_PROMPT,
                "Oats",
                "150 kcal | Protein: 5g | Carbs: 27g | Fats: 3g",
                "Ingredients:",
                "- Oats: 40g",
                "------------------------------------------------------------",
                CONTINUE_PROMPT,
                "Goodbye!") + System.lineSeparator();
        assertTrue(output.endsWith(expected), output);
        assertEquals(0, countOccurrences(output, "Are you sure"), output);
        assertEquals(0, countOccurrences(output, "Error: "), output);
    }

    @Test
    void main_listRecipeWithNoRecipes_continuesWithoutError() {
        String output = runCli(session("list-recipes", ADD_RICE, "list-recipes", "bye"));

        assertEquals(1, countOccurrences(output, "No recipes yet. Use 'add-recipe' to add one."), output);
        assertTrue(output.contains("1. Rice (200 kcal)"), output);
        assertEquals(0, countOccurrences(output, "Error: "), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_listRecipeBadIndex_showsErrorAndRecovers() {
        String output = runCli(session("list-recipes 1", ADD_RICE, "list-recipes 2", "list-recipes abc",
                "list-recipes 1", "bye"));

        assertEquals(3, countOccurrences(output, "Error: "), output);
        assertTrue(output.contains("Error: There are no recipes to show. Use 'add-recipe' to add one."), output);
        assertTrue(output.contains("Error: Recipe index must be between 1 and 1."), output);
        assertTrue(output.contains("Error: Recipe index must be a single positive whole number."), output);
        assertEquals(6, countOccurrences(output, CONTINUE_PROMPT), output);
        // The last command still works after the errors.
        assertTrue(output.contains("- Rice: 150g" + System.lineSeparator()
                + "------------------------------------------------------------" + System.lineSeparator()
                + CONTINUE_PROMPT + System.lineSeparator() + "Goodbye!"), output);
    }

    @Test
    void main_listRecipeThenDelete_showsTheNumbersDeleteUses() {
        String output = runCli(session(ADD_RICE, ADD_OATS, "list-recipes", "delete-recipe 2", "y",
                "list-recipes", "bye"));

        assertTrue(output.contains("Are you sure you want to delete recipe 2: Oats? (y/n)"), output);
        assertTrue(output.endsWith("Recipes:" + System.lineSeparator() + "1. Rice (200 kcal)"
                + System.lineSeparator() + "------------------------------------------------------------"
                + System.lineSeparator() + CONTINUE_PROMPT
                + System.lineSeparator() + "Goodbye!" + System.lineSeparator()), output);
    }

    /**
     * Builds console input: the answer to the name prompt, then each line, ending with a newline.
     */
    private static String session(String... lines) {
        return "Tester" + System.lineSeparator()
                + String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}

