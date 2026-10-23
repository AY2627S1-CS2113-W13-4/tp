package mishmash.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mishmash.model.DayPlan;
import mishmash.model.GroceryItem;
import mishmash.model.MealPlan;
import mishmash.model.Recipe;

/**
 * Combines recipe ingredients into a grocery list for planned meals.
 */
public class GroceryListGenerator {
    /**
     * Generates groceries by counting every meal occurrence in the supplied plan.
     *
     * <p>Recipes repeated across days contribute their ingredients each time. Existing aggregation
     * combines exact name/unit matches and preserves separate entries for incompatible units.
     *
     * @param plan The plan whose ingredients should be collected.
     * @return An unmodifiable grocery list in first-appearance order.
     * @throws NullPointerException If the plan is null.
     */
    public List<GroceryItem> generateForPlan(MealPlan plan) {
        Objects.requireNonNull(plan, "plan");
        List<GroceryItem> ingredients = new ArrayList<>();
        for (DayPlan day : plan.getDays()) {
            for (Recipe recipe : day.getMeals()) {
                ingredients.addAll(recipe.getIngredients());
            }
        }
        return generate(ingredients);
    }

    /**
     * Combines quantities for ingredients with the same name and unit.
     * The input should contain the ingredients from every recipe in the active meal plan.
     * Names and units are matched exactly, and the input list and its items are left unchanged.
     *
     * @param ingredients ingredients required by the planned meals
     * @return an unmodifiable list of combined grocery items in the order each ingredient first appears
     * @throws NullPointerException if the input list or any ingredient is null
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

    /**
     * Identifies an ingredient by its name and unit so incompatible units stay separate.
     */
    private record ItemKey(String name, String unit) {
    }
}
