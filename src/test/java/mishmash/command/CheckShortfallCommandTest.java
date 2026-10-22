package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import mishmash.model.ActiveMealPlan;
import mishmash.model.DayPlan;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.NutritionTargets;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.testutil.TestRecipes;

/**
 * Checks daily comparison boundaries, omitted targets, and preservation of existing plan data.
 */
class CheckShortfallCommandTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final RecipeBook recipeBook = new RecipeBook();

    @Test
    void execute_multipleDays_reportsShortfallsSeparatelyWithoutChangingData() {
        MealPlan plan = new MealPlan(List.of(
                createDayPlan(1800, 80, 250, 60), createDayPlan(2200, 120, 300, 70)));
        activePlan.set(plan);
        List<GroceryItem> groceries = List.of(new GroceryItem("Rice", new BigDecimal("150"), "g"));
        activePlan.setGroceryList(groceries);
        List<GroceryItem> originalGroceries = activePlan.getGroceryList().orElseThrow();
        recipeBook.add(TestRecipes.recipe("Stored recipe", 100, 10, 10, 10));
        List<Recipe> originalRecipes = recipeBook.getRecipes();
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 250, 60));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1:",
                "- Calories: 1800/2000 kcal (short by 200 kcal).",
                "- Protein: 80/100 g (short by 20 g).",
                "Day 2: No shortfalls for the supplied targets.");

        assertEquals(expected, command.execute(recipeBook));
        assertSame(plan, activePlan.get().orElseThrow());
        assertSame(originalGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(groceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(originalRecipes, recipeBook.getRecipes());
        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_emptyPlan_promptsForGenerationWithoutCreatingGroceries() {
        activePlan.set(new MealPlan(List.of()));

        String result = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, null, null, null)).execute(recipeBook);

        assertEquals("No active meal plan. Use 'generate-plan' first.", result);
        assertTrue(activePlan.getGroceryList().isEmpty());
    }

    @Test
    void execute_emptyDay_reportsZeroTotalsAgainstMinimumAndMaximumTargets() {
        activePlan.set(new MealPlan(List.of(new DayPlan(List.of()))));

        String result = new CheckShortfallCommand(activePlan,
                new NutritionTargets(Integer.MAX_VALUE, 1, 1, 1)).execute(recipeBook);

        assertTrue(result.contains("Calories: 0/2147483647 kcal (short by 2147483647 kcal)."));
        assertTrue(result.contains("Protein: 0/1 g (short by 1 g)."));
        assertTrue(result.contains("Carbs: 0/1 g (short by 1 g)."));
        assertTrue(result.contains("Fats: 0/1 g (short by 1 g)."));
    }

    @Test
    void execute_exactTargetsAndSurpluses_areNotShortfalls() {
        activePlan.set(new MealPlan(List.of(createDayPlan(2000, 100, 250, 70))));

        String result = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 250, 60)).execute(recipeBook);

        assertTrue(result.contains("Day 1: No shortfalls for the supplied targets."));
        assertFalse(result.contains("short by"));
    }

    @Test
    void execute_allNutrientsOneUnitBelowTarget_reportsEvenWithinGenerationTolerance() {
        activePlan.set(new MealPlan(List.of(createDayPlan(1999, 99, 249, 59))));

        String result = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 250, 60)).execute(recipeBook);

        assertTrue(result.contains("Calories: 1999/2000 kcal (short by 1 kcal)."));
        assertTrue(result.contains("Protein: 99/100 g (short by 1 g)."));
        assertTrue(result.contains("Carbs: 249/250 g (short by 1 g)."));
        assertTrue(result.contains("Fats: 59/60 g (short by 1 g)."));
    }

    @Test
    void execute_omittedTargets_doesNotCheckThoseNutrients() {
        activePlan.set(new MealPlan(List.of(createDayPlan(0, 100, 0, 0))));

        String result = new CheckShortfallCommand(activePlan,
                new NutritionTargets(null, 100, null, null)).execute(recipeBook);

        assertTrue(result.contains("No shortfalls for the supplied targets."));
        assertFalse(result.contains("Calories:"));
        assertFalse(result.contains("Carbs:"));
        assertFalse(result.contains("Fats:"));
    }

    @Test
    void execute_largeDailyTotals_doesNotOverflowIntoFalseShortfalls() {
        Recipe large = TestRecipes.recipe("Large", Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        activePlan.set(new MealPlan(List.of(new DayPlan(List.of(large, large, large)))));

        String result = new CheckShortfallCommand(activePlan,
                new NutritionTargets(Integer.MAX_VALUE, Integer.MAX_VALUE,
                        Integer.MAX_VALUE, Integer.MAX_VALUE)).execute(recipeBook);

        assertTrue(result.contains("No shortfalls for the supplied targets."));
    }

    @Test
    void execute_planAddedOrReplacedAfterConstruction_readsLatestPlan() {
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, null, null, null));
        assertEquals("No active meal plan. Use 'generate-plan' first.", command.execute(recipeBook));

        activePlan.set(new MealPlan(List.of(createDayPlan(0, 0, 0, 0))));
        assertTrue(command.execute(recipeBook).contains("short by 2000 kcal"));

        activePlan.set(new MealPlan(List.of(createDayPlan(2000, 0, 0, 0))));
        assertTrue(command.execute(recipeBook).contains("No shortfalls for the supplied targets."));
    }

    @Test
    void constructor_missingTargetsOrDependencies_rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> new CheckShortfallCommand(activePlan, NutritionTargets.none()));
        assertThrows(NullPointerException.class, () -> new CheckShortfallCommand(activePlan, null));
        assertThrows(NullPointerException.class,
                () -> new CheckShortfallCommand(null, new NutritionTargets(2000, null, null, null)));
    }

    /**
     * Creates a day with controlled totals, independently of random plan generation.
     */
    private DayPlan createDayPlan(int calories, int protein, int carbs, int fats) {
        return new DayPlan(List.of(TestRecipes.recipe("Meal", calories, protein, carbs, fats)));
    }
}
