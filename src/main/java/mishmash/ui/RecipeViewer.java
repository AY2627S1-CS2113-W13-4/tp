package mishmash.ui;

import java.util.Objects;

import mishmash.model.GroceryItem;
import mishmash.model.Recipe;

/**
 * Formats a recipe consistently for addition confirmations and detailed recipe listings.
 */
public class RecipeViewer {
    /**
     * Returns the recipe's name, nutrition, and ingredients without changing the recipe.
     *
     * @param recipe The recipe to display.
     * @return The formatted details without a trailing line separator.
     * @throws NullPointerException If the recipe is null.
     */
    public String view(Recipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        StringBuilder result = new StringBuilder(recipe.getName());
        result.append(System.lineSeparator())
                .append(recipe.getCalories()).append(" kcal | Protein: ")
                .append(recipe.getProtein()).append("g | Carbs: ")
                .append(recipe.getCarbs()).append("g | Fats: ")
                .append(recipe.getFats()).append("g")
                .append(System.lineSeparator())
                .append("Ingredients:");
        for (GroceryItem ingredient : recipe.getIngredients()) {
            result.append(System.lineSeparator())
                    .append("- ").append(ingredient);
        }
        result.append(System.lineSeparator()).append("Food tags: ").append(recipe.getFoodInfo().describe());
        return result.toString();
    }
}
