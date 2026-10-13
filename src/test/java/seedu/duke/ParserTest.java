package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import seedu.duke.MealPlanViewer.MealSummary;

/**
 * Checks recipe and viewing-command parsing, including rejection of invalid arguments.
 */
class ParserTest {
    private static final String VALID_COMMAND =
            "add-recipe n/Chicken and Rice cal/650 p/50 c/70 f/15"
            + " i/Chicken Breast:200g i/White Rice:150g";

    private final Parser parser = new Parser();

    @Test
    void parseCommand_validInput_preservesRecipeFields() throws MishMashException {
        RecipeBook recipeBook = new RecipeBook();

        parser.parseCommand(VALID_COMMAND).execute(recipeBook);

        assertEquals(1, recipeBook.getRecipes().size());
        Recipe recipe = recipeBook.getRecipes().get(0);
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
        RecipeBook recipeBook = new RecipeBook();

        parser.parseCommand("add-recipe i/Olive Oil:2.5ml f/2 c/0 p/0 cal/20 n/Oil")
                .execute(recipeBook);

        assertEquals("Oil", recipeBook.getRecipes().get(0).getName());
        assertEquals(new BigDecimal("2.5"),
                recipeBook.getRecipes().get(0).getIngredients().get(0).getQuantity());
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
        RecipeBook recipeBook = new RecipeBook();
        parser.parseCommand(VALID_COMMAND).execute(recipeBook);

        assertThrows(MishMashException.class,
                () -> parser.parseCommand(VALID_COMMAND + " i/Broken").execute(recipeBook));

        assertEquals(1, recipeBook.getRecipes().size());

        parser.parseCommand(VALID_COMMAND.replace("Chicken and Rice", "Second recipe"))
                .execute(recipeBook);

        assertEquals(2, recipeBook.getRecipes().size());
        assertEquals("Chicken and Rice", recipeBook.getRecipes().get(0).getName());
        assertEquals("Second recipe", recipeBook.getRecipes().get(1).getName());
    }

    @Test
    void parseCommand_emptyOrUnknown_throwsMishMashException() {
        assertThrows(MishMashException.class, () -> parser.parseCommand(""));
        assertThrows(MishMashException.class, () -> parser.parseCommand(null));
        assertThrows(MishMashException.class, () -> parser.parseCommand("unknown"));
    }

    @Test
    void parseCommand_viewingCommands_createsCorrectCommandTypes() throws MishMashException {
        assertInstanceOf(ViewPlanCommand.class, parser.parseCommand("view-plan"));
        assertInstanceOf(ViewPlanCommand.class, parser.parseCommand("view-plan d/2"));
        assertInstanceOf(ViewListCommand.class, parser.parseCommand("view-list"));
    }

    @Test
    void parseCommand_viewPlanWhitespace_preservesSelectedDay() throws MishMashException {
        Parser connectedParser = new Parser(() -> List.of(
                List.of(new MealSummary("Rice", 200, 4, 45, 1)),
                List.of(new MealSummary("Beans", 300, 20, 40, 5))), () -> Optional.empty());

        String display = connectedParser.parseCommand(" \tview-plan\t d/ 002 \t").execute(new RecipeBook());

        assertTrue(display.startsWith("================ Day 2 ================"));
        assertTrue(display.contains("Meal 1: Beans"));
        assertFalse(display.contains("Rice"));
    }

    @Test
    void parseCommand_invalidViewPlanArguments_includesUsage() {
        String[] invalidArguments = {"2", "d/", "d/0", "d/-1", "d/+1", "d/1.5", "d/abc",
            "d/2147483648", "d/1 d/2", "d/1 extra", "x/1", "D/1", "d/1 cal/200"};
        for (String arguments : invalidArguments) {
            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand("view-plan " + arguments), arguments);
            assertTrue(exception.getMessage().contains(Parser.VIEW_PLAN_USAGE), arguments);
        }
    }

    @Test
    void parseCommand_viewListArguments_includesUsage() {
        for (String arguments : List.of("extra", "d/1", "i/Rice:200g")) {
            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand("view-list " + arguments));
            assertTrue(exception.getMessage().contains(Parser.VIEW_LIST_USAGE));
        }
    }

    @Test
    void parseCommand_viewingCommands_readsDataOnlyWhenExecuted() throws MishMashException {
        AtomicInteger sourceReadCount = new AtomicInteger();
        Parser connectedParser = new Parser(() -> {
            sourceReadCount.incrementAndGet();
            return List.of();
        }, () -> {
            sourceReadCount.incrementAndGet();
            return Optional.of(List.of());
        });

        Command allDaysCommand = connectedParser.parseCommand("view-plan");
        Command selectedDayCommand = connectedParser.parseCommand("view-plan d/1");
        Command groceryCommand = connectedParser.parseCommand(" \tview-list \t");
        assertThrows(MishMashException.class, () -> connectedParser.parseCommand("view-plan d/0"));
        assertThrows(MishMashException.class, () -> connectedParser.parseCommand("view-list extra"));
        assertEquals(0, sourceReadCount.get());

        allDaysCommand.execute(new RecipeBook());
        selectedDayCommand.execute(new RecipeBook());
        assertTrue(groceryCommand.execute(new RecipeBook()).endsWith("Total items to purchase: 0"));
        assertEquals(3, sourceReadCount.get());
    }

    @Test
    void parseCommand_largeDay_checksActivePlanDuringExecution() throws MishMashException {
        Parser connectedParser = new Parser(() -> List.of(List.of(new MealSummary("Rice", 200, 4, 45, 1))),
                () -> Optional.empty());
        Command command = connectedParser.parseCommand("view-plan d/2147483647");

        MishMashException exception = assertThrows(MishMashException.class,
                () -> command.execute(new RecipeBook()));

        assertTrue(exception.getMessage().contains("Day number must be between 1 and 1."));
    }

    @Test
    void constructor_nullDataSource_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Parser(null, () -> Optional.empty()));
        assertThrows(NullPointerException.class, () -> new Parser(() -> List.of(), null));
    }

    @Test
    void parseCommand_validDeleteIndex_deletesThatRecipeWhenExecuted() throws MishMashException {
        RecipeBook book = createBookWithTwoRecipes();

        String result = parser.parseCommand("delete-recipe 2").execute(book);

        assertEquals("Recipe deleted!" + System.lineSeparator() + "Second recipe"
                + System.lineSeparator() + "Recipes remaining: 1", result);
        assertEquals(1, book.getRecipes().size());
        assertEquals("Chicken and Rice", book.getRecipes().get(0).getName());
    }

    @Test
    void parseCommand_deleteIndex_asksForConfirmationNamingTheRecipe() throws MishMashException {
        RecipeBook book = createBookWithTwoRecipes();

        Command command = parser.parseCommand("delete-recipe 1");

        assertEquals("Are you sure you want to delete recipe 1: Chicken and Rice? (y/n)",
                command.confirmationPrompt(book).orElseThrow());
        assertEquals(2, book.getRecipes().size());
    }

    @Test
    void parseCommand_deleteWithExtraWhitespaceOrLeadingZeros_acceptsIndex() throws MishMashException {
        String[] inputs = {"  delete-recipe \t 1  ", "delete-recipe 0001", "delete-recipe    1"};

        for (String input : inputs) {
            RecipeBook book = createBookWithTwoRecipes();

            parser.parseCommand(input).execute(book);

            assertEquals(1, book.getRecipes().size(), input);
            assertEquals("Second recipe", book.getRecipes().get(0).getName(), input);
        }
    }

    @Test
    void parseCommand_invalidDeleteArguments_throwsWithDeleteUsageOnly() {
        String[][] invalidCases = {
                {"delete-recipe", "Recipe index is required."},
                {"delete-recipe 0", "Recipe index must be at least 1."},
                {"delete-recipe 000", "Recipe index must be at least 1."},
                {"delete-recipe -1", "Recipe index must be a single positive whole number."},
                {"delete-recipe +1", "Recipe index must be a single positive whole number."},
                {"delete-recipe 1.5", "Recipe index must be a single positive whole number."},
                {"delete-recipe abc", "Recipe index must be a single positive whole number."},
                {"delete-recipe 1 2", "Recipe index must be a single positive whole number."},
                {"delete-recipe 1,2", "Recipe index must be a single positive whole number."},
                {"delete-recipe n/Rice", "Recipe index must be a single positive whole number."},
                {"delete-recipe 2147483648", "Recipe index must not exceed 2147483647."}
        };

        for (String[] invalidCase : invalidCases) {
            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand(invalidCase[0]), invalidCase[0]);

            String message = exception.getMessage();
            assertTrue(message.contains(invalidCase[1]), message);
            assertTrue(message.contains(Parser.DELETE_RECIPE_USAGE), message);
            assertFalse(message.contains("Format: add-recipe"), message);
        }
    }

    @Test
    void parseCommand_largestIntIndex_parsesAndFailsOnlyWhenExecuted() throws MishMashException {
        RecipeBook book = createBookWithTwoRecipes();

        Command command = parser.parseCommand("delete-recipe 2147483647");

        MishMashException exception = assertThrows(MishMashException.class,
                () -> command.execute(book));
        assertEquals("Recipe index must be between 1 and 2.", exception.getMessage());
        assertEquals(2, book.getRecipes().size());
    }

    @Test
    void parseCommand_deleteLookalikes_areUnknownCommands() {
        String[] inputs = {"delete-recipes", "Delete-Recipe 1", "delete 1"};

        for (String input : inputs) {
            MishMashException exception = assertThrows(MishMashException.class,
                    () -> parser.parseCommand(input), input);

            assertTrue(exception.getMessage().startsWith("Unknown command: "), input);
        }
    }

    @Test
    void commandUsage_listsDeleteRecipeUsage() {
        assertTrue(Parser.COMMAND_USAGE.contains(Parser.ADD_RECIPE_USAGE));
        assertTrue(Parser.COMMAND_USAGE.contains(Parser.DELETE_RECIPE_USAGE));
    }

    @Test
    void parseCommand_invalidAddRecipe_doesNotShowDeleteUsage() {
        MishMashException exception = assertThrows(MishMashException.class,
                () -> parser.parseCommand("add-recipe"));

        assertTrue(exception.getMessage().contains(Parser.ADD_RECIPE_USAGE));
        assertFalse(exception.getMessage().contains(Parser.DELETE_RECIPE_USAGE));
    }

    @Test
    void parseCommand_addRecipe_needsNoConfirmation() throws MishMashException {
        RecipeBook book = new RecipeBook();

        assertFalse(parser.parseCommand(VALID_COMMAND).confirmationPrompt(book).isPresent());
    }

    /**
     * Creates a book with "Chicken and Rice" at index 1 and "Second recipe" at index 2.
     */
    private RecipeBook createBookWithTwoRecipes() throws MishMashException {
        RecipeBook book = new RecipeBook();
        parser.parseCommand(VALID_COMMAND).execute(book);
        parser.parseCommand(VALID_COMMAND.replace("Chicken and Rice", "Second recipe")).execute(book);
        return book;
    }
}
