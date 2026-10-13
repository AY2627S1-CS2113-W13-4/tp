package seedu.mishmash.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Holds the currently active meal plan in memory, so later features such as
 * {@code view-plan} and the grocery list can read it.
 *
 * <p>Only one plan is active at a time; generating a new plan replaces the old one.
 * The plan is not saved to disk (persistence is out of scope for V1).</p>
 *
 * <p>This is a plain object rather than a static singleton so that the app can
 * create one instance and share it, and tests can create a fresh one each time.</p>
 */
public class ActiveMealPlan {
    private MealPlan plan;

    /**
     * Makes the given plan the active one, replacing any previous plan.
     *
     * @param plan The new active plan.
     * @throws NullPointerException If the plan is null.
     */
    public void set(MealPlan plan) {
        this.plan = Objects.requireNonNull(plan, "plan");
    }

    /** Returns the active plan, or an empty Optional if no plan has been generated yet. */
    public Optional<MealPlan> get() {
        return Optional.ofNullable(plan);
    }
}
