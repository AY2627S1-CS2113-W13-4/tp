package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

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
                    assertTrue(output.contains("Please enter another command, or type 'bye' to exit."),
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
                            + AddRecipeEdgeCaseTest.VALID_COMMAND + System.lineSeparator()
                            + "bye" + System.lineSeparator());
                    String diagnostic = input.isBlank() ? "Enter a command." : "Unknown command: " + input;
                    assertTrue(output.contains("Error: " + diagnostic), output);
                    assertEquals(1, countOccurrences(output, "Format: add-recipe"), output);
                    assertTrue(output.contains("Please enter another command, or type 'bye' to exit."),
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
                AddRecipeEdgeCaseTest.VALID_COMMAND, "bye"));

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
                "Duke",
                "add-recipe",
                "add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g",
                "bye") + System.lineSeparator();

        String output = runCli(input);

        String expected = EXPECTED_BANNER + System.lineSeparator()
                + String.join(System.lineSeparator(),
                        "What is your name?",
                        "Hello Duke",
                        "Welcome to MishMash!",
                        "Use 'add-recipe', 'view-plan', or 'view-list', or type 'bye' to exit.",
                        "Error: Expected prefixed recipe arguments.",
                        Parser.ADD_RECIPE_USAGE,
                        "Please enter another command, or type 'bye' to exit.",
                        "New recipe added!",
                        "Rice",
                        "200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
                        "Ingredients:",
                        "- Rice: 150g",
                        "Goodbye!") + System.lineSeparator();

        assertEquals(expected, output);
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
}
