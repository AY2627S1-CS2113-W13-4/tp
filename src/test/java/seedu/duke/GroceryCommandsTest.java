package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

/**
 * Checks grocery command integration, snapshot safety, invalidation, and diagnostic logging.
 */
class GroceryCommandsTest {
    private final Model model = new Model();
    private final Parser parser = new Parser(model);
    private final RecipeBook book = new RecipeBook();

    @Test
    void execute_repeatedMeals_combinesIngredientsAndKeepsUnitsSeparate() throws MishMashException {
        Recipe rice = createRecipe("Rice meal", "Rice", "0.1", "g");
        Recipe other = new Recipe("Other meal", 0, 0, 0, 0, List.of(
                new GroceryItem("Rice", new BigDecimal("0.2"), "g"),
                new GroceryItem("Rice", BigDecimal.ONE, "kg"),
                new GroceryItem("rice", BigDecimal.ONE, "g")));
        model.setMealPlan(List.of(rice, rice, other));
        book.add(rice);

        assertEquals("Grocery list generated. Use 'view-list' to view it.",
                parser.parseCommand("generate-list").execute(book));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Rice: 0.4g",
                "[ ] Rice: 1kg",
                "[ ] rice: 1g",
                "=============================================",
                "Total items to purchase: 3");
        assertEquals(expected, parser.parseCommand("view-list").execute(book));
        assertEquals(expected, parser.parseCommand("view-list").execute(book));
        assertEquals(List.of(rice, rice, other), model.getMealPlan());
        assertEquals(List.of(rice), book.getRecipes());
        assertEquals(new BigDecimal("0.1"), rice.getIngredients().get(0).getQuantity());
    }

    @Test
    void execute_missingState_reportsRequiredCommand() {
        MishMashException generateError = assertThrows(MishMashException.class,
                () -> parser.parseCommand("generate-list").execute(book));
        assertEquals("No active meal plan. Use 'generate-plan' first.", generateError.getMessage());
        MishMashException viewError = assertThrows(MishMashException.class,
                () -> parser.parseCommand("view-list").execute(book));
        assertEquals("No grocery list generated. Use 'generate-list' first.", viewError.getMessage());
    }

    @Test
    void parseCommand_arguments_rejectsWithoutChangingStoredList() throws MishMashException {
        model.setMealPlan(List.of(createRecipe("Meal", "Rice", "1", "g")));
        parser.parseCommand("generate-list").execute(book);
        List<GroceryItem> original = model.getGroceryList().orElseThrow();

        for (String input : List.of("generate-list r/1", "view-list extra")) {
            MishMashException error = assertThrows(MishMashException.class, () -> parser.parseCommand(input));
            assertTrue(error.getMessage().contains("takes no arguments"));
            assertTrue(error.getMessage().contains("Format: " + input.split(" ")[0]));
        }
        assertEquals(original, model.getGroceryList().orElseThrow());
        assertTrue(parser.parseCommand("  view-list \t ").execute(book).contains("Rice: 1g"));
    }

    @Test
    void setMealPlan_replacement_clearsOldGroceriesAndAllowsRegeneration() throws MishMashException {
        Recipe rice = createRecipe("Rice meal", "Rice", "1", "g");
        model.setMealPlan(List.of(rice));
        parser.parseCommand("generate-list").execute(book);
        parser.parseCommand("generate-list").execute(book);
        assertTrue(parser.parseCommand("view-list").execute(book).contains("Rice: 1g"));

        model.setMealPlan(List.of(createRecipe("Bean meal", "Beans", "2", "g")));
        assertTrue(model.getGroceryList().isEmpty());
        assertThrows(MishMashException.class, () -> parser.parseCommand("view-list").execute(book));
        parser.parseCommand("generate-list").execute(book);
        String display = parser.parseCommand("view-list").execute(book);
        assertTrue(display.contains("Beans: 2g"));
        assertFalse(display.contains("Rice"));

        model.setMealPlan(List.of());
        assertTrue(model.getGroceryList().isEmpty());
    }

    @Test
    void setSnapshots_mutableOrInvalidInput_preservesIndependentState() throws MishMashException {
        Recipe rice = createRecipe("Meal", "Rice", "1", "g");
        List<Recipe> recipes = new ArrayList<>(List.of(rice));
        model.setMealPlan(recipes);
        recipes.clear();
        parser.parseCommand("generate-list").execute(book);
        List<GroceryItem> original = model.getGroceryList().orElseThrow();

        assertThrows(NullPointerException.class, () -> model.setMealPlan(Arrays.asList(rice, null)));
        assertThrows(NullPointerException.class, () -> model.setMealPlan(null));
        assertThrows(NullPointerException.class, () -> model.setGroceryList(Arrays.asList(original.get(0), null)));
        assertThrows(NullPointerException.class, () -> model.setGroceryList(null));
        assertEquals(List.of(rice), model.getMealPlan());
        assertEquals(original, model.getGroceryList().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> model.getMealPlan().clear());
        assertThrows(UnsupportedOperationException.class, () -> original.clear());

        List<GroceryItem> groceries = new ArrayList<>(original);
        model.setGroceryList(groceries);
        groceries.clear();
        assertEquals(original, model.getGroceryList().orElseThrow());
    }

    @Test
    void view_generatedEmptyList_displaysZeroItems() throws MishMashException {
        model.setGroceryList(List.of());
        assertTrue(parser.parseCommand("view-list").execute(book).endsWith("Total items to purchase: 0"));
    }

    @Test
    void execute_logging_recordsCountsAndLevelsWithoutNames() throws MishMashException {
        Logger logger = Logger.getLogger(GenerateListCommand.class.getName());
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
                // Captured records need no flushing.
            }

            @Override
            public void close() {
                // This handler owns no external resources.
            }
        };
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);
        try {
            assertThrows(MishMashException.class, () -> parser.parseCommand("generate-list").execute(book));
            model.setMealPlan(List.of(createRecipe("Private recipe", "Private ingredient", "1", "g")));
            parser.parseCommand("generate-list").execute(book);
            assertEquals(List.of(Level.WARNING, Level.INFO), records.stream().map(LogRecord::getLevel).toList());
            assertEquals(List.of(1, 1), Arrays.asList(records.get(1).getParameters()));
            assertTrue(records.stream().noneMatch(record -> record.getMessage().contains("Private")));
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
        }
    }

    /**
     * Creates a recipe with one ingredient for command tests.
     */
    private Recipe createRecipe(String name, String ingredient, String quantity, String unit) {
        return new Recipe(name, 0, 0, 0, 0,
                List.of(new GroceryItem(ingredient, new BigDecimal(quantity), unit)));
    }
}
