package seedu.duke;

/**
 * Represents an action that operates on the recipe book.
 */
public interface Command {
    /**
     * Executes the action and returns feedback for display.
     *
     * @param recipeBook The recipe book to update.
     * @return The feedback to display to the user.
     */
    String execute(RecipeBook recipeBook);
}
