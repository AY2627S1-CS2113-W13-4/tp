package mishmash.command;

import java.util.Objects;
import java.util.logging.Logger;

import mishmash.model.ActiveMealPlan;
import mishmash.model.RecipeBook;

//@@author Rixte
/**
 * Clears the active meal plan and its derived state without accessing other session data.
 */
public class ResetPlanCommand implements Command {
    public static final String COMMAND_WORD = "reset-plan";
    public static final String USAGE = "Format: " + COMMAND_WORD;

    /** Records state transitions without including recipe or meal contents. */
    private static final Logger LOGGER = Logger.getLogger(ResetPlanCommand.class.getName());

    private final ActiveMealPlan activePlan;

    /**
     * Creates a reset that reads the latest plan state only when executed.
     *
     * @param activePlan The shared holder of planned meals, their generation request, and groceries.
     * @throws NullPointerException If the shared plan holder is missing.
     */
    public ResetPlanCommand(ActiveMealPlan activePlan) {
        this.activePlan = Objects.requireNonNull(activePlan, "activePlan");
    }

    /**
     * Clears plan state, including an empty generated grocery snapshot, and safely supports repeated resets.
     * Recipes, dietary preferences, and recorded meals are owned elsewhere and are not modified.
     *
     * @param recipeBook The shared command context; resetting does not read or change recipes.
     * @return A reset confirmation, or a message when no plan or groceries were present.
     */
    @Override
    public String execute(RecipeBook recipeBook) {
        boolean hadPlan = activePlan.get().isPresent();
        boolean hadGroceries = activePlan.getGroceryList().isPresent();
        activePlan.clear();
        assert activePlan.get().isEmpty() && activePlan.getRequest().isEmpty()
                && activePlan.getGroceryList().isEmpty() : "Reset must clear all plan state.";
        LOGGER.fine(() -> "Reset plan state; had plan: " + hadPlan + "; had groceries: " + hadGroceries);
        if (!hadPlan && !hadGroceries) {
            return "No active meal plan to reset.";
        }
        return "Meal plan reset. Recipes, dietary preferences, and recorded meals are unchanged.";
    }
}
//@@author
