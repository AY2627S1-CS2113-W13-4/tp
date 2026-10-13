package seedu.duke;

/**
 * Represents an action that operates on application data.
 */
public interface Command {
    /**
     * Executes the action and returns feedback for display.
     *
     * @param recipeBook The recipe book to update.
     * @return The feedback to display to the user.
     * @throws MishMashException If the required application data is unavailable.
     */
    String execute(RecipeBook recipeBook) throws MishMashException;
}
