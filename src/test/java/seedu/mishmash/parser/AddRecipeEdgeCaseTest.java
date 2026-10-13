package seedu.mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import seedu.mishmash.exception.MishMashException;
import seedu.mishmash.model.Recipe;
import seedu.mishmash.model.RecipeBook;
import seedu.mishmash.testutil.AddRecipeTestData;

/**
 * Exercises malformed recipe inputs and checks that failures do not change the book.
 * The shared TSV cases are also used by the CLI tests and can be tried manually.
 */
class AddRecipeEdgeCaseTest {
    @TestFactory
    Stream<DynamicTest> parseCommand_invalidInput_preservesBook() throws IOException {
        return AddRecipeTestData.invalidCases().stream().map(testCase -> DynamicTest.dynamicTest(testCase[0], () -> {
            Parser parser = new Parser();
            RecipeBook book = new RecipeBook();
            parser.parseCommand(AddRecipeTestData.VALID_COMMAND).execute(book);
            Recipe original = book.getRecipes().get(0);

            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand(testCase[2]).execute(book));

            assertTrue(exception.getMessage().contains(testCase[1]), exception.getMessage());
            assertTrue(exception.getMessage().contains(Parser.ADD_RECIPE_USAGE), exception.getMessage());
            assertEquals(List.of(original), book.getRecipes());

            parser.parseCommand(AddRecipeTestData.VALID_COMMAND.replace("n/Rice", "n/Second recipe")).execute(book);
            assertEquals(2, book.getRecipes().size());
            assertEquals(original, book.getRecipes().get(0));
            assertEquals("Second recipe", book.getRecipes().get(1).getName());
        }));
    }
}
