package mishmash.ui;

import java.util.List;
import java.util.Objects;

import mishmash.model.NutritionTotals;
import mishmash.model.Recipe;

//@@author GauthamNandi
/**
 * Formats recorded meals and their nutrition for {@code record-meal} and {@code view-meals} without changing them.
 */
public class MealLogViewer {
    private static final String HEADER = "================ Recorded Meals ================";
    private static final String DIVIDER = "-".repeat(HEADER.length());
    private static final String FOOTER = "=".repeat(HEADER.length());

    /**
     * Returns every recorded meal in order with its own nutrition, followed by the totals of all of them.
     *
     * @param meals The recorded meals, oldest first.
     * @param totals The nutrition summed over those meals.
     * @return The formatted meals and totals without a trailing line separator.
     * @throws NullPointerException If either argument, or a meal, is null.
     */
    public String view(List<Recipe> meals, NutritionTotals totals) {
        Objects.requireNonNull(meals, "meals");
        Objects.requireNonNull(totals, "totals");

        StringBuilder result = new StringBuilder(HEADER);
        int mealNumber = 1;
        for (Recipe meal : meals) {
            result.append(System.lineSeparator())
                    .append("Meal ").append(mealNumber).append(": ").append(describe(meal));
            mealNumber++;
        }
        result.append(System.lineSeparator()).append(DIVIDER)
                .append(System.lineSeparator()).append(formatTotals("Total", totals))
                .append(System.lineSeparator()).append(FOOTER);
        return result.toString();
    }

    /**
     * Returns the feedback shown right after a meal is recorded: the meal, then the running totals.
     *
     * @param meal The meal that was just recorded.
     * @param totals The nutrition summed over every recorded meal, including this one.
     * @return The confirmation without a trailing line separator.
     * @throws NullPointerException If either argument is null.
     */
    public String viewRecorded(Recipe meal, NutritionTotals totals) {
        Objects.requireNonNull(totals, "totals");
        return "Meal recorded!" + System.lineSeparator()
                + describe(meal) + System.lineSeparator()
                + formatTotals("Total so far", totals);
    }

    /**
     * Describes one meal on a single line, in the same layout as the meal-plan display.
     *
     * @throws NullPointerException If the meal is null.
     */
    private String describe(Recipe meal) {
        Objects.requireNonNull(meal, "meal");
        return meal.getName() + " (" + meal.getCalories() + " kcal | P: " + meal.getProtein()
                + "g, C: " + meal.getCarbs() + "g, F: " + meal.getFats() + "g)";
    }

    /**
     * Formats a labelled totals line.
     *
     * @param label The text before the colon, such as {@code Total}.
     */
    private String formatTotals(String label, NutritionTotals totals) {
        return label + ": " + totals.calories() + " kcal | Protein: " + totals.protein()
                + "g | Carbs: " + totals.carbs() + "g | Fats: " + totals.fats() + "g";
    }
}
//@@author
