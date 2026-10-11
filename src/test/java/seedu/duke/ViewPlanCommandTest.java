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
    private static final String DAY_ONE_DISPLAY = String.join(System.lineSeparator(),
            "================ Day 1 ================",
            "Meal 1: Rice (200 kcal | P: 4g, C: 45g, F: 1g)",
            "--------------------------------------",
            "Daily Totals: 200 kcal | Protein: 4g | Carbs: 45g | Fats: 1g",
            "======================================");
    private static final String DAY_TWO_DISPLAY = String.join(System.lineSeparator(),
            "================ Day 2 ================",
            "Meal 1: Beans (300 kcal | P: 20g, C: 40g, F: 5g)",
            "--------------------------------------",
            "Daily Totals: 300 kcal | Protein: 20g | Carbs: 40g | Fats: 5g",
            "======================================");

    private final RecipeBook recipeBook = new RecipeBook();
    private final List<List<MealSummary>> mealsByDay = List.of(
            List.of(new MealSummary("Rice", 200, 4, 45, 1)),
            List.of(new MealSummary("Beans", 300, 20, 40, 5)));

    @Test
    void execute_allDays_displaysPlanWithoutChangingRecipes() throws MishMashException {
        String expected = DAY_ONE_DISPLAY + System.lineSeparator() + System.lineSeparator() + DAY_TWO_DISPLAY;

        assertEquals(expected, new ViewPlanCommand(() -> mealsByDay).execute(recipeBook));
        assertTrue(recipeBook.getRecipes().isEmpty());
    }

    @Test
    void execute_selectedDay_displaysOnlyRequestedDay() throws MishMashException {
        assertEquals(DAY_TWO_DISPLAY, new ViewPlanCommand(() -> mealsByDay, 2).execute(recipeBook));
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
                () -> new ViewPlanCommand(() -> mealsByDay, 3).execute(recipeBook));

        assertEquals("Day number must be between 1 and 2." + System.lineSeparator()
                + Parser.VIEW_PLAN_USAGE, exception.getMessage());
        assertTrue(recipeBook.getRecipes().isEmpty());
    }

    @Test
    void execute_replacedPlan_readsLatestData() throws MishMashException {
        // This holder lets the test replace the supplied plan without recreating the command.
        AtomicReference<List<List<MealSummary>>> currentMealsByDay = new AtomicReference<>(mealsByDay);
        ViewPlanCommand command = new ViewPlanCommand(currentMealsByDay::get);
        command.execute(recipeBook);

        currentMealsByDay.set(List.of(mealsByDay.get(0)));

        assertEquals(DAY_ONE_DISPLAY, command.execute(recipeBook));
        currentMealsByDay.set(List.of());
        assertEquals("No active meal plan. Use 'generate-plan' first.", command.execute(recipeBook));
    }

    @Test
    void execute_planShrinks_rechecksSelectedDay() throws MishMashException {
        AtomicReference<List<List<MealSummary>>> currentMealsByDay = new AtomicReference<>(mealsByDay);
        ViewPlanCommand command = new ViewPlanCommand(currentMealsByDay::get, 2);
        assertEquals(DAY_TWO_DISPLAY, command.execute(recipeBook));

        currentMealsByDay.set(List.of(mealsByDay.get(0)));

        assertThrows(MishMashException.class, () -> command.execute(recipeBook));
        currentMealsByDay.set(mealsByDay);
        assertEquals(DAY_TWO_DISPLAY, command.execute(recipeBook));
    }

    @Test
    void constructor_invalidDayOrSource_rejectsInput() {
        assertThrows(IllegalArgumentException.class, () -> new ViewPlanCommand(() -> mealsByDay, 0));
        assertThrows(IllegalArgumentException.class, () -> new ViewPlanCommand(() -> mealsByDay, -1));
        assertThrows(NullPointerException.class, () -> new ViewPlanCommand(null));
        assertThrows(NullPointerException.class, () -> new ViewPlanCommand(null, 1));
    }
}
