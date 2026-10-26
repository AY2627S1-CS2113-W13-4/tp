package mishmash.model;

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
     * Removes the recipe at the given position; every later recipe moves up by one position.
     * Recipes are identified by position because several recipes may share the same name.
     *
     * @param index The zero-based position of the recipe to remove.
     * @return The recipe that was removed.
     * @throws IndexOutOfBoundsException If the index is negative or not less than {@link #size()}.
     */
    public Recipe remove(int index) {
        return recipes.remove(Objects.checkIndex(index, recipes.size()));
    }

    /**
     * Replaces one recipe without changing the book's size, order, or earlier snapshots.
     *
     * @param index The zero-based position to replace.
     * @param recipe The validated replacement recipe.
     * @throws IndexOutOfBoundsException If the position does not exist.
     * @throws NullPointerException If the replacement recipe is null.
     */
    public void replace(int index, Recipe recipe) {
        Objects.checkIndex(index, recipes.size());
        recipes.set(index, Objects.requireNonNull(recipe, "recipe"));
    }

    /**
     * Returns how many recipes are stored.
     */
    public int size() {
        return recipes.size();
    }

    /**
     * Returns a read-only snapshot of the stored recipes.
     */
    public List<Recipe> getRecipes() {
        return List.copyOf(recipes);
    }
}

