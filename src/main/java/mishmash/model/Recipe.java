package mishmash.model;

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

    //@@author peilingggg
    /**
     * Creates a recipe with non-negative nutrition and at least one ingredient.
     * Copies the ingredient list so callers cannot change the recipe afterwards.
     *
     * @throws IllegalArgumentException If a field violates a recipe constraint.
     * @throws NullPointerException If the name, ingredient list, or an item is null.
     */
    public Recipe(String name, int calories, int protein, int carbs, int fats,
            List<GroceryItem> ingredients) {
        this.name = validateName(name);
        validateNutrition(calories, protein, carbs, fats);
        validateIngredients(ingredients);
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.ingredients = List.copyOf(ingredients);
    }

    /**
     * Trims a recipe name and rejects missing or blank names.
     *
     * @param name The name supplied by the caller.
     * @return The non-blank name to store.
     * @throws NullPointerException If the name is null.
     * @throws IllegalArgumentException If the trimmed name is blank.
     */
    private static String validateName(String name) {
        String trimmedName = Objects.requireNonNull(name, "name").trim();
        if (trimmedName.isBlank()) {
            throw new IllegalArgumentException("Recipe name must not be blank.");
        }
        return trimmedName;
    }

    /**
     * Checks that all supplied nutrition values are non-negative.
     *
     * @throws IllegalArgumentException If any nutrition value is negative.
     */
    private static void validateNutrition(int calories, int protein, int carbs, int fats) {
        if (calories < 0 || protein < 0 || carbs < 0 || fats < 0) {
            throw new IllegalArgumentException("Nutrition values must not be negative.");
        }
    }

    /**
     * Checks that a recipe has ingredients and that every ingredient has a positive quantity.
     *
     * @param ingredients The ingredient list supplied by the caller.
     * @throws NullPointerException If the list or any ingredient is null.
     * @throws IllegalArgumentException If the list is empty or any quantity is not positive.
     */
    private static void validateIngredients(List<GroceryItem> ingredients) {
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
    }
    //@@author

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

