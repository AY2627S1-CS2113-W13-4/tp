package mishmash.ui;

import java.util.List;
import java.util.Objects;

import mishmash.model.NutritionTargets;
import mishmash.model.NutritionTotals;

/**
 * Formats an existing meal plan for the {@code view-plan} display without changing its meals.
 */
public class MealPlanViewer {
    private static final String NO_PLAN_MESSAGE = "No active meal plan. Use 'generate-plan' first.";

    /**
     * Returns every day of the plan in order, separated by a blank line.
     * Each inner list contains the meals for one day, so the first list represents day 1.
     *
     * @param mealsByDay Meals grouped by day in the active plan.
     * @return The formatted plan, or a message if there is no active plan.
     * @throws NullPointerException If the plan, any day, or any meal is null.
     */
    public String view(List<List<MealSummary>> mealsByDay) {
        return view(mealsByDay, NutritionTargets.none());
    }

    /**
     * Returns every day with warnings for daily totals that exceed supplied targets.
     *
     * @param mealsByDay The plan's meals grouped by day, starting with day 1.
     * @param targets The exact daily targets to compare against.
     * @return The formatted plan, or a message if there is no active plan.
     * @throws NullPointerException If the plan, any day, any meal, or the targets are null.
     */
    public String view(List<List<MealSummary>> mealsByDay, NutritionTargets targets) {
        Objects.requireNonNull(mealsByDay, "mealsByDay");
        Objects.requireNonNull(targets, "targets");
        if (mealsByDay.isEmpty()) {
            return NO_PLAN_MESSAGE;
        }

        StringBuilder result = new StringBuilder();
        for (int dayIndex = 0; dayIndex < mealsByDay.size(); dayIndex++) {
            if (dayIndex > 0) {
                result.append(System.lineSeparator()).append(System.lineSeparator());
            }
            result.append(formatDay(mealsByDay.get(dayIndex), dayIndex + 1, targets));
        }
        return result.toString();
    }

    /**
     * Returns one day of the plan, using the day number supplied through {@code view-plan d/DAY_NUMBER}.
     *
     * @param mealsByDay Meals grouped by day in the active plan.
     * @param dayNumber The day to display, starting at 1.
     * @return The formatted day, or a message if there is no active plan.
     * @throws IllegalArgumentException If the day number is outside a non-empty plan.
     * @throws NullPointerException If the plan, selected day, or a selected meal is null.
     */
    public String view(List<List<MealSummary>> mealsByDay, int dayNumber) {
        return view(mealsByDay, dayNumber, NutritionTargets.none());
    }

    /**
     * Returns one selected day with warnings for totals that exceed supplied targets.
     *
     * @param mealsByDay The plan's meals grouped by day.
     * @param dayNumber The selected day, starting at 1.
     * @param targets The exact daily targets to compare against.
     * @return The formatted day, or a message if there is no active plan.
     * @throws IllegalArgumentException If the day number is outside a non-empty plan.
     * @throws NullPointerException If the plan, selected day, selected meal, or targets are null.
     */
    public String view(List<List<MealSummary>> mealsByDay, int dayNumber, NutritionTargets targets) {
        Objects.requireNonNull(mealsByDay, "mealsByDay");
        Objects.requireNonNull(targets, "targets");
        if (mealsByDay.isEmpty()) {
            return NO_PLAN_MESSAGE;
        }
        if (dayNumber < 1 || dayNumber > mealsByDay.size()) {
            throw new IllegalArgumentException("Day number must be between 1 and " + mealsByDay.size() + ".");
        }

        return formatDay(mealsByDay.get(dayNumber - 1), dayNumber, targets);
    }

    /**
     * Formats one day's meals and sums their nutrition values for the daily totals.
     *
     * @param meals The meals in their planned order for the selected day.
     * @param dayNumber The validated day number in the plan, starting at 1.
     * @param targets The daily targets used for excess warnings.
     * @return The day's meal breakdown and nutrition totals.
     */
    private String formatDay(List<MealSummary> meals, int dayNumber, NutritionTargets targets) {
        assert dayNumber > 0 : "A displayed day must have a positive number: " + dayNumber;
        Objects.requireNonNull(meals, "meals");

        StringBuilder result = new StringBuilder("================ Day ")
                .append(dayNumber).append(" ================");
        result.append(formatMeals(meals));
        result.append(formatDailyTotals(meals, targets));
        return result.toString();
    }

    //@@author Rixte
    /**
     * Formats each meal in its planned order, starting the numbering at one for each day.
     *
     * @param meals The meals to display.
     * @return The meal lines, each preceded by a line separator.
     * @throws NullPointerException If a meal is null.
     */
    private String formatMeals(List<MealSummary> meals) {
        StringBuilder result = new StringBuilder();
        int mealNumber = 1;
        for (MealSummary meal : meals) {
            Objects.requireNonNull(meal, "meal");
            result.append(System.lineSeparator())
                    .append("Meal ").append(mealNumber).append(": ").append(meal.name())
                    .append(" (").append(meal.calories()).append(" kcal | P: ")
                    .append(meal.protein()).append("g, C: ").append(meal.carbs())
                    .append("g, F: ").append(meal.fats()).append("g)");

            mealNumber++;
        }
        return result.toString();
    }
    //@@author

    /**
     * Sums a day's nutrition and formats the footer, including zero totals for an empty day.
     *
     * @param meals The meals already checked while formatting their display lines.
     * @param targets The daily targets used for excess warnings.
     * @return The daily totals and separators, preceded by a line separator.
     */
    private String formatDailyTotals(List<MealSummary> meals, NutritionTargets targets) {
        // Sum as long values so several valid integer values cannot overflow an int.
        long calories = meals.stream().mapToLong(MealSummary::calories).sum();
        long protein = meals.stream().mapToLong(MealSummary::protein).sum();
        long carbs = meals.stream().mapToLong(MealSummary::carbs).sum();
        long fats = meals.stream().mapToLong(MealSummary::fats).sum();

        StringBuilder result = new StringBuilder(System.lineSeparator());
        result.append("--------------------------------------")
                .append(System.lineSeparator())
                .append("Daily Totals: ").append(calories).append(" kcal | Protein: ")
                .append(protein).append("g | Carbs: ").append(carbs)
                .append("g | Fats: ").append(fats).append("g")
                .append(formatWarnings(new NutritionTotals(calories, protein, carbs, fats), targets))
                .append(System.lineSeparator())
                .append("======================================");
        return result.toString();
    }

    /**
     * Formats only exceeded targets, in the same nutrient order as the daily totals.
     */
    private String formatWarnings(NutritionTotals totals, NutritionTargets targets) {
        NutritionTotals excesses = targets.calculateExcesses(totals);
        return formatWarning("Calories exceed", "kcal", totals.calories(), targets.getCalories(), excesses.calories())
                + formatWarning("Protein exceeds", "g", totals.protein(), targets.getProtein(), excesses.protein())
                + formatWarning("Carbs exceed", "g", totals.carbs(), targets.getCarbs(), excesses.carbs())
                + formatWarning("Fats exceed", "g", totals.fats(), targets.getFats(), excesses.fats());
    }

    /**
     * Formats one warning with its excess, actual total, and supplied target, or nothing for zero excess.
     */
    private String formatWarning(String subject, String unit, long actual, Integer target, long excess) {
        if (excess == 0) {
            return "";
        }
        return System.lineSeparator() + "Warning: " + subject + " target by " + excess + " " + unit
                + " (" + actual + " " + unit + " actual; " + target + " " + unit + " target).";
    }

    //@@author Rixte
    /**
     * Holds only the meal details needed for display, independently of recipe storage and plan generation.
     * A record supplies read-only accessors such as {@code name()} and {@code calories()}.
     *
     * @param name The meal's display name.
     * @param calories Total kilocalories in the meal.
     * @param protein Total grams of protein in the meal.
     * @param carbs Total grams of carbohydrates in the meal.
     * @param fats Total grams of fat in the meal.
     */
    public record MealSummary(String name, int calories, int protein, int carbs, int fats) {
        /**
         * Creates display data with a non-blank name and non-negative nutrition values.
         *
         * @throws NullPointerException If the name is null.
         * @throws IllegalArgumentException If the name is blank or any nutrition value is negative.
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
    //@@author
}
