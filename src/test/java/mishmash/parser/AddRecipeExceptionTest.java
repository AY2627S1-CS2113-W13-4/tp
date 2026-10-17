package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;

/**
 * Checks that recipe validation retains diagnostic causes and user-facing messages.
 */
class AddRecipeExceptionTest {
    @Test
    void parseCommand_overflowNutrition_preservesCauseChainAndUsage() {
        String input = "add-recipe n/Rice cal/2147483648 p/4 c/45 f/1 i/Rice:150g";

        MishMashException exception = assertThrows(MishMashException.class,
                () -> new Parser().parseCommand(input));

        IllegalArgumentException validation = assertInstanceOf(
                IllegalArgumentException.class, exception.getCause());
        assertInstanceOf(NumberFormatException.class, validation.getCause());
        assertEquals("Calories must not exceed 2147483647."
                + System.lineSeparator() + Parser.ADD_RECIPE_USAGE, exception.getMessage());
    }

    @Test
    void parseCommand_blankName_preservesValidationCauseAndUsage() {
        String input = "add-recipe n/ cal/200 p/4 c/45 f/1 i/Rice:150g";

        MishMashException exception = assertThrows(MishMashException.class,
                () -> new Parser().parseCommand(input));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Value must not be blank: n/"
                + System.lineSeparator() + Parser.ADD_RECIPE_USAGE, exception.getMessage());
    }
}
