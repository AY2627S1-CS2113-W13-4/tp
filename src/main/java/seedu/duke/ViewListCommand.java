package seedu.duke;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays the stored grocery list without regenerating or changing it.
 */
public class ViewListCommand implements Command {
    private static final Logger LOGGER = Logger.getLogger(ViewListCommand.class.getName());
    private final Model model;

    /**
     * Creates a command using the shared session model.
     *
     * @param model The meal plan and grocery-list state.
     * @throws NullPointerException If the model is null.
     */
    public ViewListCommand(Model model) {
        this.model = Objects.requireNonNull(model, "model");
    }

    /**
     * Formats the stored grocery snapshot without changing it.
     *
     * @param recipeBook The recipe book used by the command loop; its recipes are left unchanged.
     * @return The grocery list display.
     * @throws MishMashException If a grocery list has not been generated for the current plan.
     */
    @Override
    public String execute(RecipeBook recipeBook) throws MishMashException {
        Objects.requireNonNull(recipeBook, "recipeBook");
        Optional<List<GroceryItem>> groceries = model.getGroceryList();
        if (groceries.isEmpty()) {
            LOGGER.warning("Grocery viewing rejected: no generated list.");
            throw new MishMashException("No grocery list generated. Use 'generate-list' first.");
        }

        String display = new GroceryListViewer().view(groceries.get());
        LOGGER.log(Level.FINE, "Grocery list viewed: {0} items.", groceries.get().size());
        return display;
    }
}
