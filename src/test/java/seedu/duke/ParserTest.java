package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * Checks recipe parsing and rejection of invalid commands.
 */
class ParserTest {
    private static final String VALID_COMMAND =
            "add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15"
                + " i/Chicken Breast:200g i/White Rice:150g";

    private final Parser parser = new Parser();

    @Test
    void parseCommand_validInput_preservesRecipeFields() throws MishMashException {
        RecipeBook book = new RecipeBook();

        parser.parseCommand(VALID_COMMAND).execute(book);

        assertEquals(1, book.getRecipes().size());
        Recipe recipe = book.getRecipes().get(0);
        assertEquals("Chicken and Rice", recipe.getName());
        assertEquals(650, recipe.getCalories());
        assertEquals(50, recipe.getProtein());
        assertEquals(70, recipe.getCarbs());
        assertEquals(15, recipe.getFats());
        assertEquals(2, recipe.getIngredients().size());
        assertEquals("Chicken Breast", recipe.getIngredients().get(0).getName());
        assertEquals(new BigDecimal("200"), recipe.getIngredients().get(0).getQuantity());
        assertEquals("g", recipe.getIngredients().get(0).getUnit());
    }

    @Test
    void parseCommand_reorderedFields_acceptsDecimalIngredient() throws MishMashException {
        RecipeBook book = new RecipeBook();

        parser.parseCommand("add-recipe i/Olive Oil:2.5ml f/2 c/0 p/0 cal/20 n/Oil")
                .execute(book);

        assertEquals("Oil", book.getRecipes().get(0).getName());
        assertEquals(new BigDecimal("2.5"),
                book.getRecipes().get(0).getIngredients().get(0).getQuantity());
    }

    @Test
    void parseCommand_missingField_throwsMishMashException() {
        String[] requiredFields = {
            "n/Chicken and Rice",
            "cal/650",
            "p/50",
            "c/70",
            "f/15"
        };

        for (String field : requiredFields) {
            assertThrows(MishMashException.class,
                    () -> parser.parseCommand(VALID_COMMAND.replace(field, "")));
        }

        assertThrows(MishMashException.class,
                () -> parser.parseCommand("add-recipe n/Rice cal/200 p/4 c/45 f/1"));
    }

    @Test
    void parseCommand_invalidValues_throwsMishMashException() {
        String[] invalidCommands = {
            VALID_COMMAND.replace("cal/650", "cal/abc"),
            VALID_COMMAND.replace("p/50", "p/-1"),
            VALID_COMMAND.replace("c/70", "c/1.5"),
            VALID_COMMAND.replace("f/15", "f/2147483648"),
            VALID_COMMAND.replace("n/Chicken and Rice", "n/ "),
            VALID_COMMAND + " cal/100",
            VALID_COMMAND + " x/unknown",
            VALID_COMMAND + " i/Rice",
            VALID_COMMAND + " i/Rice:10",
            VALID_COMMAND + " i/Rice:0g",
            VALID_COMMAND + " i/Rice:-10g"
        };

        for (String input : invalidCommands) {
            assertThrows(MishMashException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    void parseCommand_invalidRecipe_includesUsage() {
        MishMashException exception = assertThrows(MishMashException.class,
                () -> parser.parseCommand("add-recipe"));

        assertTrue(exception.getMessage().contains(Parser.ADD_RECIPE_USAGE));
    }

    @Test
    void parseCommand_invalidBetweenValid_preservesRecipeBook() throws MishMashException {
        RecipeBook book = new RecipeBook();
        parser.parseCommand(VALID_COMMAND).execute(book);

        assertThrows(MishMashException.class,
                () -> parser.parseCommand(VALID_COMMAND + " i/Broken").execute(book));

        assertEquals(1, book.getRecipes().size());

        parser.parseCommand(VALID_COMMAND.replace("Chicken and Rice", "Second recipe"))
                .execute(book);

        assertEquals(2, book.getRecipes().size());
        assertEquals("Chicken and Rice", book.getRecipes().get(0).getName());
        assertEquals("Second recipe", book.getRecipes().get(1).getName());
    }

    @Test
    void parseCommand_emptyOrUnknown_throwsMishMashException() {
        assertThrows(MishMashException.class, () -> parser.parseCommand(""));
        assertThrows(MishMashException.class, () -> parser.parseCommand(null));
        assertThrows(MishMashException.class, () -> parser.parseCommand("unknown"));
    }
}

