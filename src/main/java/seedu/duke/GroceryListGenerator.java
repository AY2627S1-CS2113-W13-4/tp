package seedu.duke;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Combines recipe ingredients into a grocery list for planned meals. */
public class GroceryListGenerator {
    /**
     * Combines quantities for ingredients with the same name and unit.
     * The input should contain the ingredients from every recipe in the active meal plan.
     *
     * @param ingredients ingredients required by the planned meals
     * @return combined grocery items in the order each ingredient first appears
     */
    public List<GroceryItem> generate(List<GroceryItem> ingredients) {
        Objects.requireNonNull(ingredients, "ingredients");
        Map<ItemKey, BigDecimal> combinedQuantities = new LinkedHashMap<>();
        for (GroceryItem item : ingredients) {
            Objects.requireNonNull(item, "ingredient");
            ItemKey key = new ItemKey(item.getName(), item.getUnit());
            combinedQuantities.merge(key, item.getQuantity(), BigDecimal::add);
        }

        List<GroceryItem> groceryList = new ArrayList<>();
        for (Map.Entry<ItemKey, BigDecimal> entry : combinedQuantities.entrySet()) {
            ItemKey key = entry.getKey();
            groceryList.add(new GroceryItem(key.name(), entry.getValue(), key.unit()));
        }
        return List.copyOf(groceryList);
    }

    /** Identifies an ingredient by its name and unit so incompatible units stay separate. */
    private record ItemKey(String name, String unit) {
    }
}
