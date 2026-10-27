package mishmash.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//@@author GauthamNandi
/**
 * Holds the meals the user has recorded as eaten during the current session, in the order they were recorded.
 *
 * <p>This is separate from the active meal plan, which holds meals the user intends to eat. A recorded meal
 * keeps the recipe's nutrition as it was when it was recorded, because {@link Recipe} cannot be changed.
 * The log is kept in memory only and is not saved to disk.</p>
 */
public class MealLog {
    private final List<Recipe> meals = new ArrayList<>();

    /**
     * Adds a meal to the end of the log. Recording the same recipe twice keeps two entries.
     *
     * @param recipe The recipe that was eaten.
     * @throws NullPointerException If the recipe is null.
     */
    public void record(Recipe recipe) {
        meals.add(Objects.requireNonNull(recipe, "recipe"));
    }

    /**
     * Returns a read-only snapshot of the recorded meals, oldest first.
     * Later recordings do not appear in a snapshot that has already been returned.
     */
    public List<Recipe> getMeals() {
        return List.copyOf(meals);
    }

    /**
     * Adds up the nutrition of every recorded meal, recomputing it from the log each time so it cannot
     * differ from the recorded meals.
     *
     * @return The totals, which are all zero if no meal has been recorded.
     */
    public NutritionTotals getTotals() {
        // Sum as long values so several valid integer values cannot overflow an int.
        long calories = meals.stream().mapToLong(Recipe::getCalories).sum();
        long protein = meals.stream().mapToLong(Recipe::getProtein).sum();
        long carbs = meals.stream().mapToLong(Recipe::getCarbs).sum();
        long fats = meals.stream().mapToLong(Recipe::getFats).sum();
        return new NutritionTotals(calories, protein, carbs, fats);
    }
}
//@@author
