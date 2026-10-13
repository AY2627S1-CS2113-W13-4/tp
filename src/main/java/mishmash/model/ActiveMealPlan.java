package mishmash.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds the active meal plan and its generated grocery snapshot in memory.
 *
 * <p>Only one plan is active at a time; generating a new plan replaces the old one.
 * The plan is not saved to disk (persistence is out of scope for V1).</p>
 *
 * <p>This is a plain object rather than a static singleton so that the app can
 * create one instance and share it, and tests can create a fresh one each time.</p>
 */
public class ActiveMealPlan {
    private MealPlan plan;
    // Absence means groceries have not been generated for the current plan.
    private Optional<List<GroceryItem>> groceryList = Optional.empty();

    /**
     * Makes the given plan active and clears groceries derived from the previous plan.
     *
     * @param plan The new active plan.
     * @throws NullPointerException If the plan is null.
     */
    public void set(MealPlan plan) {
        this.plan = Objects.requireNonNull(plan, "plan");
        groceryList = Optional.empty();
    }

    /**
     * Returns the active plan, or an empty Optional if no plan has been generated yet.
     */
    public Optional<MealPlan> get() {
        return Optional.ofNullable(plan);
    }

    /**
     * Stores a read-only snapshot after grocery generation succeeds.
     * Copying before assignment preserves the old list if validation fails.
     *
     * @param items The generated groceries in display order.
     * @throws NullPointerException If the list or any item is null.
     */
    public void setGroceryList(List<GroceryItem> items) {
        groceryList = Optional.of(List.copyOf(items));
    }

    /**
     * Returns the generated snapshot, distinguishing an empty list from no generated list.
     *
     * @return The optional read-only grocery list for the active plan.
     */
    public Optional<List<GroceryItem>> getGroceryList() {
        return groceryList;
    }
}
