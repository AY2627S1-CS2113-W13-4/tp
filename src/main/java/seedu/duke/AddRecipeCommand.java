package seedu.duke;

import java.util.Objects;

/**
 * Adds a validated recipe and returns its confirmation.
 */
public class AddRecipeCommand implements Command {
    private final Recipe recipe;

    /**
     * Creates a command that adds the supplied recipe.
     *
     * @param recipe The validated recipe to add.
     * @throws NullPointerException If the recipe is null.
     */
    public AddRecipeCommand(Recipe recipe) {
        this.recipe = Objects.requireNonNull(recipe, "recipe");
    }

    @Override
    public String execute(RecipeBook recipeBook) {
        Objects.requireNonNull(recipeBook, "recipeBook");
        recipeBook.add(recipe);

        StringBuilder result = new StringBuilder("New recipe added!");
        result.append(System.lineSeparator())
                .append(recipe.getName())
                .append(System.lineSeparator())
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

        return result.toString();
    }
}
