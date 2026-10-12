package seedu.duke;

/**
 * Represents an application action, such as adding a recipe or viewing generated data.
 */
public interface Command {
    /**
     * Executes the action and returns feedback for display.
     *
     * @param recipeBook The recipe book available to the command.
     * @return The feedback to display to the user.
     * @throws MishMashException If the request cannot be executed with the current application data.
     */
    String execute(RecipeBook recipeBook) throws MishMashException;
}
