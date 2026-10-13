package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import mishmash.testutil.TestRecipes;
import mishmash.ui.MealPlanViewer;
import mishmash.ui.MealPlanViewer.MealSummary;

/**
 * Checks day totals, defensive copying, and the conversion of a plan to {@link MealPlanViewer} input.
 */
class MealPlanTest {
    private static final Recipe OATMEAL = TestRecipes.recipe("Protein Oatmeal", 450, 35, 60, 8);
    private static final Recipe CHICKEN = TestRecipes.recipe("Chicken and Rice", 650, 50, 70, 15);
    private static final Recipe SALMON = TestRecipes.recipe("Salmon Salad", 550, 42, 20, 32);

    @Test
    void dayPlan_hugeValues_totalsDoNotOverflow() {
        Recipe huge = TestRecipes.recipe("Huge", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE);
        DayPlan day = new DayPlan(List.of(huge, huge, huge));

        assertEquals(3L * Integer.MAX_VALUE, day.getTotalCalories());
        assertEquals(3L * Integer.MAX_VALUE, day.getTotalProtein());
        assertEquals(3L * Integer.MAX_VALUE, day.getTotalCarbs());
        assertEquals(3L * Integer.MAX_VALUE, day.getTotalFats());
    }

    @Test
    void dayPlan_totals_sumAllMeals() {
        DayPlan day = new DayPlan(List.of(OATMEAL, CHICKEN, SALMON));

        assertEquals(1650, day.getTotalCalories());
        assertEquals(127, day.getTotalProtein());
        assertEquals(150, day.getTotalCarbs());
        assertEquals(55, day.getTotalFats());
    }

    @Test
    void dayPlan_nullMeals_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DayPlan(null));
        assertThrows(NullPointerException.class, () -> new DayPlan(Arrays.asList(OATMEAL, null)));
    }

    @Test
    void mealPlan_nullDays_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MealPlan(null));
    }

    @Test
    void getDays_returnsUnmodifiableList() {
        MealPlan plan = new MealPlan(List.of(new DayPlan(List.of(OATMEAL))));

        assertThrows(UnsupportedOperationException.class, () -> plan.getDays().add(new DayPlan(List.of(CHICKEN))));
        assertEquals(1, plan.getNumberOfDays());
    }

    @Test
    void toMealSummaries_twoDays_keepsOrderAndNutritionValues() {
        MealPlan plan = new MealPlan(List.of(
                new DayPlan(List.of(OATMEAL, CHICKEN)),
                new DayPlan(List.of(SALMON))));

        List<List<MealSummary>> expected = List.of(
                List.of(new MealSummary("Protein Oatmeal", 450, 35, 60, 8),
                        new MealSummary("Chicken and Rice", 650, 50, 70, 15)),
                List.of(new MealSummary("Salmon Salad", 550, 42, 20, 32)));
        assertEquals(expected, plan.toMealSummaries());
    }

    @Test
    void toMealSummaries_viewedByMealPlanViewer_showsDailyTotals() {
        MealPlan plan = new MealPlan(List.of(new DayPlan(List.of(OATMEAL, CHICKEN, SALMON))));

        String display = new MealPlanViewer().view(plan.toMealSummaries());

        assertTrue(display.contains("Meal 1: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)"));
        assertTrue(display.contains("Daily Totals: 1650 kcal | Protein: 127g | Carbs: 150g | Fats: 55g"));
    }
}
