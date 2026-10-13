package mishmash.model;

import java.util.Objects;

import mishmash.logic.MealPlanGenerator;
import mishmash.ui.MealPlanViewer;

/**
 * The validated arguments of a {@code generate-plan} command.
 *
 * <p>Like {@link MealPlanViewer.MealSummary}, this is a {@code record}: Java generates the
 * constructor, the {@code days()} and {@code targets()} getters, and equals/hashCode/toString.</p>
 *
 * @param days Number of days to plan, from {@value MealPlanGenerator#MIN_DAYS}
 *         to {@value MealPlanGenerator#MAX_DAYS}.
 * @param targets Daily nutrition targets (possibly none).
 */
public record GeneratePlanRequest(int days, NutritionTargets targets) {
    /**
     * Creates a request with a supported number of days and non-null targets.
     *
     * @throws NullPointerException If the targets are null.
     * @throws IllegalArgumentException If the number of days is outside the supported range.
     */
    public GeneratePlanRequest {
        Objects.requireNonNull(targets, "targets");
        if (days < MealPlanGenerator.MIN_DAYS || days > MealPlanGenerator.MAX_DAYS) {
            throw new IllegalArgumentException("Days must be between " + MealPlanGenerator.MIN_DAYS
                    + " and " + MealPlanGenerator.MAX_DAYS + ".");
        }
    }
}
