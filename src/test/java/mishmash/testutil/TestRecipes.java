package mishmash.testutil;

import java.math.BigDecimal;
import java.util.List;

import mishmash.model.GroceryItem;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;

/**
 * Builds recipes for tests that only care about a recipe's name and nutrition.
 */
public final class TestRecipes {
    private TestRecipes() {
    }

    //@@author GauthamNandi
    /**
     * Creates a recipe with one placeholder ingredient, which every recipe must have.
     */
    public static Recipe recipe(String name, int calories, int protein, int carbs, int fats) {
        return new Recipe(name, calories, protein, carbs, fats,
                List.of(new GroceryItem("Ingredient", BigDecimal.ONE, "g")));
    }
    //@@author

    /**
     * Returns three distinct recipes with identical nutrition and one gram of the same placeholder ingredient.
     * Predictable totals keep generation and viewing integration tests independent of recipe order.
     */
    public static RecipeBook recipeBook(int calories, int protein, int carbs, int fats) {
        RecipeBook book = new RecipeBook();
        for (String name : List.of("A", "B", "C")) {
            book.add(recipe(name, calories, protein, carbs, fats));
        }
        return book;
    }
}
