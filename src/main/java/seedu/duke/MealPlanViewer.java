package seedu.duke;

import java.util.List;
import java.util.Objects;

/**
 * Formats an existing meal plan for the view-plan display without changing its meals.
 */
public class MealPlanViewer {
    private static final String NO_PLAN_MESSAGE = "No active meal plan. Use 'generate-plan' first.";

    /**
     * Returns every day of the plan in order, separated by a blank line.
     * Each inner list contains the meals for one day, so the first list represents day 1.
     *
     * @param mealPlan meals grouped by day in the active plan
     * @return the formatted plan, or a message if there is no active plan
     * @throws NullPointerException if the plan, any day, or any meal is null
     */
    public String view(List<List<MealSummary>> mealPlan) {
        Objects.requireNonNull(mealPlan, "mealPlan");
        if (mealPlan.isEmpty()) {
            return NO_PLAN_MESSAGE;
        }

        StringBuilder result = new StringBuilder();
        for (int dayIndex = 0; dayIndex < mealPlan.size(); dayIndex++) {
            if (dayIndex > 0) {
                result.append(System.lineSeparator()).append(System.lineSeparator());
            }
            result.append(formatDay(mealPlan.get(dayIndex), dayIndex + 1));
        }
        return result.toString();
    }

    /**
     * Returns one day of the plan, using the day number supplied through view-plan d/DAY_NUMBER.
     *
     * @param mealPlan meals grouped by day in the active plan
     * @param dayNumber the day to display, starting at 1
     * @return the formatted day, or a message if there is no active plan
     * @throws IllegalArgumentException if the day number is outside a non-empty plan
     * @throws NullPointerException if the plan, selected day, or a selected meal is null
     */
    public String view(List<List<MealSummary>> mealPlan, int dayNumber) {
        Objects.requireNonNull(mealPlan, "mealPlan");
        if (mealPlan.isEmpty()) {
            return NO_PLAN_MESSAGE;
        }
        if (dayNumber < 1 || dayNumber > mealPlan.size()) {
            throw new IllegalArgumentException("Day number must be between 1 and " + mealPlan.size() + ".");
        }
        return formatDay(mealPlan.get(dayNumber - 1), dayNumber);
    }

    /**
     * Formats one day's meals and sums their nutrition values for the daily totals.
     */
    private String formatDay(List<MealSummary> meals, int dayNumber) {
        Objects.requireNonNull(meals, "meals");
        StringBuilder result = new StringBuilder("================ Day ")
                .append(dayNumber).append(" ================");
        // Use long totals so adding several valid integer values cannot overflow an int.
        long calories = 0;
        long protein = 0;
        long carbs = 0;
        long fats = 0;
        int mealNumber = 1;

        for (MealSummary meal : meals) {
            Objects.requireNonNull(meal, "meal");
            result.append(System.lineSeparator())
                    .append("Meal ").append(mealNumber).append(": ").append(meal.name())
                    .append(" (").append(meal.calories()).append(" kcal | P: ")
                    .append(meal.protein()).append("g, C: ").append(meal.carbs())
                    .append("g, F: ").append(meal.fats()).append("g)");
            calories += meal.calories();
            protein += meal.protein();
            carbs += meal.carbs();
            fats += meal.fats();
            mealNumber++;
        }

        result.append(System.lineSeparator())
                .append("--------------------------------------")
                .append(System.lineSeparator())
                .append("Daily Totals: ").append(calories).append(" kcal | Protein: ")
                .append(protein).append("g | Carbs: ").append(carbs)
                .append("g | Fats: ").append(fats).append("g")
                .append(System.lineSeparator())
                .append("======================================");
        return result.toString();
    }

    /**
     * Holds only the meal details needed for display, independently of recipe storage and plan generation.
     * A record supplies read-only accessors such as name() and calories().
     *
     * @param name the meal's display name
     * @param calories total kilocalories in the meal
     * @param protein total grams of protein in the meal
     * @param carbs total grams of carbohydrates in the meal
     * @param fats total grams of fat in the meal
     */
    public record MealSummary(String name, int calories, int protein, int carbs, int fats) {
        /**
         * Creates display data with a non-blank name and non-negative nutrition values.
         *
         * @throws NullPointerException if the name is null
         * @throws IllegalArgumentException if the name is blank or any nutrition value is negative
         */
        public MealSummary {
            Objects.requireNonNull(name, "name");
            if (name.isBlank()) {
                throw new IllegalArgumentException("Meal name must not be blank.");
            }
            if (calories < 0 || protein < 0 || carbs < 0 || fats < 0) {
                throw new IllegalArgumentException("Nutrition values must not be negative.");
            }
        }
    }
}
