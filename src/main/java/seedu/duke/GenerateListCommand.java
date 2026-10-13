package seedu.duke;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates and stores groceries for every recipe occurrence in the active meal plan.
 */
public class GenerateListCommand implements Command {
    private static final Logger LOGGER = Logger.getLogger(GenerateListCommand.class.getName());
    private final Model model;

    /**
     * Creates a command using the shared session model.
     *
     * @param model The meal plan and grocery-list state.
     * @throws NullPointerException If the model is null.
     */
    public GenerateListCommand(Model model) {
        this.model = Objects.requireNonNull(model, "model");
    }

    /**
     * Combines all planned ingredients and replaces the stored list only after generation succeeds.
     *
     * @param recipeBook The recipe book used by the command loop; its recipes are left unchanged.
     * @return The generation confirmation.
     * @throws MishMashException If there is no active meal plan.
     */
    @Override
    public String execute(RecipeBook recipeBook) throws MishMashException {
        Objects.requireNonNull(recipeBook, "recipeBook");
        List<Recipe> recipes = model.getMealPlan();
        if (recipes.isEmpty()) {
            LOGGER.warning("Grocery generation rejected: no active meal plan.");
            throw new MishMashException("No active meal plan. Use 'generate-plan' first.");
        }

        List<GroceryItem> ingredients = new ArrayList<>();
        for (Recipe recipe : recipes) {
            ingredients.addAll(recipe.getIngredients());
        }

        // The generator keeps incompatible units separate; store only its completed result.
        List<GroceryItem> items = new GroceryListGenerator().generate(ingredients);
        model.setGroceryList(items);
        LOGGER.log(Level.INFO, "Grocery list generated: {0} meal occurrences, {1} items.",
                new Object[] {recipes.size(), items.size()});
        return "Grocery list generated. Use 'view-list' to view it.";
    }
}
