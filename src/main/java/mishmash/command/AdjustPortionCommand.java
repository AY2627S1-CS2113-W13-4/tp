package mishmash.command;

import java.util.Objects;
import java.util.logging.Logger;

import mishmash.exception.MishMashException;
import mishmash.model.ActiveMealPlan;
import mishmash.model.DayPlan;
import mishmash.model.MealPlan;
import mishmash.model.PortionAdjustment;
import mishmash.model.RecipeBook;

/**
 * Changes the whole-recipe portion count of one meal in the active plan.
 */
public final class AdjustPortionCommand implements Command {
    /** Command word shared by parsing and user guidance. */
    public static final String COMMAND_WORD = "adjust-portion";

    private static final Logger LOGGER = Logger.getLogger(AdjustPortionCommand.class.getName());

    private final PortionAdjustment adjustment;
    private final ActiveMealPlan activeMealPlan;

    /**
     * Creates a command that resolves its one-based positions against the plan during execution.
     *
     * @param adjustment The validated positions and portion count.
     * @param activeMealPlan The shared active plan to read and update.
     * @throws NullPointerException If either argument is null.
     */
    public AdjustPortionCommand(PortionAdjustment adjustment, ActiveMealPlan activeMealPlan) {
        this.adjustment = Objects.requireNonNull(adjustment, "adjustment");
        this.activeMealPlan = Objects.requireNonNull(activeMealPlan, "activeMealPlan");
    }

    /**
     * Updates one planned meal atomically and clears groceries only when its portion count changes.
     *
     * @param recipeBook The shared command context; recipes are not read or changed.
     * @return A summary of the updated meal or a prompt to generate a plan first.
     * @throws MishMashException If the selected day or meal does not exist, or totals would overflow.
     */
    @Override
    public String execute(RecipeBook recipeBook) throws MishMashException {
        Objects.requireNonNull(recipeBook, "recipeBook");
        MealPlan currentPlan = activeMealPlan.get().orElse(null);
        if (currentPlan == null || currentPlan.getNumberOfDays() == 0) {
            LOGGER.fine("Portion adjustment skipped: no active meal plan");
            return "No active meal plan. Use 'generate-plan' first.";
        }

        validateDayNumber(currentPlan);
        DayPlan selectedDay = currentPlan.getDays().get(adjustment.dayNumber() - 1);
        validateMealNumber(selectedDay);
        int mealIndex = adjustment.mealNumber() - 1;
        int currentPortionCount = selectedDay.getPlannedMeals().get(mealIndex).getPortionCount();
        if (currentPortionCount == adjustment.portionCount()) {
            return "Meal " + adjustment.mealNumber() + " on Day " + adjustment.dayNumber()
                    + " already has " + formatPortions(adjustment.portionCount()) + ". Nothing was changed.";
        }

        MealPlan updatedPlan;
        try {
            updatedPlan = currentPlan.withAdjustedPortion(
                    adjustment.dayNumber() - 1, mealIndex, adjustment.portionCount());
        } catch (ArithmeticException e) {
            throw new MishMashException("That portion count makes the day's nutrition totals too large.", e);
        }

        boolean hadGroceryList = activeMealPlan.getGroceryList().isPresent();
        activeMealPlan.update(updatedPlan);
        LOGGER.fine(() -> "Adjusted Day " + adjustment.dayNumber() + ", Meal "
                + adjustment.mealNumber() + " to " + adjustment.portionCount() + " portions");
        String result = "Meal " + adjustment.mealNumber() + " on Day " + adjustment.dayNumber()
                + " updated to " + formatPortions(adjustment.portionCount()) + ".";
        if (hadGroceryList) {
            return result + System.lineSeparator()
                    + "The previous grocery list was cleared. Run 'generate-list' to update it.";
        }
        return result + System.lineSeparator() + "Run 'generate-list' to create groceries for the updated plan.";
    }

    /**
     * Rejects a day position outside the plan without exposing zero-based model positions.
     */
    private void validateDayNumber(MealPlan plan) throws MishMashException {
        if (adjustment.dayNumber() > plan.getNumberOfDays()) {
            throw new MishMashException("Day number must be between 1 and " + plan.getNumberOfDays() + ".");
        }
    }

    /**
     * Rejects a meal position outside the selected day.
     */
    private void validateMealNumber(DayPlan day) throws MishMashException {
        int mealCount = day.getPlannedMeals().size();
        if (mealCount == 0) {
            throw new MishMashException("Day " + adjustment.dayNumber() + " has no meals to adjust.");
        }
        if (adjustment.mealNumber() > mealCount) {
            throw new MishMashException("Meal number for Day " + adjustment.dayNumber()
                    + " must be between 1 and " + mealCount + ".");
        }
    }

    /**
     * Formats singular and plural portion labels consistently.
     */
    private String formatPortions(int portionCount) {
        return portionCount + (portionCount == 1 ? " portion" : " portions");
    }
}
