package seedu.duke;

import java.util.List;
import java.util.Objects;

/**
 * Holds a recipe's nutrition and ingredients independently of its display.
 */
public class Recipe {
    private final String name;
    private final int calories;
    private final int protein;
    private final int carbs;
    private final int fats;
    private final List<GroceryItem> ingredients;

    /**
     * Creates a recipe with non-negative nutrition and at least one ingredient.
     * Copies the ingredient list so callers cannot change the recipe afterwards.
     *
     * @throws IllegalArgumentException if a field violates a recipe constraint
     * @throws NullPointerException if the name, ingredient list, or an item is null
     */
    public Recipe(String name, int calories, int protein, int carbs, int fats,
            List<GroceryItem> ingredients) {
        this.name = Objects.requireNonNull(name, "name").trim();
        if (this.name.isBlank()) {
            throw new IllegalArgumentException("Recipe name must not be blank.");
        }
        if (calories < 0 || protein < 0 || carbs < 0 || fats < 0) {
            throw new IllegalArgumentException("Nutrition values must not be negative.");
        }

        Objects.requireNonNull(ingredients, "ingredients");
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("At least one ingredient is required.");
        }
        for (GroceryItem ingredient : ingredients) {
            Objects.requireNonNull(ingredient, "ingredient");
            if (ingredient.getQuantity().signum() <= 0) {
                throw new IllegalArgumentException("Ingredient quantity must be greater than zero.");
            }
        }

        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.ingredients = List.copyOf(ingredients);
    }

    /**
     * Returns the recipe name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the recipe's calories in kilocalories.
     */
    public int getCalories() {
        return calories;
    }

    /**
     * Returns the recipe's protein in grams.
     */
    public int getProtein() {
        return protein;
    }

    /**
     * Returns the recipe's carbs in grams.
     */
    public int getCarbs() {
        return carbs;
    }

    /**
     * Returns the recipe's fats in grams.
     */
    public int getFats() {
        return fats;
    }

    /**
     * Returns the recipe's read-only ingredient list.
     *
     * @return The ingredients in their original order.
     */
    public List<GroceryItem> getIngredients() {
        return ingredients;
    }
}


