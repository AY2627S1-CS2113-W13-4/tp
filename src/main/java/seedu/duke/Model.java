package seedu.duke;

import java.util.List;
import java.util.Optional;

/**
 * Shares the active meal plan and its generated grocery list between commands in one session.
 */
public class Model {
    private List<Recipe> mealPlan = List.of();
    // Absence differs from a generated list that contains no items.
    private Optional<List<GroceryItem>> groceryList = Optional.empty();

    /**
     * Replaces the meal plan with a read-only snapshot and invalidates its old grocery list.
     * Each recipe occurrence represents one meal, including repeated recipes.
     *
     * @param recipes The recipes in meal order, supplied by the meal-plan generator.
     * @throws NullPointerException If the list or any recipe is null.
     */
    public void setMealPlan(List<Recipe> recipes) {
        // Copy before changing either field so invalid input leaves the current state intact.
        List<Recipe> snapshot = List.copyOf(recipes);
        mealPlan = snapshot;
        groceryList = Optional.empty();
    }

    /**
     * Returns the read-only active meal plan, or an empty list if no meals are planned.
     *
     * @return The recipes in meal order.
     */
    public List<Recipe> getMealPlan() {
        return mealPlan;
    }

    /**
     * Stores a read-only snapshot after grocery generation has succeeded.
     *
     * @param items The generated grocery items.
     * @throws NullPointerException If the list or any item is null.
     */
    public void setGroceryList(List<GroceryItem> items) {
        groceryList = Optional.of(List.copyOf(items));
    }

    /**
     * Returns the generated grocery list, or an empty optional before generation.
     *
     * @return The optional read-only grocery-list snapshot.
     */
    public Optional<List<GroceryItem>> getGroceryList() {
        return groceryList;
    }
}
