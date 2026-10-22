package mishmash.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import mishmash.model.ActiveMealPlan;
import mishmash.model.DayPlan;
import mishmash.model.MealPlan;
import mishmash.model.NutritionTargets;
import mishmash.model.RecipeBook;

/**
 * Reports daily nutrition shortfalls in the active plan without changing meals or targets.
 * A shortfall is any amount below a supplied target, independently of generation's tolerance.
 */
public class CheckShortfallCommand implements Command {
    /** At least one of these daily targets must be supplied. */
    public static final String USAGE = "Format: check-shortfall [cal/DAILY_CALORIES] [p/DAILY_PROTEIN]"
            + " [c/DAILY_CARBS] [f/DAILY_FATS]";

    private static final Logger LOGGER = Logger.getLogger(CheckShortfallCommand.class.getName());

    /** Reads the latest plan when the command executes, rather than capturing an earlier plan. */
    private final ActiveMealPlan activePlan;
    private final NutritionTargets targets;

    /**
     * Creates a check against explicit daily targets, leaving omitted nutrients unchecked.
     *
     * @param activePlan The shared holder of the current meal plan.
     * @param targets The daily targets to compare with each day's planned totals.
     * @throws NullPointerException If either argument is null.
     * @throws IllegalArgumentException If no target is supplied.
     */
    public CheckShortfallCommand(ActiveMealPlan activePlan, NutritionTargets targets) {
        this.activePlan = Objects.requireNonNull(activePlan, "activePlan");
        this.targets = Objects.requireNonNull(targets, "targets");
        if (!targets.hasAnyTarget()) {
            throw new IllegalArgumentException("Supply at least one daily nutrition target.");
        }
    }

    /**
     * Compares every day separately so a surplus on one day cannot hide another day's shortfall.
     * Values at or above a target are not shortfalls; this command does not check excesses.
     *
     * @param recipeBook The shared command context; the check does not modify recipes.
     * @return Each day's shortfalls, or a prompt to generate a plan first.
     */
    @Override
    public String execute(RecipeBook recipeBook) {
        MealPlan plan = activePlan.get().orElse(null);
        if (plan == null || plan.getNumberOfDays() == 0) {
            return "No active meal plan. Use 'generate-plan' first.";
        }
        List<String> output = new ArrayList<>();
        output.add("Daily nutrition shortfalls (compared with the supplied targets):");
        for (int index = 0; index < plan.getNumberOfDays(); index++) {
            List<String> shortfalls = findShortfalls(plan.getDays().get(index));
            if (shortfalls.isEmpty()) {
                output.add("Day " + (index + 1) + ": No shortfalls for the supplied targets.");
            } else {
                output.add("Day " + (index + 1) + ":");
                output.addAll(shortfalls);
            }
        }
        LOGGER.fine(() -> "Checked nutrition shortfalls for " + plan.getNumberOfDays() + " days");
        return String.join(System.lineSeparator(), output);
    }

    /** Collects only the supplied nutrients whose totals are strictly below their daily targets. */
    private List<String> findShortfalls(DayPlan day) {
        List<String> shortfalls = new ArrayList<>();
        addShortfall(shortfalls, "Calories", "kcal", day.getTotalCalories(), targets.getCalories());
        addShortfall(shortfalls, "Protein", "g", day.getTotalProtein(), targets.getProtein());
        addShortfall(shortfalls, "Carbs", "g", day.getTotalCarbs(), targets.getCarbs());
        addShortfall(shortfalls, "Fats", "g", day.getTotalFats(), targets.getFats());
        return shortfalls;
    }

    /** Uses long totals so large recipe values cannot overflow and appear to be shortfalls. */
    private void addShortfall(List<String> shortfalls, String label, String unit, long actual, Integer target) {
        if (target != null && actual < target) {
            shortfalls.add("- " + label + ": " + actual + "/" + target + " " + unit
                    + " (short by " + (target - actual) + " " + unit + ").");
        }
    }
}
