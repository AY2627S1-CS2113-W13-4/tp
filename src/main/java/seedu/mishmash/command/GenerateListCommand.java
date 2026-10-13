package seedu.mishmash.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.mishmash.logic.GroceryListGenerator;
import seedu.mishmash.model.ActiveMealPlan;
import seedu.mishmash.model.DayPlan;
import seedu.mishmash.model.GroceryItem;
import seedu.mishmash.model.MealPlan;
import seedu.mishmash.model.Recipe;
import seedu.mishmash.model.RecipeBook;

/**
 * Combines ingredients from the active meal plan and stores its completed grocery list.
 */
public class GenerateListCommand implements Command {
    /**
     * Describes the command's syntax without arguments.
     */
    public static final String USAGE = "Format: generate-list";

    private static final Logger LOGGER = Logger.getLogger(GenerateListCommand.class.getName());
    private final ActiveMealPlan activeMealPlan;

    /**
     * Creates a generation command connected to the shared active plan.
     *
     * @param activeMealPlan The source of meals and destination for generated groceries.
     * @throws NullPointerException If the active plan holder is null.
     */
    public GenerateListCommand(ActiveMealPlan activeMealPlan) {
        this.activeMealPlan = Objects.requireNonNull(activeMealPlan, "activeMealPlan");
    }

    /**
     * Generates groceries for the latest plan, counting every occurrence of a recipe.
     * A failed generation leaves the previously stored grocery list intact.
     *
     * @param recipeBook The shared command context; generation does not read or change the recipe book.
     * @return A confirmation, or a prompt to generate a meal plan first.
     */
    @Override
    public String execute(RecipeBook recipeBook) {
        Optional<MealPlan> plan = activeMealPlan.get();
        if (plan.isEmpty()) {
            LOGGER.warning("Grocery generation rejected: no active meal plan");
            return "No active meal plan. Use 'generate-plan' first.";
        }

        List<GroceryItem> ingredients = new ArrayList<>();
        for (DayPlan day : plan.get().getDays()) {
            for (Recipe meal : day.getMeals()) {
                ingredients.addAll(meal.getIngredients());
            }
        }

        // The generator combines exact name/unit matches without converting incompatible units.
        List<GroceryItem> groceries = new GroceryListGenerator().generate(ingredients);
        activeMealPlan.setGroceryList(groceries);
        LOGGER.info(() -> "Generated grocery list from " + ingredients.size()
                + " ingredient occurrences into " + groceries.size() + " items");
        return "Grocery list generated. Use 'view-list' to view it.";
    }
}
