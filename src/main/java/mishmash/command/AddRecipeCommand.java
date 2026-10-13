package mishmash.command;

import java.util.Objects;

import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.ui.RecipeViewer;

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

        return "New recipe added!" + System.lineSeparator() + new RecipeViewer().view(recipe);
    }
}
