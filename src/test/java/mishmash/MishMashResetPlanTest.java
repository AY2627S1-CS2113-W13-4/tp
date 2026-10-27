package mishmash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.command.ResetPlanCommand;

//@@author Rixte
/**
 * Checks the reset command's complete console workflow, preservation of session data, and error recovery.
 */
class MishMashResetPlanTest {
    @Test
    void main_resetGeneratedPlan_preservesOtherSessionDataAndAllowsRegeneration() {
        String output = runCli(
                "add-recipe n/A cal/500 p/20 c/50 f/10 i/Rice:100g food/none",
                "add-recipe n/B cal/500 p/20 c/50 f/10 i/Rice:100g food/none",
                "add-recipe n/C cal/500 p/20 c/50 f/10 i/Rice:100g food/none",
                "set-diet diet/vegetarian", "record-meal 1", "generate-plan d/2 cal/1500", "generate-list",
                "view-list", "reset-plan", "view-plan", "view-list", "view-meals", "view-diet", "list-recipes",
                "generate-plan d/1", "generate-list", "view-list", "bye");
        int resetPosition = output.indexOf("Meal plan reset.");
        int regenerationPosition = output.indexOf("Meal plan successfully generated for 1 day(s)!", resetPosition);

        assertTrue(resetPosition >= 0 && regenerationPosition > resetPosition, output);
        String afterReset = output.substring(resetPosition, regenerationPosition);
        assertTrue(afterReset.contains("No active meal plan. Use 'generate-plan' first."), afterReset);
        assertTrue(afterReset.contains("No grocery list generated. Use 'generate-list' first."), afterReset);
        assertTrue(afterReset.contains("Meal 1: A"), afterReset);
        assertTrue(afterReset.contains("1. A (500 kcal)"), afterReset);
        assertTrue(afterReset.contains("vegetarian"), afterReset);
        assertFalse(afterReset.contains("[ ] Rice:"), afterReset);
        assertTrue(output.substring(regenerationPosition).contains("[ ] Rice: 300g"), output);
        assertEquals(0, countOccurrences(output, "Error: "));
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_invalidReset_recoversWithoutClearingDataOrConsumingNextCommand() {
        String output = runCli("reset-plan extra", "reset-plan", "reset-plan", "help", "bye");

        assertEquals(1, countOccurrences(output, "Error: reset-plan does not accept arguments."));
        assertEquals(2, countOccurrences(output, ResetPlanCommand.USAGE));
        assertEquals(2, countOccurrences(output, "No active meal plan to reset."));
        assertEquals(1, countOccurrences(output, "Available commands (names are case-sensitive):"));
        assertFalse(output.contains("Are you sure"), output);
        assertTrue(output.indexOf("Error: ") < output.indexOf("No active meal plan to reset."), output);
        assertTrue(output.endsWith("Goodbye!" + System.lineSeparator()), output);
    }

    @Test
    void main_invalidResetWithActivePlan_keepsMealsAndGroceriesUntilValidReset() {
        String output = runCli(
                "add-recipe n/A cal/500 p/20 c/50 f/10 i/Rice:100g food/none",
                "add-recipe n/B cal/500 p/20 c/50 f/10 i/Rice:100g food/none",
                "add-recipe n/C cal/500 p/20 c/50 f/10 i/Rice:100g food/none",
                "generate-plan d/1", "generate-list", "reset-plan d/1", "view-plan", "view-list",
                "reset-plan", "view-plan", "view-list", "bye");
        int failure = output.indexOf("Error: reset-plan does not accept arguments.");
        int reset = output.indexOf("Meal plan reset.", failure);

        assertTrue(failure >= 0 && reset > failure, output);
        String preserved = output.substring(failure, reset);
        assertTrue(preserved.contains("Daily Totals: 1500 kcal"), preserved);
        assertTrue(preserved.contains("[ ] Rice: 300g"), preserved);
        String cleared = output.substring(reset);
        assertTrue(cleared.contains("No active meal plan."), cleared);
        assertTrue(cleared.contains("No grocery list generated."), cleared);
        assertFalse(cleared.contains("[ ] Rice:"), cleared);
        assertEquals(1, countOccurrences(output, "Error: "));
    }

    @TestFactory
    Stream<DynamicTest> main_endOfInputAfterResetCommand_finishesWithoutRequestingAnotherAnswer() {
        return Stream.of("reset-plan", "reset-plan extra").map(input -> DynamicTest.dynamicTest(input, () -> {
            String output = runCli(input);

            assertTrue(output.contains(input.endsWith("extra") ? "Error: reset-plan does not accept arguments."
                    : "No active meal plan to reset."), output);
            assertFalse(output.contains("Are you sure"), output);
            assertFalse(output.contains("Goodbye!"), output);
            assertEquals(2, countOccurrences(output,
                    "Enter a command, or type 'help' for all commands or 'bye' to exit."));
        }));
    }

    /**
     * Runs the real entry point with test input and restores global streams even if execution fails.
     */
    private String runCli(String... commands) {
        String input = "Tester" + System.lineSeparator() + String.join(System.lineSeparator(), commands)
                + System.lineSeparator();
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(output);
            MishMash.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    /**
     * Counts literal occurrences, including syntax text containing regular-expression characters.
     */
    private int countOccurrences(String output, String fragment) {
        return output.split(Pattern.quote(fragment), -1).length - 1;
    }
}
//@@author
