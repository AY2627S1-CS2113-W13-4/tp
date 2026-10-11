package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import seedu.duke.MealPlanViewer.MealSummary;

/**
 * Checks meal-plan command execution, missing plans, day bounds, and access to the latest supplied data.
 */
class ViewPlanCommandTest {
    private static final String DAY_ONE = String.join(System.lineSeparator(),
            "================ Day 1 ================",
            "Meal 1: Rice (200 kcal | P: 4g, C: 45g, F: 1g)",
            "--------------------------------------",
            "Daily Totals: 200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
            "======================================");
    private static final String DAY_TWO = String.join(System.lineSeparator(),
            "================ Day 2 ================",
            "Meal 1: Beans (300 kcal | P: 20g, C: 40g, F: 5g)",
            "--------------------------------------",
            "Daily Totals: 300 kcal | Protein: 20g | Carbs: 40g | Fats: 5g",
            "======================================");

    private final RecipeBook recipeBook = new RecipeBook();
    private final List<List<MealSummary>> plan = List.of(
            List.of(new MealSummary("Rice", 200, 4, 45, 1)),
            List.of(new MealSummary("Beans", 300, 20, 40, 5)));

    @Test
    void execute_allDays_displaysPlanWithoutChangingRecipes() throws MishMashException {
        String expected = DAY_ONE + System.lineSeparator() + System.lineSeparator() + DAY_TWO;

        assertEquals(expected, new ViewPlanCommand(() -> plan).execute(recipeBook));
        assertTrue(recipeBook.getRecipes().isEmpty());
    }

    @Test
    void execute_selectedDay_displaysOnlyRequestedDay() throws MishMashException {
        assertEquals(DAY_TWO, new ViewPlanCommand(() -> plan, 2).execute(recipeBook));
    }

    @Test
    void execute_noActivePlan_displaysGeneratePrompt() throws MishMashException {
        String expected = "No active meal plan. Use 'generate-plan' first.";

        assertEquals(expected, new ViewPlanCommand(() -> List.of()).execute(recipeBook));
        assertEquals(expected, new ViewPlanCommand(() -> List.of(), 2).execute(recipeBook));
    }

    @Test
    void execute_dayOutsideActivePlan_throwsRecoverableError() {
        MishMashException exception = assertThrows(MishMashException.class,
                () -> new ViewPlanCommand(() -> plan, 3).execute(recipeBook));

        assertEquals("Day number must be between 1 and 2." + System.lineSeparator()
                + Parser.VIEW_PLAN_USAGE, exception.getMessage());
        assertTrue(recipeBook.getRecipes().isEmpty());
    }

    @Test
    void execute_replacedPlan_readsLatestData() throws MishMashException {
        // This holder lets the test replace the supplied plan without recreating the command.
        AtomicReference<List<List<MealSummary>>> currentPlan = new AtomicReference<>(plan);
        ViewPlanCommand command = new ViewPlanCommand(currentPlan::get);
        command.execute(recipeBook);

        currentPlan.set(List.of(plan.get(0)));

        assertEquals(DAY_ONE, command.execute(recipeBook));
        currentPlan.set(List.of());
        assertEquals("No active meal plan. Use 'generate-plan' first.", command.execute(recipeBook));
    }

    @Test
    void execute_planShrinks_rechecksSelectedDay() throws MishMashException {
        AtomicReference<List<List<MealSummary>>> currentPlan = new AtomicReference<>(plan);
        ViewPlanCommand command = new ViewPlanCommand(currentPlan::get, 2);
        assertEquals(DAY_TWO, command.execute(recipeBook));

        currentPlan.set(List.of(plan.get(0)));

        assertThrows(MishMashException.class, () -> command.execute(recipeBook));
        currentPlan.set(plan);
        assertEquals(DAY_TWO, command.execute(recipeBook));
    }

    @Test
    void constructor_invalidDayOrSource_rejectsInput() {
        assertThrows(IllegalArgumentException.class, () -> new ViewPlanCommand(() -> plan, 0));
        assertThrows(IllegalArgumentException.class, () -> new ViewPlanCommand(() -> plan, -1));
        assertThrows(NullPointerException.class, () -> new ViewPlanCommand(null));
        assertThrows(NullPointerException.class, () -> new ViewPlanCommand(null, 1));
    }
}
