package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import mishmash.model.ActiveMealPlan;
import mishmash.model.DayPlan;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.NutritionTargets;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.testutil.TestRecipes;

//@@author Rixte
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
    void execute_differentShortfallsOnEachDay_keepsDeficitsUnderCorrectDay() {
        activePlan.set(new MealPlan(List.of(
                createDayPlan(1800, 100, 250, 60), createDayPlan(2000, 80, 250, 60))));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 250, 60));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1:",
                "- Calories: 1800/2000 kcal (short by 200 kcal).",
                "Day 2:",
                "- Protein: 80/100 g (short by 20 g).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_middleDayMeetsTargets_doesNotCarryDeficitsBetweenDays() {
        activePlan.set(new MealPlan(List.of(createDayPlan(1800, 100, 250, 60),
                createDayPlan(2000, 100, 250, 60), createDayPlan(2000, 100, 200, 50))));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 250, 60));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1:",
                "- Calories: 1800/2000 kcal (short by 200 kcal).",
                "Day 2: No shortfalls for the supplied targets.",
                "Day 3:",
                "- Carbs: 200/250 g (short by 50 g).",
                "- Fats: 50/60 g (short by 10 g).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_sevenDayPlan_checksTheFinalDayAfterSixDaysWithoutDeficits() {
        DayPlan sufficientDay = createDayPlan(2000, 100, 250, 60);
        activePlan.set(new MealPlan(List.of(sufficientDay, sufficientDay, sufficientDay,
                sufficientDay, sufficientDay, sufficientDay, createDayPlan(1999, 100, 250, 60))));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, null, null, null));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1: No shortfalls for the supplied targets.",
                "Day 2: No shortfalls for the supplied targets.",
                "Day 3: No shortfalls for the supplied targets.",
                "Day 4: No shortfalls for the supplied targets.",
                "Day 5: No shortfalls for the supplied targets.",
                "Day 6: No shortfalls for the supplied targets.",
                "Day 7:",
                "- Calories: 1999/2000 kcal (short by 1 kcal).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_variedMeals_comparesTheirCombinedNutrition() {
        DayPlan day = new DayPlan(List.of(TestRecipes.recipe("Chicken", 600, 40, 70, 15),
                TestRecipes.recipe("Tofu", 500, 25, 60, 12), TestRecipes.recipe("Oats", 400, 15, 50, 10)));
        activePlan.set(new MealPlan(List.of(day)));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 200, 60));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1:",
                "- Calories: 1500/2000 kcal (short by 500 kcal).",
                "- Protein: 80/100 g (short by 20 g).",
                "- Carbs: 180/200 g (short by 20 g).",
                "- Fats: 37/60 g (short by 23 g).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_repeatedMealOccurrences_countsEachOccurrenceOnEveryDay() {
        Recipe meal = TestRecipes.recipe("Repeated", 600, 30, 50, 10);
        DayPlan day = new DayPlan(List.of(meal, meal, meal));
        activePlan.set(new MealPlan(List.of(day, day)));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, 100, 200, 60));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1:",
                "- Calories: 1800/2000 kcal (short by 200 kcal).",
                "- Protein: 90/100 g (short by 10 g).",
                "- Carbs: 150/200 g (short by 50 g).",
                "- Fats: 30/60 g (short by 30 g).",
                "Day 2:",
                "- Calories: 1800/2000 kcal (short by 200 kcal).",
                "- Protein: 90/100 g (short by 10 g).",
                "- Carbs: 150/200 g (short by 50 g).",
                "- Fats: 30/60 g (short by 30 g).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @TestFactory
    List<DynamicTest> execute_everyTargetSubset_reportsOnlyRequestedNutrients() {
        List<DynamicTest> tests = new ArrayList<>();
        List<String> deficitLines = List.of("- Calories: 1800/2000 kcal (short by 200 kcal).",
                "- Protein: 80/100 g (short by 20 g).", "- Carbs: 200/250 g (short by 50 g).",
                "- Fats: 50/60 g (short by 10 g).");
        // Four optional targets have 15 non-empty combinations, represented by masks 1 through 15.
        for (int mask = 1; mask < 16; mask++) {
            NutritionTargets targets = new NutritionTargets((mask & 1) != 0 ? 2000 : null,
                    (mask & 2) != 0 ? 100 : null, (mask & 4) != 0 ? 250 : null, (mask & 8) != 0 ? 60 : null);
            List<String> expectedLines = new ArrayList<>(List.of(
                    "Daily nutrition shortfalls (compared with the supplied targets):", "Day 1:"));
            for (int nutrient = 0; nutrient < 4; nutrient++) {
                if ((mask & (1 << nutrient)) != 0) {
                    expectedLines.add(deficitLines.get(nutrient));
                }
            }
            tests.add(DynamicTest.dynamicTest("Target subset " + mask, () -> {
                ActiveMealPlan plan = new ActiveMealPlan();
                plan.set(new MealPlan(List.of(createDayPlan(1800, 80, 200, 50))));
                CheckShortfallCommand command = new CheckShortfallCommand(plan, targets);

                assertEquals(String.join(System.lineSeparator(), expectedLines), command.execute(recipeBook));
            }));
        }
        return tests;
    }

    @Test
    void execute_maximumTargetsAndTotalsOneBelow_reportsOneUnitForEveryNutrient() {
        activePlan.set(new MealPlan(List.of(createDayPlan(2147483646, 2147483646, 2147483646, 2147483646))));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):", "Day 1:",
                "- Calories: 2147483646/2147483647 kcal (short by 1 kcal).",
                "- Protein: 2147483646/2147483647 g (short by 1 g).",
                "- Carbs: 2147483646/2147483647 g (short by 1 g).",
                "- Fats: 2147483646/2147483647 g (short by 1 g).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_largeCalorieSurplus_doesNotHideProteinDeficit() {
        Recipe meal = TestRecipes.recipe("Large", Integer.MAX_VALUE, 1, 1, 1);
        activePlan.set(new MealPlan(List.of(new DayPlan(List.of(meal, meal)))));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan,
                new NutritionTargets(Integer.MAX_VALUE, 3, null, null));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):", "Day 1:",
                "- Protein: 2/3 g (short by 1 g).");

        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void execute_separateChecksWithDifferentTargets_doNotAffectEachOther() {
        activePlan.set(new MealPlan(List.of(createDayPlan(1800, 80, 200, 50))));
        CheckShortfallCommand higher = new CheckShortfallCommand(activePlan,
                new NutritionTargets(2000, null, null, null));
        CheckShortfallCommand lower = new CheckShortfallCommand(activePlan,
                new NutritionTargets(1800, null, null, null));
        String higherResult = higher.execute(recipeBook);

        assertEquals(String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1: No shortfalls for the supplied targets."), lower.execute(recipeBook));
        assertEquals(higherResult, higher.execute(recipeBook));
        assertTrue(higherResult.contains("Calories: 1800/2000 kcal (short by 200 kcal)."));
        assertTrue(activePlan.getGroceryList().isEmpty());
    }

    @Test
    void execute_generatedEmptyGrocerySnapshot_preservesItsPresenceAndIdentity() {
        activePlan.set(new MealPlan(List.of(createDayPlan(2000, 100, 250, 60))));
        activePlan.setGroceryList(List.of());
        List<GroceryItem> groceries = activePlan.getGroceryList().orElseThrow();

        new CheckShortfallCommand(activePlan, new NutritionTargets(2000, null, null, null)).execute(recipeBook);

        assertSame(groceries, activePlan.getGroceryList().orElseThrow());
        assertTrue(activePlan.getGroceryList().orElseThrow().isEmpty());
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
    void execute_emptyMiddleDay_reportsItsDeficitsAndStillChecksTheFollowingDay() {
        activePlan.set(new MealPlan(List.of(createDayPlan(1, 1, 1, 1),
                new DayPlan(List.of()), createDayPlan(1, 1, 1, 1))));
        CheckShortfallCommand command = new CheckShortfallCommand(activePlan, new NutritionTargets(1, 1, 1, 1));

        String expected = String.join(System.lineSeparator(),
                "Daily nutrition shortfalls (compared with the supplied targets):",
                "Day 1: No shortfalls for the supplied targets.", "Day 2:",
                "- Calories: 0/1 kcal (short by 1 kcal).",
                "- Protein: 0/1 g (short by 1 g).",
                "- Carbs: 0/1 g (short by 1 g).",
                "- Fats: 0/1 g (short by 1 g).",
                "Day 3: No shortfalls for the supplied targets.");

        assertEquals(expected, command.execute(recipeBook));
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
//@@author
