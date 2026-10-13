package seedu.duke;

import java.math.BigDecimal;
import java.util.List;

/**
 * Builds recipes for tests that only care about a recipe's name and nutrition.
 */
final class TestRecipes {
    private TestRecipes() {
    }

    /**
     * Creates a recipe with one placeholder ingredient, which every recipe must have.
     */
    static Recipe recipe(String name, int calories, int protein, int carbs, int fats) {
        return new Recipe(name, calories, protein, carbs, fats,
                List.of(new GroceryItem("Ingredient", BigDecimal.ONE, "g")));
    }
}
