package seedu.duke;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores recipes in the order they were added.
 */
public class RecipeBook {
    private final List<Recipe> recipes = new ArrayList<>();

    /**
     * Adds a recipe without changing existing recipes.
     */
    public void add(Recipe recipe) {
        recipes.add(Objects.requireNonNull(recipe, "recipe"));
    }

    /**
     * Returns a read-only snapshot of the stored recipes.
     */
    public List<Recipe> getRecipes() {
        return List.copyOf(recipes);
    }
}