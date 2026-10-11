package seedu.duke;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Displays an already generated grocery list without generating or storing groceries.
 */
public class ViewListCommand implements Command {
    /**
     * Reads the current list when the command executes; an empty Optional means no list has been generated.
     */
    private final Supplier<Optional<List<GroceryItem>>> groceryListSource;

    /**
     * Creates a viewing command connected to the caller's generated-list data.
     * A present but empty list is valid and displays zero items.
     *
     * @param groceryListSource A function returning the currently generated list, if available.
     * @throws NullPointerException If the source is null.
     */
    public ViewListCommand(Supplier<Optional<List<GroceryItem>>> groceryListSource) {
        this.groceryListSource = Objects.requireNonNull(groceryListSource, "groceryListSource");
    }

    @Override
    public String execute(RecipeBook recipeBook) {
        Optional<List<GroceryItem>> groceries = groceryListSource.get();
        if (groceries.isEmpty()) {
            return "No grocery list generated. Use 'generate-list' first.";
        }
        return new GroceryListViewer().view(groceries.get());
    }
}
