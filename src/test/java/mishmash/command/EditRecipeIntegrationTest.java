package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import mishmash.exception.MishMashException;
import mishmash.logic.MealPlanGenerator;
import mishmash.model.ActiveMealPlan;
import mishmash.model.GeneratePlanRequest;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.parser.Parser;
import mishmash.testutil.TestRecipes;

/**
 * Checks recipe edits against saved plans, shortfall reports, and subsequent regeneration.
 */
class EditRecipeIntegrationTest {
    @TestFactory
    List<DynamicTest> execute_nutritionEdits_changeShortfallsOnlyAfterRegeneration() {
        List<DynamicTest> tests = new ArrayList<>();
        for (String prefix : List.of("cal", "p", "c", "f")) {
            for (boolean shouldIncrease : List.of(false, true)) {
                String scenario = prefix + (shouldIncrease ? " increase" : " decrease");
                tests.add(DynamicTest.dynamicTest(scenario, () -> verifyNutritionEdit(prefix, shouldIncrease)));
            }
        }
        return tests;
    }

    @TestFactory
    List<DynamicTest> execute_rejectedEdits_preserveShortfallsAndEverySavedSnapshot() {
        List<DynamicTest> tests = new ArrayList<>();
        for (String input : List.of("edit-recipe 1 cal/0 p/-1", "edit-recipe 1 cal/0 cal/1",
                "edit-recipe 1 i/New:2g i/Water:0ml", "edit-recipe 1 cal/0 food/invalid",
                "edit-recipe 1 cal/2147483648", "edit-recipe 4 cal/0")) {
            tests.add(DynamicTest.dynamicTest(input, () -> verifyRejectedEdit(input)));
        }
        return tests;
    }

    /**
     * Verifies one nutrient change against the saved plan and a newly generated replacement.
     */
    private void verifyNutritionEdit(String prefix, boolean shouldIncrease) throws MishMashException {
        ActiveMealPlan active = new ActiveMealPlan();
        Parser parser = new Parser(new MealPlanGenerator(new Random(1)), active);
        RecipeBook book = TestRecipes.recipeBook(100, 100, 100, 100);
        parser.parseCommand("generate-plan d/2").execute(book);
        parser.parseCommand("generate-list").execute(book);
        MealPlan savedPlan = active.get().orElseThrow();
        GeneratePlanRequest savedRequest = active.getRequest().orElseThrow();
        List<GroceryItem> savedGroceries = active.getGroceryList().orElseThrow();
        int dailyTarget = shouldIncrease ? 400 : 300;
        Command check = parser.parseCommand("check-shortfall " + prefix + "/" + dailyTarget);
        String originalReport = check.execute(book);
        assertEquals(shouldIncrease, originalReport.contains("short by 100 "), originalReport);

        int editedNutrition = shouldIncrease ? 200 : 0;
        parser.parseCommand("edit-recipe 1 " + prefix + "/" + editedNutrition).execute(book);

        assertEquals(originalReport, check.execute(book));
        assertSame(savedPlan, active.get().orElseThrow());
        assertSame(savedRequest, active.getRequest().orElseThrow());
        assertSame(savedGroceries, active.getGroceryList().orElseThrow());
        parser.parseCommand("generate-plan d/2").execute(book);
        assertTrue(active.getGroceryList().isEmpty());
        String regeneratedReport = check.execute(book);
        assertEquals(!shouldIncrease, regeneratedReport.contains("short by 100 "), regeneratedReport);
        assertEquals(2, regeneratedReport.lines().filter(line -> line.startsWith("Day ")).count());
        assertEquals(3, book.size());
    }

    /**
     * Verifies that a rejected edit preserves recipes and generated state before a valid edit succeeds.
     */
    private void verifyRejectedEdit(String input) throws MishMashException {
        ActiveMealPlan active = new ActiveMealPlan();
        Parser parser = new Parser(new MealPlanGenerator(new Random(1)), active);
        RecipeBook book = TestRecipes.recipeBook(100, 10, 20, 5);
        parser.parseCommand("generate-plan d/1").execute(book);
        parser.parseCommand("generate-list").execute(book);
        List<Recipe> originalRecipes = book.getRecipes();
        MealPlan savedPlan = active.get().orElseThrow();
        GeneratePlanRequest savedRequest = active.getRequest().orElseThrow();
        List<GroceryItem> savedGroceries = active.getGroceryList().orElseThrow();
        Command check = parser.parseCommand("check-shortfall cal/400 p/40");
        String originalReport = check.execute(book);

        assertThrows(MishMashException.class, () -> parser.parseCommand(input).execute(book));

        assertEquals(originalRecipes, book.getRecipes());
        assertSame(savedPlan, active.get().orElseThrow());
        assertSame(savedRequest, active.getRequest().orElseThrow());
        assertSame(savedGroceries, active.getGroceryList().orElseThrow());
        assertEquals(originalReport, check.execute(book));
        parser.parseCommand("edit-recipe 1 cal/200 p/20").execute(book);
        assertEquals(200, book.getRecipes().getFirst().getCalories());
        assertEquals(originalReport, check.execute(book));
    }
}
