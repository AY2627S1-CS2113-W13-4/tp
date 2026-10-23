package mishmash.model;

import java.util.List;
import java.util.Objects;

/**
 * The meals chosen for a single day of a meal plan, with their nutrition totals.
 */
public class DayPlan {
    private final List<Recipe> meals;

    /**
     * Creates a day plan from the given meals.
     *
     * @param meals The recipes eaten on this day, in order; copied so later changes do not leak in.
     * @throws NullPointerException If the list or any meal is null.
     */
    public DayPlan(List<Recipe> meals) {
        Objects.requireNonNull(meals, "meals");
        this.meals = List.copyOf(meals);
    }

    /**
     * Returns an unmodifiable list of this day's meals.
     */
    public List<Recipe> getMeals() {
        return meals;
    }

    /**
     * Calculates the nutrition totals for every meal occurrence in this day, including repeated recipes.
     * Returns zero for each nutrient when the day has no meals.
     *
     * @return The daily calories in kilocalories and protein, carbohydrates, and fats in grams.
     */
    public NutritionTotals calculateDailyNutritionTotals() {
        return new NutritionTotals(getTotalCalories(), getTotalProtein(), getTotalCarbs(), getTotalFats());
    }

    /**
     * Returns the total kilocalories of all meals in the day.
     */
    public long getTotalCalories() {
        return meals.stream().mapToLong(Recipe::getCalories).sum();
    }

    /**
     * Returns the total grams of protein of all meals in the day.
     */
    public long getTotalProtein() {
        return meals.stream().mapToLong(Recipe::getProtein).sum();
    }

    /**
     * Returns the total grams of carbohydrates of all meals in the day.
     */
    public long getTotalCarbs() {
        return meals.stream().mapToLong(Recipe::getCarbs).sum();
    }

    /**
     * Returns the total grams of fat of all meals in the day.
     */
    public long getTotalFats() {
        return meals.stream().mapToLong(Recipe::getFats).sum();
    }
}
