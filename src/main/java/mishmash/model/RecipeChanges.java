package mishmash.model;

import java.util.List;
import java.util.Objects;

/**
 * Holds validated, immutable edits to apply to the recipe found when a command executes.
 * Nullable scalar fields mean unchanged; an empty ingredient list means unchanged.
 *
 * @param name The replacement name, or null to keep the current name.
 * @param calories The replacement calories, or null to keep the current value.
 * @param protein The replacement protein, or null to keep the current value.
 * @param carbs The replacement carbs, or null to keep the current value.
 * @param fats The replacement fats, or null to keep the current value.
 * @param ingredients The entire replacement ingredient list, or an empty list to keep it.
 * @param foodInfo The replacement food declaration, or null to keep the current declaration.
 */
public record RecipeChanges(String name, Integer calories, Integer protein, Integer carbs, Integer fats,
        List<GroceryItem> ingredients, FoodInfo foodInfo) {
    /**
     * Validates supplied fields and copies ingredients before a command can retain them.
     *
     * @throws IllegalArgumentException If no edits are supplied or a supplied field is invalid.
     * @throws NullPointerException If ingredients or any ingredient is null.
     */
    public RecipeChanges {
        ingredients = List.copyOf(ingredients);
        if (name == null && calories == null && protein == null && carbs == null && fats == null
                && ingredients.isEmpty() && foodInfo == null) {
            throw new IllegalArgumentException("Provide at least one recipe field to edit.");
        }
        if (name != null) {
            name = name.trim();
            if (name.isBlank()) {
                throw new IllegalArgumentException("Recipe name must not be blank.");
            }
        }
        validateNutrition(calories);
        validateNutrition(protein);
        validateNutrition(carbs);
        validateNutrition(fats);
        for (GroceryItem ingredient : ingredients) {
            if (ingredient.getQuantity().signum() <= 0) {
                throw new IllegalArgumentException("Ingredient quantity must be greater than zero.");
            }
        }
    }

    /** Rejects negative nutrition while allowing an omitted value. */
    private static void validateNutrition(Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("Nutrition values must not be negative.");
        }
    }

    /**
     * Builds a replacement recipe without modifying the original or any existing meal-plan snapshot.
     *
     * @param original The recipe currently at the selected position.
     * @return A new recipe combining supplied edits with the original's omitted fields.
     * @throws NullPointerException If the original recipe is null.
     */
    public Recipe applyTo(Recipe original) {
        Objects.requireNonNull(original, "original");
        return new Recipe(name == null ? original.getName() : name,
                calories == null ? original.getCalories() : calories,
                protein == null ? original.getProtein() : protein,
                carbs == null ? original.getCarbs() : carbs,
                fats == null ? original.getFats() : fats,
                ingredients.isEmpty() ? original.getIngredients() : ingredients,
                foodInfo == null ? original.getFoodInfo() : foodInfo);
    }
}
