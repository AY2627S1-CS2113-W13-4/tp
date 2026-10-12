package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Checks CLI output and recovery after an invalid command.
 */

class MishMashTest {

    private static final String ADD_RICE = AddRecipeEdgeCaseTest.VALID_COMMAND;
    private static final String ADD_OATS = "add-recipe n/Oats cal/150 p/5 c/27 f/3 i/Oats:40g";
    private static final String CONTINUE_PROMPT = "Please enter another command, or type 'bye' to exit.";

    @TestFactory
    Stream<DynamicTest> main_invalidRecipe_recoversForNextCommand() throws IOException {
        return AddRecipeEdgeCaseTest.invalidCases().stream().map(testCase ->
                DynamicTest.dynamicTest(testCase[0], () -> {
                    String output = runCli("Tester" + System.lineSeparator()
                            + testCase[2] + System.lineSeparator()
                            + AddRecipeEdgeCaseTest.VALID_COMMAND + System.lineSeparator()
                            + "bye" + System.lineSeparator());
                    assertTrue(output.contains(testCase[1]), output);
                    assertTrue(output.contains(Parser.ADD_RECIPE_USAGE), output);
                    assertTrue(output.contains("Please enter another command, or type 'bye' to exit."), output);
                    assertEquals(1, output.split("Error: ", -1).length - 1, output);
                    assertEquals(1, output.split("New recipe added!", -1).length - 1, output);
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
                            + AddRecipeEdgeCaseTest.VALID_COMMAND + System.lineSeparator()
                            + "bye" + System.lineSeparator());
                    String diagnostic = input.isBlank() ? "Enter a command." : "Unknown command: " + input;
                    assertTrue(output.contains("Error: " + diagnostic), output);
                    assertEquals(1, output.split("Format: add-recipe", -1).length - 1, output);
                    assertTrue(output.contains("Please enter another command, or type 'bye' to exit."), output);
                    assertEquals(1, output.split("New recipe added!", -1).length - 1, output);
                    assertTrue(output.indexOf("Error: ") < output.indexOf("New recipe added!"), output);
                    assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
                }));
    }

    @Test
    void main_invalidRecipeAtEndOfInput_exitsWithoutCrashing() {
        String output = runCli("Tester" + System.lineSeparator() + "add-recipe");
        assertTrue(output.contains("Error: Expected prefixed recipe arguments."), output);
        assertTrue(output.contains(Parser.ADD_RECIPE_USAGE), output);
        assertEquals(0, output.split("New recipe added!", -1).length - 1, output);
    }

    /**
     * Runs a CLI session and restores the global streams even if execution fails.
     *
     * @param input The simulated console input.
     * @return The captured output.
     */
    private String runCli(String input) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);
            MishMash.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void main_invalidThenValid_displaysExpectedOutput() {
        String input = String.join(System.lineSeparator(),
                "Duke",
                "add-recipe",
                "add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g",
                "bye") + System.lineSeparator();

        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOutput =
                 new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capturedOutput);

            MishMash.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        String banner = """
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

        String expected = banner + System.lineSeparator()
                + String.join(System.lineSeparator(),
                "What is your name?",
                "Hello Duke",
                "Welcome to MishMash!",
                "Add a recipe with 'add-recipe', or type 'bye' to exit.",
                "Error: Expected prefixed recipe arguments.",
                Parser.ADD_RECIPE_USAGE,
                "Please enter another command, or type 'bye' to exit.",
                "New recipe added!",
                "Rice",
                "200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
                "Ingredients:",
                "- Rice: 150g",
                "Goodbye!") + System.lineSeparator();

        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
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
                "Are you sure you want to delete recipe 1: Rice? (y/n)",
                "Recipe deleted!",
                "Rice",
                "Recipes remaining: 0",
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

                    assertEquals(1, count(output, "Recipe deleted!"), output);
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

                    assertEquals(2, count(output, "Are you sure you want to delete recipe 1: Rice? (y/n)"), output);
                    assertEquals(1, count(output, "Cancelled. Nothing was changed."), output);
                    assertEquals(1, count(output, "Recipe deleted!"), output);
                    assertTrue(output.indexOf("Cancelled.") < output.indexOf("Recipe deleted!"), output);
                    assertEquals(1, count(output, "Goodbye!"), output);
                    assertEquals(0, count(output, "Error: "), output);
                }));
    }

    @Test
    void main_deleteThenEndOfInput_cancelsInsteadOfDeleting() {
        String output = runCli(session(ADD_RICE, "delete-recipe 1"));

        assertTrue(output.contains("Are you sure you want to delete recipe 1: Rice? (y/n)"), output);
        assertTrue(output.contains("Cancelled. Nothing was changed."), output);
        assertEquals(0, count(output, "Recipe deleted!"), output);
    }

    @Test
    void main_deleteMissingRecipe_showsErrorWithoutAskingOrShowingUsage() {
        String output = runCli(session("delete-recipe 1", ADD_RICE, "delete-recipe 2", "bye"));

        assertEquals(0, count(output, "Are you sure"), output);
        assertEquals(0, count(output, "Format:"), output);
        assertEquals(2, count(output, "Error: "), output);
        assertEquals(2, count(output, CONTINUE_PROMPT), output);
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

                    assertEquals(1, count(output, "Error: "), output);
                    assertEquals(1, count(output, "Format: delete-recipe INDEX"), output);
                    assertEquals(0, count(output, "Format: add-recipe"), output);
                    // Only the later, valid command reaches the confirmation question.
                    assertEquals(1, count(output, "Are you sure"), output);
                    assertEquals(1, count(output, "Recipe deleted!"), output);
                    assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
                }));
    }

    @Test
    void main_unknownCommand_listsEveryCommandUsageOnce() {
        String output = runCli(session("unknown", "bye"));

        assertTrue(output.contains("Error: Unknown command: unknown"), output);
        assertEquals(1, count(output, Parser.ADD_RECIPE_USAGE), output);
        assertEquals(1, count(output, Parser.DELETE_RECIPE_USAGE), output);
    }

    /**
     * Builds console input: the answer to the name prompt, then each line, ending with a newline.
     */
    private static String session(String... lines) {
        return "Tester" + System.lineSeparator()
                + String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    /**
     * Counts how many times a fragment appears in the output, treating it as plain text.
     */
    private static int count(String output, String fragment) {
        int count = 0;
        for (int at = output.indexOf(fragment); at >= 0; at = output.indexOf(fragment, at + fragment.length())) {
            count++;
        }
        return count;
    }
}
