package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Exercises malformed recipe inputs and checks that failures do not change the book.
 * The shared TSV cases are also used by the CLI tests and can be tried manually.
 */
class AddRecipeEdgeCaseTest {
    static final String VALID_COMMAND = "add-recipe n/Rice cal/200 p/4 c/45 f/1 i/Rice:150g";

    /**
     * Loads each named invalid command and the expected diagnostic fragment.
     *
     * @return The tab-separated test cases.
     * @throws IOException If the resource cannot be read.
     */
    static List<String[]> invalidCases() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(AddRecipeEdgeCaseTest.class
                        .getResourceAsStream("/add-recipe-invalid.tsv")), StandardCharsets.UTF_8))) {
            return reader.lines().filter(line -> !line.startsWith("#"))
                    .map(line -> line.split("\t", 3)).toList();
        }
    }

    @TestFactory
    Stream<DynamicTest> parseCommand_invalidInput_preservesBook() throws IOException {
        return invalidCases().stream().map(testCase -> DynamicTest.dynamicTest(testCase[0], () -> {
            Parser parser = new Parser();
            RecipeBook book = new RecipeBook();
            parser.parseCommand(VALID_COMMAND).execute(book);
            Recipe original = book.getRecipes().get(0);

            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand(testCase[2]).execute(book));

            assertTrue(exception.getMessage().contains(testCase[1]), exception.getMessage());
            assertTrue(exception.getMessage().contains(Parser.ADD_RECIPE_USAGE), exception.getMessage());
            assertEquals(List.of(original), book.getRecipes());

            parser.parseCommand(VALID_COMMAND.replace("n/Rice", "n/Second recipe")).execute(book);
            assertEquals(2, book.getRecipes().size());
            assertEquals(original, book.getRecipes().get(0));
            assertEquals("Second recipe", book.getRecipes().get(1).getName());
        }));
    }
}
