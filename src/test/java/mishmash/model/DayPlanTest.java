package mishmash.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import mishmash.testutil.TestRecipes;

/**
 * Checks combined daily nutrition totals for ordinary meals, empty days, and large values.
 */
class DayPlanTest {
    private static final Recipe OATMEAL = TestRecipes.recipe("Protein Oatmeal", 450, 35, 60, 8);
    private static final Recipe CHICKEN = TestRecipes.recipe("Chicken and Rice", 650, 50, 70, 15);
    private static final Recipe SALMON = TestRecipes.recipe("Salmon Salad", 550, 42, 20, 32);

    @Test
    void calculateDailyNutritionTotals_threeMeals_sumsAllNutrients() {
        DayPlan day = new DayPlan(List.of(OATMEAL, CHICKEN, SALMON));

        assertEquals(new NutritionTotals(1650, 127, 150, 55), day.calculateDailyNutritionTotals());
    }

    @Test
    void calculateDailyNutritionTotals_emptyDay_returnsZeroTotals() {
        DayPlan day = new DayPlan(List.of());

        assertEquals(new NutritionTotals(0, 0, 0, 0), day.calculateDailyNutritionTotals());
    }

    @Test
    void calculateDailyNutritionTotals_zeroNutrition_returnsZeroTotals() {
        DayPlan day = new DayPlan(List.of(TestRecipes.recipe("Water", 0, 0, 0, 0)));

        assertEquals(new NutritionTotals(0, 0, 0, 0), day.calculateDailyNutritionTotals());
    }

    @Test
    void calculateDailyNutritionTotals_singleMeal_returnsMealNutrition() {
        DayPlan day = new DayPlan(List.of(OATMEAL));

        assertEquals(new NutritionTotals(450, 35, 60, 8), day.calculateDailyNutritionTotals());
    }

    @Test
    void calculateDailyNutritionTotals_repeatedMeals_countsEveryOccurrence() {
        DayPlan day = new DayPlan(List.of(CHICKEN, CHICKEN, OATMEAL));

        assertEquals(new NutritionTotals(1750, 135, 200, 38), day.calculateDailyNutritionTotals());
    }

    @Test
    void calculateDailyNutritionTotals_hugeValues_doesNotOverflow() {
        Recipe huge = TestRecipes.recipe("Huge", Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        DayPlan day = new DayPlan(List.of(huge, huge, huge));
        long expectedTotal = 3L * Integer.MAX_VALUE;

        assertEquals(new NutritionTotals(expectedTotal, expectedTotal, expectedTotal, expectedTotal),
                day.calculateDailyNutritionTotals());
    }

    @Test
    void calculateDailyNutritionTotals_twoDaysRepeatedCalls_keepsTotalsIndependentAndMealsUnchanged() {
        List<Recipe> firstMeals = List.of(OATMEAL, CHICKEN, SALMON);
        List<Recipe> secondMeals = List.of(CHICKEN, CHICKEN, OATMEAL);
        DayPlan firstDay = new DayPlan(firstMeals);
        DayPlan secondDay = new DayPlan(secondMeals);
        NutritionTotals firstTotals = firstDay.calculateDailyNutritionTotals();

        assertEquals(new NutritionTotals(1750, 135, 200, 38), secondDay.calculateDailyNutritionTotals());
        assertEquals(new NutritionTotals(1650, 127, 150, 55), firstTotals);
        assertEquals(firstTotals, firstDay.calculateDailyNutritionTotals());
        assertEquals(firstMeals, firstDay.getMeals());
        assertEquals(secondMeals, secondDay.getMeals());
    }
}
