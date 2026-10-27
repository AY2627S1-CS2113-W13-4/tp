package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.DayPlan;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;
import mishmash.testutil.TestRecipes;

/**
 * Checks grocery-list viewing through parsing, generation, saved snapshots, and decimal formatting.
 */
class ViewListIntegrationTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final Parser parser = new Parser(new MealPlanGenerator(new Random(1)), activePlan);
    private final RecipeBook recipeBook = TestRecipes.recipeBook(100, 10, 20, 5);

    @Test
    void execute_recipesDeleted_keepsSavedGroceriesViewable() throws MishMashException {
        parser.parseCommand("generate-plan d/2").execute(recipeBook);
        parser.parseCommand("generate-list").execute(recipeBook);
        Command view = parser.parseCommand("view-list");
        String originalDisplay = view.execute(recipeBook);
        MealPlan savedPlan = activePlan.get().orElseThrow();
        GeneratePlanRequest savedRequest = activePlan.getRequest().orElseThrow();
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();

        while (recipeBook.size() > 0) {
            recipeBook.remove(0);
        }

        assertTrue(originalDisplay.contains("[ ] Ingredient: 6g"));
        assertEquals(originalDisplay, view.execute(recipeBook));
        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedRequest, activePlan.getRequest().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(0, recipeBook.size());
    }

    @Test
    void execute_clearedPlanAndEmptyGroceries_distinguishesMissingFromGeneratedEmpty() throws MishMashException {
        Command view = parser.parseCommand("view-list");
        parser.parseCommand("generate-plan d/1").execute(recipeBook);
        parser.parseCommand("generate-list").execute(recipeBook);
        assertTrue(view.execute(recipeBook).contains("[ ] Ingredient: 3g"));

        activePlan.clear();

        assertEquals("No grocery list generated. Use 'generate-list' first.", view.execute(recipeBook));
        MealPlan emptyDayPlan = new MealPlan(List.of(new DayPlan(List.of())));
        activePlan.set(emptyDayPlan);
        activePlan.setGroceryList(List.of());
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();
        assertTrue(view.execute(recipeBook).endsWith("Total items to purchase: 0"));
        assertSame(emptyDayPlan, activePlan.get().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
    }

    @Test
    void execute_tinyQuantity_preservesPrecisionThroughParsingAndViewing() throws MishMashException {
        parser.parseCommand("generate-plan d/1").execute(recipeBook);
        MealPlan savedPlan = activePlan.get().orElseThrow();
        activePlan.setGroceryList(List.of(new GroceryItem("Rice", new BigDecimal("0.000001"), "g")));
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();

        String display = parser.parseCommand("view-list").execute(recipeBook);

        assertTrue(display.contains("[ ] Rice: 0.000001g"), display);
        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
    }

    @TestFactory
    Stream<DynamicTest> parseCommand_invalidArguments_preservesSavedStateAndDisplay() {
        return Stream.of("view-list d/1", "view-list extra")
                .map(input -> DynamicTest.dynamicTest(input, () -> verifyRejectedViewing(input)));
    }

    /**
     * Checks invalid viewing syntax while preserving the displayed snapshot and related saved state.
     */
    private void verifyRejectedViewing(String input) throws MishMashException {
        parser.parseCommand("generate-plan d/1").execute(recipeBook);
        parser.parseCommand("generate-list").execute(recipeBook);
        MealPlan savedPlan = activePlan.get().orElseThrow();
        GeneratePlanRequest savedRequest = activePlan.getRequest().orElseThrow();
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();
        List<Recipe> originalRecipes = recipeBook.getRecipes();
        Command view = parser.parseCommand("view-list");
        String originalDisplay = view.execute(recipeBook);

        MishMashException error = assertThrows(MishMashException.class, () -> parser.parseCommand(input));

        assertTrue(error.getMessage().endsWith(ViewListCommand.USAGE));
        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedRequest, activePlan.getRequest().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(originalRecipes, recipeBook.getRecipes());
        assertEquals(originalDisplay, view.execute(recipeBook));
    }
}
