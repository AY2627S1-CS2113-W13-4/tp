package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
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
import mishmash.ui.MealPlanViewer.MealSummary;

/**
 * Checks meal-plan viewing through parsing, generation, saved snapshots, and display formatting.
 */
class ViewPlanIntegrationTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final Parser parser = new Parser(new MealPlanGenerator(new Random(1)), activePlan);
    private final RecipeBook recipeBook = TestRecipes.recipeBook(100, 10, 20, 5);

    @Test
    void execute_selectedDayAfterRegeneration_rechecksBoundsWithoutChangingGroceries() throws MishMashException {
        Command secondDay = parser.parseCommand("view-plan d/2");
        parser.parseCommand("generate-plan d/2").execute(recipeBook);
        assertTrue(secondDay.execute(recipeBook).contains("================ Day 2 ================"));
        parser.parseCommand("generate-plan d/1").execute(recipeBook);
        parser.parseCommand("generate-list").execute(recipeBook);
        MealPlan savedPlan = activePlan.get().orElseThrow();
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();
        String groceryDisplay = parser.parseCommand("view-list").execute(recipeBook);

        MishMashException error = assertThrows(MishMashException.class, () -> secondDay.execute(recipeBook));

        assertTrue(error.getMessage().contains("between 1 and 1"));
        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(groceryDisplay, parser.parseCommand("view-list").execute(recipeBook));
        parser.parseCommand("generate-plan d/2").execute(recipeBook);
        String result = secondDay.execute(recipeBook);
        assertTrue(result.contains("================ Day 2 ================"));
        assertFalse(result.contains("================ Day 1 ================"));
        assertTrue(activePlan.getGroceryList().isEmpty());
    }

    @Test
    void execute_independentSources_viewingFailureDoesNotDisableOtherViewer() throws MishMashException {
        MealSummary meal = new MealSummary("Saved meal", 100, 10, 20, 5);
        AtomicReference<List<List<MealSummary>>> days = new AtomicReference<>(List.of(List.of(meal)));
        AtomicReference<Optional<List<GroceryItem>>> groceries = new AtomicReference<>();
        Parser externalParser = new Parser(days::get, groceries::get);
        Command viewPlan = externalParser.parseCommand("view-plan");
        Command viewList = externalParser.parseCommand("view-list");

        assertThrows(IllegalStateException.class, () -> viewList.execute(recipeBook));
        assertTrue(viewPlan.execute(recipeBook).contains("Meal 1: Saved meal"));
        groceries.set(Optional.of(List.of(new GroceryItem("Rice", BigDecimal.ONE, "g"))));
        days.set(null);
        assertThrows(IllegalStateException.class, () -> viewPlan.execute(recipeBook));
        assertTrue(viewList.execute(recipeBook).contains("[ ] Rice: 1g"));
        days.set(List.of(List.of(meal)));
        assertTrue(viewPlan.execute(recipeBook).contains("Daily Totals: 100 kcal"));
        assertEquals(3, recipeBook.size());
    }

    @Test
    void execute_recipesDeleted_keepsSavedPlanViewable() throws MishMashException {
        parser.parseCommand("generate-plan d/2").execute(recipeBook);
        parser.parseCommand("generate-list").execute(recipeBook);
        Command view = parser.parseCommand("view-plan");
        String originalDisplay = view.execute(recipeBook);
        MealPlan savedPlan = activePlan.get().orElseThrow();
        GeneratePlanRequest savedRequest = activePlan.getRequest().orElseThrow();
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();

        while (recipeBook.size() > 0) {
            recipeBook.remove(0);
        }

        assertTrue(originalDisplay.contains("Daily Totals: 300 kcal | Protein: 30g | Carbs: 60g | Fats: 15g"));
        assertEquals(originalDisplay, view.execute(recipeBook));
        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedRequest, activePlan.getRequest().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(0, recipeBook.size());
    }

    @Test
    void execute_clearedPlanAndEmptyDayReplacement_displaysLatestState() throws MishMashException {
        Command view = parser.parseCommand("view-plan");
        parser.parseCommand("generate-plan d/1").execute(recipeBook);
        assertTrue(view.execute(recipeBook).contains("Meal 1:"));

        activePlan.clear();

        assertEquals("No active meal plan. Use 'generate-plan' first.", view.execute(recipeBook));
        MealPlan emptyDayPlan = new MealPlan(List.of(new DayPlan(List.of())));
        activePlan.set(emptyDayPlan);
        String display = view.execute(recipeBook);
        assertTrue(display.contains("Daily Totals: 0 kcal | Protein: 0g | Carbs: 0g | Fats: 0g"));
        assertFalse(display.contains("Meal 1:"));
        assertSame(emptyDayPlan, activePlan.get().orElseThrow());
    }

    @Test
    void execute_largeTotals_preservesValuesThroughParsingAndViewing() throws MishMashException {
        Recipe large = TestRecipes.recipe("Large meal", Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        MealPlan plan = new MealPlan(List.of(new DayPlan(List.of(large, large))));
        activePlan.set(plan);

        String display = parser.parseCommand("view-plan d/1").execute(recipeBook);

        assertTrue(display.contains("Daily Totals: 4294967294 kcal | Protein: 4294967294g"
                + " | Carbs: 4294967294g | Fats: 4294967294g"), display);
        assertSame(plan, activePlan.get().orElseThrow());
    }

    @TestFactory
    Stream<DynamicTest> parseCommand_invalidArguments_preservesSavedStateAndDisplay() {
        return Stream.of("view-plan d/0", "view-plan d/2147483648", "view-plan d/1 d/2", "view-plan extra")
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
        Command view = parser.parseCommand("view-plan");
        String originalDisplay = view.execute(recipeBook);

        MishMashException error = assertThrows(MishMashException.class, () -> parser.parseCommand(input));

        assertTrue(error.getMessage().endsWith(ViewPlanCommand.USAGE));
        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedRequest, activePlan.getRequest().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(originalRecipes, recipeBook.getRecipes());
        assertEquals(originalDisplay, view.execute(recipeBook));
    }
}
