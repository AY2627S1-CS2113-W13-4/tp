package seedu.duke;

import java.util.Optional;

/**
 * Represents an action that operates on the recipe book.
 */
public interface Command {
    /**
     * Returns a question the user must answer before this action runs, for actions that cannot be undone.
     * Most commands need no confirmation, so the default is to ask nothing. The caller shows the question,
     * reads the answer, and calls {@link #execute(RecipeBook)} only if the user agrees.
     *
     * @param recipeBook The recipe book the action would change; it is not modified.
     * @return The question to ask, or empty if the action can run immediately.
     * @throws MishMashException If the action could not be carried out, so there is nothing to confirm.
     */
    default Optional<String> confirmationPrompt(RecipeBook recipeBook) throws MishMashException {
        return Optional.empty();
    }

    /**
     * Executes the action and returns feedback for display.
     *
     * @param recipeBook The recipe book to update.
     * @return The feedback to display to the user.
     * @throws MishMashException If the action cannot be carried out on the current recipe book,
     *      for example when a recipe to delete does not exist.
     */
    String execute(RecipeBook recipeBook) throws MishMashException;
}
