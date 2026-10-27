package mishmash.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import mishmash.model.NutritionTotals;
import mishmash.model.Recipe;
import mishmash.testutil.TestRecipes;

//@@author GauthamNandi
/**
 * Checks the layout of the recorded-meal display and the confirmation shown after recording a meal.
 */
class MealLogViewerTest {
    private static final String HEADER = "================ Recorded Meals ================";
    private static final String DIVIDER = "------------------------------------------------";
    private static final String FOOTER = "================================================";

    private final MealLogViewer viewer = new MealLogViewer();
    private final Recipe chickenRice = TestRecipes.recipe("Chicken and Rice", 650, 50, 70, 15);
    private final Recipe oatmeal = TestRecipes.recipe("Protein Oatmeal", 450, 35, 60, 8);

    @Test
    void view_severalMeals_listsEachMealThenTotals() {
        String expected = String.join(System.lineSeparator(),
                HEADER,
                "Meal 1: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)",
                "Meal 2: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)",
                DIVIDER,
                "Total: 1100 kcal | Protein: 85g | Carbs: 130g | Fats: 23g",
                FOOTER);

        assertEquals(expected,
                viewer.view(List.of(chickenRice, oatmeal), new NutritionTotals(1100, 85, 130, 23)));
    }

    @Test
    void view_singleMeal_numbersItOne() {
        String expected = String.join(System.lineSeparator(),
                HEADER,
                "Meal 1: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)",
                DIVIDER,
                "Total: 450 kcal | Protein: 35g | Carbs: 60g | Fats: 8g",
                FOOTER);

        assertEquals(expected, viewer.view(List.of(oatmeal), new NutritionTotals(450, 35, 60, 8)));
    }

    @Test
    void view_sameRecipeTwice_showsTwoNumberedLines() {
        String result = viewer.view(List.of(oatmeal, oatmeal), new NutritionTotals(900, 70, 120, 16));

        assertEquals(1, countOccurrences(result, "Meal 1: Protein Oatmeal"));
        assertEquals(1, countOccurrences(result, "Meal 2: Protein Oatmeal"));
    }

    @Test
    void view_noMeals_showsOnlyHeaderAndZeroTotals() {
        String expected = String.join(System.lineSeparator(),
                HEADER,
                DIVIDER,
                "Total: 0 kcal | Protein: 0g | Carbs: 0g | Fats: 0g",
                FOOTER);

        assertEquals(expected, viewer.view(List.of(), new NutritionTotals(0, 0, 0, 0)));
    }

    @Test
    void view_totalsBeyondIntRange_printsFullValue() {
        long beyondInt = 2L * Integer.MAX_VALUE;

        String result = viewer.view(List.of(oatmeal), new NutritionTotals(beyondInt, 0, 0, 0));

        assertEquals(1, countOccurrences(result, "Total: 4294967294 kcal"));
    }

    @Test
    void view_decorativeLines_matchTheHeaderWidth() {
        assertEquals(HEADER.length(), DIVIDER.length());
        assertEquals(HEADER.length(), FOOTER.length());
    }

    @Test
    void view_nullArgumentsOrMeal_throwNullPointerException() {
        NutritionTotals totals = new NutritionTotals(0, 0, 0, 0);

        assertThrows(NullPointerException.class, () -> viewer.view(null, totals));
        assertThrows(NullPointerException.class, () -> viewer.view(List.of(oatmeal), null));
        assertThrows(NullPointerException.class, () -> viewer.view(Arrays.asList(oatmeal, null), totals));
    }

    @Test
    void viewRecorded_firstMeal_showsMealAndRunningTotals() {
        String expected = String.join(System.lineSeparator(),
                "Meal recorded!",
                "Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)",
                "Total so far: 650 kcal | Protein: 50g | Carbs: 70g | Fats: 15g");

        assertEquals(expected, viewer.viewRecorded(chickenRice, new NutritionTotals(650, 50, 70, 15)));
    }

    @Test
    void viewRecorded_laterMeal_showsThatMealButTheCumulativeTotals() {
        String expected = String.join(System.lineSeparator(),
                "Meal recorded!",
                "Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)",
                "Total so far: 1100 kcal | Protein: 85g | Carbs: 130g | Fats: 23g");

        assertEquals(expected, viewer.viewRecorded(oatmeal, new NutritionTotals(1100, 85, 130, 23)));
    }

    @Test
    void viewRecorded_nullArguments_throwNullPointerException() {
        NutritionTotals totals = new NutritionTotals(0, 0, 0, 0);

        assertThrows(NullPointerException.class, () -> viewer.viewRecorded(null, totals));
        assertThrows(NullPointerException.class, () -> viewer.viewRecorded(oatmeal, null));
    }

    private static int countOccurrences(String text, String part) {
        int count = 0;
        for (int from = text.indexOf(part); from >= 0; from = text.indexOf(part, from + part.length())) {
            count++;
        }
        return count;
    }
}
//@@author
