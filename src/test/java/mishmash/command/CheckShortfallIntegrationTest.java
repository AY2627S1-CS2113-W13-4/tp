package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.DayPlan;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.NutritionTargets;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;
import mishmash.testutil.TestRecipes;

/**
 * Checks shortfall commands against replaced plans and invalid internal nutrition totals.
 */
class CheckShortfallIntegrationTest {
    @Test
    void execute_planClearedAfterParsing_promptsThenReadsReplacement() throws MishMashException {
        ActiveMealPlan active = new ActiveMealPlan();
        RecipeBook book = new RecipeBook();
        Recipe meal = TestRecipes.recipe("Meal", 100, 10, 20, 5);
        active.set(new MealPlan(List.of(new DayPlan(List.of(meal)))));
        Command check = new Parser(new MealPlanGenerator(new Random(1)), active)
                .parseCommand("check-shortfall cal/200");
        assertTrue(check.execute(book).contains("short by 100 kcal"));

        active.clear();

        assertEquals("No active meal plan. Use 'generate-plan' first.", check.execute(book));
        assertTrue(active.get().isEmpty());
        assertTrue(active.getRequest().isEmpty());
        assertTrue(active.getGroceryList().isEmpty());
        active.set(new MealPlan(List.of(new DayPlan(List.of(meal, meal)))));
        assertTrue(check.execute(book).contains("No shortfalls for the supplied targets."));
        assertFalse(check.execute(book).contains("short by"));
    }

    @Test
    void execute_negativeModelTotal_assertsWithoutChangingSavedState() {
        // A faulty model implementation simulates an internal bug, rather than invalid user input.
        DayPlan invalid = new DayPlan(List.of()) {
            @Override
            public long getTotalCalories() {
                return -1;
            }
        };
        ActiveMealPlan active = new ActiveMealPlan();
        MealPlan plan = new MealPlan(List.of(invalid));
        active.set(plan);
        active.setGroceryList(List.of());
        List<GroceryItem> groceries = active.getGroceryList().orElseThrow();

        AssertionError error = assertThrows(AssertionError.class,
                () -> new CheckShortfallCommand(active, new NutritionTargets(1, null, null, null))
                        .execute(new RecipeBook()));

        assertEquals("Planned nutrition totals must not be negative.", error.getMessage());
        assertSame(plan, active.get().orElseThrow());
        assertSame(groceries, active.getGroceryList().orElseThrow());
    }
}
