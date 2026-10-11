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
     * Describes the command's syntax without arguments.
     */
    public static final String USAGE = "Format: view-list";

    private static final String NO_LIST_MESSAGE = "No grocery list generated. Use 'generate-list' first.";

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

    /**
     * Displays the latest supplied groceries, including a present list with zero items.
     *
     * @param recipeBook The shared command context; viewing does not read or modify recipes.
     * @return The formatted list or a prompt when no list has been generated.
     */
    @Override
    public String execute(RecipeBook recipeBook) {
        Optional<List<GroceryItem>> groceries = groceryListSource.get();
        if (groceries.isEmpty()) {
            return NO_LIST_MESSAGE;
        }
        return new GroceryListViewer().view(groceries.get());
    }
}
