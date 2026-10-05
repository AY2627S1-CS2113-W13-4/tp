package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Checks CLI output and recovery after an invalid command.
 */
class DukeTest {
    @Test
    void main_invalidThenValid_displaysExpectedOutput() {
        String input = String.join(System.lineSeparator(),
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

            Duke.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        String expected = String.join(System.lineSeparator(),
                "Welcome to MishMash!",
                "Add a recipe with 'add-recipe', or type 'bye' to exit.",
                "Error: Expected prefixed recipe arguments.",
                Parser.ADD_RECIPE_USAGE,
                "New recipe added!",
                "Rice",
                "200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
                "Ingredients:",
                "- Rice: 150g",
                "Goodbye!") + System.lineSeparator();

        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }
}
