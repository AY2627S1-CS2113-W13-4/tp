package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.duke.MealPlanViewer.MealSummary;

/**
 * Checks the view-plan display, day selection, nutrition totals, and invalid input handling.
 */
class MealPlanViewerTest {
    private static final String DAY_ONE_DISPLAY = String.join(System.lineSeparator(),
            "================ Day 1 ================",
            "Meal 1: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)",
            "Meal 2: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)",
            "Meal 3: Salmon Salad (550 kcal | P: 42g, C: 20g, F: 32g)",
            "--------------------------------------",
            "Daily Totals: 1650 kcal | Protein: 127g | Carbs: 150g | Fats: 55g",
            "======================================");
    private static final String DAY_TWO_DISPLAY = String.join(System.lineSeparator(),
            "================ Day 2 ================",
            "Meal 1: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)",
            "Meal 2: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)",
            "Meal 3: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)",
            "--------------------------------------",
            "Daily Totals: 1750 kcal | Protein: 135g | Carbs: 200g | Fats: 38g",
            "======================================");

    private final MealPlanViewer viewer = new MealPlanViewer();

    /**
     * Creates two days with different totals and a repeated meal on day 2.
     */
    private List<List<MealSummary>> createMealsByDay() {
        MealSummary oatmeal = new MealSummary("Protein Oatmeal", 450, 35, 60, 8);
        MealSummary chicken = new MealSummary("Chicken and Rice", 650, 50, 70, 15);
        MealSummary salmon = new MealSummary("Salmon Salad", 550, 42, 20, 32);
        return List.of(List.of(oatmeal, chicken, salmon), List.of(chicken, chicken, oatmeal));
    }

    @Test
    void view_singleDayPlan_matchesSpecifiedOutput() {
        assertEquals(DAY_ONE_DISPLAY, viewer.view(List.of(createMealsByDay().get(0))));
    }

    @Test
    void view_multipleDays_displaysEachDayWithIndependentTotals() {
        String expected = DAY_ONE_DISPLAY + System.lineSeparator() + System.lineSeparator()
                + DAY_TWO_DISPLAY;

        assertEquals(expected, viewer.view(createMealsByDay()));
    }

    @Test
    void view_daysWithDifferentMealCounts_restartsNumberingAndCalculatesTotals() {
        List<List<MealSummary>> mealsByDay = List.of(
                List.of(new MealSummary("Rice", 200, 4, 45, 1),
                        new MealSummary("Beans", 300, 20, 40, 5)),
                List.of(new MealSummary("Salmon", 550, 42, 20, 32)));
        String expected = String.join(System.lineSeparator(),
                "================ Day 1 ================",
                "Meal 1: Rice (200 kcal | P: 4g, C: 45g, F: 1g)",
                "Meal 2: Beans (300 kcal | P: 20g, C: 40g, F: 5g)",
                "--------------------------------------",
                "Daily Totals: 500 kcal | Protein: 24g | Carbs: 85g | Fats: 6g",
                "======================================",
                "",
                "================ Day 2 ================",
                "Meal 1: Salmon (550 kcal | P: 42g, C: 20g, F: 32g)",
                "--------------------------------------",
                "Daily Totals: 550 kcal | Protein: 42g | Carbs: 20g | Fats: 32g",
                "======================================");

        assertEquals(expected, viewer.view(mealsByDay));
    }

    @Test
    void view_firstDay_displaysOnlyFirstDay() {
        assertEquals(DAY_ONE_DISPLAY, viewer.view(createMealsByDay(), 1));
    }

    @Test
    void view_lastDay_preservesDayNumberAndCountsRepeatedMeals() {
        assertEquals(DAY_TWO_DISPLAY, viewer.view(createMealsByDay(), 2));
    }

    @Test
    void view_middleDay_displaysOnlySelectedDay() {
        List<List<MealSummary>> originalDays = createMealsByDay();
        List<List<MealSummary>> mealsByDay = List.of(originalDays.get(0), originalDays.get(1),
                List.of(new MealSummary("Beans", 300, 20, 40, 5)));

        assertEquals(DAY_TWO_DISPLAY, viewer.view(mealsByDay, 2));
    }

    @Test
    void view_emptyDayBetweenMeals_preservesDayNumbers() {
        List<List<MealSummary>> originalDays = createMealsByDay();
        List<List<MealSummary>> mealsByDay = List.of(originalDays.get(0), List.of(), originalDays.get(1));
        String emptyDayDisplay = String.join(System.lineSeparator(),
                "================ Day 2 ================",
                "--------------------------------------",
                "Daily Totals: 0 kcal | Protein: 0g | Carbs: 0g | Fats: 0g",
                "======================================");
        // The former second day's meals now belong to day 3, with the same nutrition totals.
        String thirdDayDisplay = DAY_TWO_DISPLAY.replace("Day 2", "Day 3");
        String expected = String.join(System.lineSeparator() + System.lineSeparator(),
                DAY_ONE_DISPLAY, emptyDayDisplay, thirdDayDisplay);

        assertEquals(expected, viewer.view(mealsByDay));
        assertEquals(emptyDayDisplay, viewer.view(mealsByDay, 2));
        assertEquals(thirdDayDisplay, viewer.view(mealsByDay, 3));
    }

    @Test
    void view_differentPlans_displaysOnlyLatestInput() {
        viewer.view(createMealsByDay());
        viewer.view(createMealsByDay(), 2);
        List<List<MealSummary>> replacementPlan =
                List.of(List.of(new MealSummary("Beans", 300, 20, 40, 5)));
        String expected = String.join(System.lineSeparator(),
                "================ Day 1 ================",
                "Meal 1: Beans (300 kcal | P: 20g, C: 40g, F: 5g)",
                "--------------------------------------",
                "Daily Totals: 300 kcal | Protein: 20g | Carbs: 40g | Fats: 5g",
                "======================================");

        assertEquals(expected, viewer.view(replacementPlan));
        assertEquals(expected, viewer.view(replacementPlan, 1));
        assertEquals("No active meal plan. Use 'generate-plan' first.", viewer.view(List.of()));
    }

    @Test
    void view_dayOutsidePlan_throwsIllegalArgumentException() {
        List<List<MealSummary>> mealsByDay = createMealsByDay();

        assertThrows(IllegalArgumentException.class, () -> viewer.view(mealsByDay, -1));
        assertThrows(IllegalArgumentException.class, () -> viewer.view(mealsByDay, 0));
        assertThrows(IllegalArgumentException.class, () -> viewer.view(mealsByDay, 3));
    }

    @Test
    void view_emptyPlan_displaysGeneratePlanPrompt() {
        String expected = "No active meal plan. Use 'generate-plan' first.";

        assertEquals(expected, viewer.view(List.of()));
        assertEquals(expected, viewer.view(List.of(), 1));
    }

    @Test
    void view_emptyDay_displaysZeroTotals() {
        String expected = String.join(System.lineSeparator(),
                "================ Day 1 ================",
                "--------------------------------------",
                "Daily Totals: 0 kcal | Protein: 0g | Carbs: 0g | Fats: 0g",
                "======================================");
        List<List<MealSummary>> mealsByDay = List.of(List.of());

        assertEquals(expected, viewer.view(mealsByDay));
        assertEquals(expected, viewer.view(mealsByDay, 1));
    }

    @Test
    void view_largeNutritionValues_totalsDoNotOverflow() {
        MealSummary meal = new MealSummary("Large meal", Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        String expected = String.join(System.lineSeparator(),
                "================ Day 1 ================",
                "Meal 1: Large meal (2147483647 kcal | P: 2147483647g, C: 2147483647g, F: 2147483647g)",
                "Meal 2: Large meal (2147483647 kcal | P: 2147483647g, C: 2147483647g, F: 2147483647g)",
                "--------------------------------------",
                "Daily Totals: 4294967294 kcal | Protein: 4294967294g"
                        + " | Carbs: 4294967294g | Fats: 4294967294g",
                "======================================");

        assertEquals(expected, viewer.view(List.of(List.of(meal, meal))));
    }

    @Test
    void view_repeatedCalls_doesNotChangePlan() {
        List<List<MealSummary>> originalMealsByDay = createMealsByDay();
        List<List<MealSummary>> mealsByDay = new ArrayList<>();
        for (List<MealSummary> meals : originalMealsByDay) {
            mealsByDay.add(new ArrayList<>(meals));
        }

        String firstDisplay = viewer.view(mealsByDay);
        viewer.view(mealsByDay, 2);

        assertEquals(firstDisplay, viewer.view(mealsByDay));
        assertEquals(originalMealsByDay, mealsByDay);
    }

    @Test
    void view_nullPlan_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> viewer.view(null));
        assertThrows(NullPointerException.class, () -> viewer.view(null, 1));
    }

    @Test
    void view_nullDay_throwsNullPointerException() {
        // Arrays.asList permits null so the viewer's validation is exercised.
        List<List<MealSummary>> mealsByDay = Arrays.asList(createMealsByDay().get(0), null);

        assertThrows(NullPointerException.class, () -> viewer.view(mealsByDay));
        assertThrows(NullPointerException.class, () -> viewer.view(mealsByDay, 2));
    }

    @Test
    void view_validDayWithAnotherNullDay_displaysOnlySelectedDay() {
        // Viewing one day only needs the selected day's meals to be valid.
        List<List<MealSummary>> mealsByDay = Arrays.asList(createMealsByDay().get(0), null);

        assertEquals(DAY_ONE_DISPLAY, viewer.view(mealsByDay, 1));
    }

    @Test
    void view_nullMeal_throwsNullPointerException() {
        List<MealSummary> meals = Arrays.asList(new MealSummary("Rice", 200, 4, 45, 1), null);
        List<List<MealSummary>> mealsByDay = List.of(meals);

        assertThrows(NullPointerException.class, () -> viewer.view(mealsByDay));
        assertThrows(NullPointerException.class, () -> viewer.view(mealsByDay, 1));
    }

    @Test
    void view_validPlanAfterRejectedInput_displaysOnlyValidMealsAndTotals() {
        // A meal is formatted before the null entry interrupts the failed call.
        List<List<MealSummary>> invalidPlan = List.of(
                Arrays.asList(new MealSummary("Incomplete meal", 999, 90, 80, 70), null));
        List<List<MealSummary>> validPlan = List.of(createMealsByDay().get(0));

        assertThrows(NullPointerException.class, () -> viewer.view(invalidPlan));
        assertEquals(DAY_ONE_DISPLAY, viewer.view(validPlan));
        assertThrows(NullPointerException.class, () -> viewer.view(invalidPlan, 1));
        assertEquals(DAY_ONE_DISPLAY, viewer.view(validPlan, 1));
    }

    @Test
    void mealSummary_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MealSummary(null, 100, 1, 2, 3));
    }

    @Test
    void mealSummary_blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MealSummary("", 100, 1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> new MealSummary(" \t ", 100, 1, 2, 3));
    }

    @Test
    void mealSummary_negativeNutrition_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MealSummary("Rice", -1, 1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> new MealSummary("Rice", 100, -1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> new MealSummary("Rice", 100, 1, -1, 3));
        assertThrows(IllegalArgumentException.class, () -> new MealSummary("Rice", 100, 1, 2, -1));
    }

    @Test
    void view_zeroNutrition_displaysZeroValues() {
        MealSummary meal = new MealSummary("Water", 0, 0, 0, 0);
        String expected = String.join(System.lineSeparator(),
                "================ Day 1 ================",
                "Meal 1: Water (0 kcal | P: 0g, C: 0g, F: 0g)",
                "--------------------------------------",
                "Daily Totals: 0 kcal | Protein: 0g | Carbs: 0g | Fats: 0g",
                "======================================");

        assertEquals(expected, viewer.view(List.of(List.of(meal))));
    }
}
