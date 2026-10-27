package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.FoodInfo;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealLog;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;

//@@author Rixte
/**
 * Checks resetting through shared parser state while preserving recipes, dietary rules, and food intake records.
 */
class ResetPlanIntegrationTest {
    private final ActiveMealPlan activePlan = new ActiveMealPlan();
    private final MealLog mealLog = new MealLog();
    private final RecipeBook book = createRecipeBook();
    private final Parser parser = new Parser(mealLog, new MealPlanGenerator(new Random(1)), activePlan);

    @Test
    void execute_generatedPlan_preservesRecipesDietAndRecordedMeals() throws MishMashException {
        parser.parseCommand("set-diet diet/vegetarian avoid/milk exclude/Peanut").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        parser.parseCommand("generate-plan d/2 cal/1500 p/60 c/150 f/30").execute(book);
        parser.parseCommand("generate-list").execute(book);
        List<Recipe> recipes = book.getRecipes();
        List<Recipe> recordedMeals = mealLog.getMeals();
        String dietDisplay = parser.parseCommand("view-diet").execute(book);
        String recordedDisplay = parser.parseCommand("view-meals").execute(book);

        parser.parseCommand("reset-plan").execute(book);

        assertTrue(activePlan.get().isEmpty());
        assertTrue(activePlan.getRequest().isEmpty());
        assertTrue(activePlan.getGroceryList().isEmpty());
        assertEquals(recipes, book.getRecipes());
        assertEquals(recordedMeals, mealLog.getMeals());
        assertSame(recipes.getFirst(), mealLog.getMeals().getFirst());
        assertEquals(dietDisplay, parser.parseCommand("view-diet").execute(book));
        assertEquals(recordedDisplay, parser.parseCommand("view-meals").execute(book));
        assertEquals("No active meal plan. Use 'generate-plan' first.",
                parser.parseCommand("view-plan").execute(book));
        assertEquals("No grocery list generated. Use 'generate-list' first.",
                parser.parseCommand("view-list").execute(book));
        assertEquals("No active meal plan. Use 'generate-plan' first.",
                parser.parseCommand("check-shortfall cal/1500").execute(book));
    }

    @Test
    void execute_resetThenRegenerate_usesRetainedRecipesWithoutOldTargetsOrGroceries() throws MishMashException {
        parser.parseCommand("generate-plan d/2 cal/1500").execute(book);
        parser.parseCommand("generate-list").execute(book);
        MealPlan oldPlan = activePlan.get().orElseThrow();
        List<GroceryItem> oldGroceries = activePlan.getGroceryList().orElseThrow();
        parser.parseCommand("reset-plan").execute(book);

        parser.parseCommand("generate-plan d/1").execute(book);

        assertEquals(1, activePlan.get().orElseThrow().getNumberOfDays());
        assertFalse(activePlan.getRequest().orElseThrow().targets().hasAnyTarget());
        assertTrue(activePlan.getGroceryList().isEmpty());
        parser.parseCommand("generate-list").execute(book);
        assertEquals(List.of("Rice: 300g"), activePlan.getGroceryList().orElseThrow().stream()
                .map(Object::toString).toList());
        assertEquals(2, oldPlan.getNumberOfDays());
        assertEquals(List.of("Rice: 600g"), oldGroceries.stream().map(Object::toString).toList());
        assertEquals(3, book.size());
    }

    @Test
    void execute_reset_keepsDietaryFilteringForFutureGeneration() throws MishMashException {
        parser.parseCommand("set-diet diet/vegetarian").execute(book);
        parser.parseCommand("generate-plan d/1").execute(book);
        parser.parseCommand("reset-plan").execute(book);
        parser.parseCommand("edit-recipe 1 food/poultry").execute(book);
        List<Recipe> recipes = book.getRecipes();

        assertThrows(MishMashException.class, () -> parser.parseCommand("generate-plan d/1").execute(book));

        assertTrue(activePlan.get().isEmpty());
        assertTrue(activePlan.getRequest().isEmpty());
        assertTrue(activePlan.getGroceryList().isEmpty());
        assertEquals(recipes, book.getRecipes());
        parser.parseCommand("edit-recipe 1 food/none").execute(book);
        parser.parseCommand("generate-plan d/1").execute(book);
        assertTrue(activePlan.get().isPresent());
    }

    @Test
    void execute_defaultParser_reusesOnePlanHolderAcrossResetGenerationAndViewing() throws MishMashException {
        Parser defaultParser = new Parser();
        Command reset = defaultParser.parseCommand("reset-plan");
        defaultParser.parseCommand("generate-plan d/1").execute(book);
        defaultParser.parseCommand("generate-list").execute(book);

        reset.execute(book);

        assertTrue(defaultParser.parseCommand("view-plan").execute(book).startsWith("No active meal plan."));
        assertTrue(defaultParser.parseCommand("view-list").execute(book).startsWith("No grocery list generated."));
        defaultParser.parseCommand("generate-plan d/1").execute(book);
        assertTrue(defaultParser.parseCommand("view-plan").execute(book).contains("Daily Totals: 1500 kcal"));
    }

    @Test
    void execute_resetAfterFailedGeneration_removesPreservedPlanButKeepsOtherData() throws MishMashException {
        parser.parseCommand("record-meal 1").execute(book);
        parser.parseCommand("generate-plan d/1").execute(book);
        parser.parseCommand("generate-list").execute(book);
        MealPlan savedPlan = activePlan.get().orElseThrow();
        GeneratePlanRequest savedRequest = activePlan.getRequest().orElseThrow();
        List<GroceryItem> savedGroceries = activePlan.getGroceryList().orElseThrow();
        List<Recipe> recipes = book.getRecipes();
        List<Recipe> meals = mealLog.getMeals();

        assertThrows(MishMashException.class, () -> parser.parseCommand("generate-plan d/2 cal/1").execute(book));

        assertSame(savedPlan, activePlan.get().orElseThrow());
        assertSame(savedRequest, activePlan.getRequest().orElseThrow());
        assertSame(savedGroceries, activePlan.getGroceryList().orElseThrow());
        assertEquals(recipes, book.getRecipes());
        parser.parseCommand("reset-plan").execute(book);
        assertTrue(activePlan.get().isEmpty());
        assertTrue(activePlan.getRequest().isEmpty());
        assertTrue(activePlan.getGroceryList().isEmpty());
        assertEquals(recipes, book.getRecipes());
        assertEquals(meals, mealLog.getMeals());
    }

    @Test
    void execute_noPlan_preservesDietaryRulesAndRepeatedFoodRecords() throws MishMashException {
        parser.parseCommand("set-diet diet/vegetarian avoid/milk").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        String diet = parser.parseCommand("view-diet").execute(book);
        String foodRecords = parser.parseCommand("view-meals").execute(book);
        List<Recipe> recipes = book.getRecipes();

        assertEquals("No active meal plan to reset.", parser.parseCommand("reset-plan").execute(book));

        assertEquals(diet, parser.parseCommand("view-diet").execute(book));
        assertEquals(foodRecords, parser.parseCommand("view-meals").execute(book));
        assertEquals(2, mealLog.getMeals().size());
        assertEquals(recipes, book.getRecipes());
    }

    @Test
    void execute_failedFirstGeneration_resetKeepsRetainedRecipesUsable() throws MishMashException {
        List<Recipe> recipes = book.getRecipes();
        assertThrows(MishMashException.class, () -> parser.parseCommand("generate-plan d/1 cal/1").execute(book));
        assertTrue(activePlan.get().isEmpty());
        assertTrue(activePlan.getRequest().isEmpty());
        assertTrue(activePlan.getGroceryList().isEmpty());
        assertEquals(recipes, book.getRecipes());

        assertEquals("No active meal plan to reset.", parser.parseCommand("reset-plan").execute(book));

        parser.parseCommand("generate-plan d/1").execute(book);
        assertTrue(activePlan.get().isPresent());
        assertEquals(recipes, book.getRecipes());
    }

    @Test
    void execute_pendingReset_keepsEditedRecipesAndOriginalFoodRecordSnapshots() throws MishMashException {
        parser.parseCommand("record-meal 1").execute(book);
        Recipe recorded = mealLog.getMeals().getFirst();
        Command reset = parser.parseCommand("reset-plan");
        parser.parseCommand("edit-recipe 1 n/Edited p/50").execute(book);
        parser.parseCommand("generate-plan d/1").execute(book);
        Recipe edited = book.getRecipes().getFirst();

        reset.execute(book);

        assertSame(edited, book.getRecipes().getFirst());
        assertEquals("Edited", edited.getName());
        assertEquals(50, edited.getProtein());
        assertSame(recorded, mealLog.getMeals().getFirst());
        assertEquals("A", recorded.getName());
        assertEquals(20, recorded.getProtein());
        assertTrue(activePlan.get().isEmpty());
    }

    @Test
    void execute_repeatedResetCycles_keepsRecipesDietAndFoodLogAvailable() throws MishMashException {
        parser.parseCommand("set-diet diet/vegetarian").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        List<Recipe> recipes = book.getRecipes();
        List<Recipe> meals = mealLog.getMeals();
        String diet = parser.parseCommand("view-diet").execute(book);
        Command reset = parser.parseCommand("reset-plan");
        for (int days = 1; days <= 3; days++) {
            parser.parseCommand("generate-plan d/" + days).execute(book);
            parser.parseCommand("generate-list").execute(book);

            reset.execute(book);

            assertTrue(activePlan.get().isEmpty());
            assertTrue(activePlan.getRequest().isEmpty());
            assertTrue(activePlan.getGroceryList().isEmpty());
            assertEquals(recipes, book.getRecipes());
            assertEquals(meals, mealLog.getMeals());
            assertEquals(diet, parser.parseCommand("view-diet").execute(book));
        }
    }

    @Test
    void execute_rejectedReset_preservesPlanRulesAndFoodRecordsBeforeRetry() throws MishMashException {
        parser.parseCommand("set-diet diet/vegetarian").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        parser.parseCommand("generate-plan d/1").execute(book);
        parser.parseCommand("generate-list").execute(book);
        MealPlan plan = activePlan.get().orElseThrow();
        GeneratePlanRequest request = activePlan.getRequest().orElseThrow();
        List<GroceryItem> groceries = activePlan.getGroceryList().orElseThrow();
        List<Recipe> recipes = book.getRecipes();
        String diet = parser.parseCommand("view-diet").execute(book);
        String meals = parser.parseCommand("view-meals").execute(book);
        for (String arguments : List.of("d/1", "yes", "--force")) {
            assertThrows(MishMashException.class, () -> parser.parseCommand("reset-plan " + arguments));

            assertSame(plan, activePlan.get().orElseThrow());
            assertSame(request, activePlan.getRequest().orElseThrow());
            assertSame(groceries, activePlan.getGroceryList().orElseThrow());
            assertEquals(recipes, book.getRecipes());
            assertEquals(diet, parser.parseCommand("view-diet").execute(book));
            assertEquals(meals, parser.parseCommand("view-meals").execute(book));
        }
        parser.parseCommand("reset-plan").execute(book);
        assertTrue(activePlan.get().isEmpty());
    }

    @Test
    void execute_separateParserSessions_resetDoesNotClearAnotherSessionsPlan() throws MishMashException {
        ActiveMealPlan otherPlan = new ActiveMealPlan();
        Parser otherParser = new Parser(new MealLog(), new MealPlanGenerator(new Random(2)), otherPlan);
        parser.parseCommand("generate-plan d/1").execute(book);
        otherParser.parseCommand("generate-plan d/2").execute(book);
        otherParser.parseCommand("generate-list").execute(book);
        MealPlan savedPlan = otherPlan.get().orElseThrow();
        List<GroceryItem> savedGroceries = otherPlan.getGroceryList().orElseThrow();

        parser.parseCommand("reset-plan").execute(book);

        assertTrue(activePlan.get().isEmpty());
        assertSame(savedPlan, otherPlan.get().orElseThrow());
        assertSame(savedGroceries, otherPlan.getGroceryList().orElseThrow());
        assertEquals(2, savedPlan.getNumberOfDays());
    }

    @Test
    void execute_resetThenEditAndRecord_keepsOldFoodSnapshotsAndUsesNewRecipesForPlanning() throws MishMashException {
        parser.parseCommand("set-diet diet/vegetarian avoid/milk exclude/Peanut").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        parser.parseCommand("generate-plan d/1 cal/1500").execute(book);
        MealPlan oldPlan = activePlan.get().orElseThrow();
        Recipe originalMeal = mealLog.getMeals().getFirst();
        String diet = parser.parseCommand("view-diet").execute(book);
        parser.parseCommand("reset-plan").execute(book);

        parser.parseCommand("edit-recipe 1 n/Edited cal/600").execute(book);
        parser.parseCommand("record-meal 1").execute(book);
        parser.parseCommand("record-meal 2").execute(book);
        Recipe editedMeal = book.getRecipes().getFirst();
        List<Recipe> recordedMeals = List.of(originalMeal, editedMeal, book.getRecipes().get(1));

        assertEquals(recordedMeals, mealLog.getMeals());
        assertEquals("A", originalMeal.getName());
        assertEquals(500, originalMeal.getCalories());
        assertEquals("Edited", editedMeal.getName());
        assertEquals(600, editedMeal.getCalories());
        assertEquals(1600L, mealLog.getTotals().calories());
        assertEquals(diet, parser.parseCommand("view-diet").execute(book));
        assertTrue(activePlan.get().isEmpty());

        parser.parseCommand("generate-plan d/1 cal/1600").execute(book);
        parser.parseCommand("generate-list").execute(book);

        assertEquals(1600, activePlan.get().orElseThrow().getDays().getFirst().getTotalCalories());
        assertEquals(1600, activePlan.getRequest().orElseThrow().targets().getCalories());
        assertEquals(List.of("Rice: 300g"), activePlan.getGroceryList().orElseThrow().stream()
                .map(Object::toString).toList());
        assertEquals(1500, oldPlan.getDays().getFirst().getTotalCalories());

        parser.parseCommand("reset-plan").execute(book);

        assertSame(editedMeal, book.getRecipes().getFirst());
        assertEquals(recordedMeals, mealLog.getMeals());
        assertEquals(diet, parser.parseCommand("view-diet").execute(book));
        assertTrue(activePlan.get().isEmpty());
        assertTrue(activePlan.getRequest().isEmpty());
        assertTrue(activePlan.getGroceryList().isEmpty());
    }

    /**
     * Returns three classified recipes with predictable totals, all compatible with a vegetarian diet.
     */
    private RecipeBook createRecipeBook() {
        RecipeBook recipes = new RecipeBook();
        for (String name : List.of("A", "B", "C")) {
            recipes.add(new Recipe(name, 500, 20, 50, 10,
                    List.of(new GroceryItem("Rice", new BigDecimal("100"), "g")), FoodInfo.parseField("none")));
        }
        return recipes;
    }
}
//@@author
