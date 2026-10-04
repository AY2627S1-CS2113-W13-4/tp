package seedu.duke;

import java.util.List;
import java.util.Objects;

/**
 * Formats a generated grocery list for the {@code view-list} display without changing its items.
 */
public class GroceryListViewer {
    /**
     * Returns a grocery list with an unchecked marker for each item and a total item count.
     * Items keep the order supplied by the generator, including separate entries for different units.
     * Returning text lets the caller choose where to display it and makes the output easy to test.
     *
     * @param groceryList The items returned by {@link GroceryListGenerator#generate(List)}.
     * @return The formatted list, with a total of zero when the list is empty.
     * @throws NullPointerException If the list or any item is null.
     */
    public String view(List<GroceryItem> groceryList) {
        Objects.requireNonNull(groceryList, "groceryList");

        StringBuilder result = new StringBuilder("================ Grocery List ================");
        for (GroceryItem item : groceryList) {
            Objects.requireNonNull(item, "grocery item");
            result.append(System.lineSeparator())
                    .append("[ ] ").append(item);
        }

        result.append(System.lineSeparator())
                .append("=============================================")
                .append(System.lineSeparator())
                .append("Total items to purchase: ").append(groceryList.size());
        return result.toString();
    }
}
