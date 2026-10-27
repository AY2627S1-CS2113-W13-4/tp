package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import mishmash.exception.MishMashException;
import mishmash.model.MealLog;
import mishmash.model.Recipe;
import mishmash.model.RecipeBook;
import mishmash.testutil.TestRecipes;

//@@author GauthamNandi
/**
 * Checks that view-meals displays the log as it is when run, without changing it.
 */
class ViewMealsCommandTest {
    private static final String HEADER = "================ Recorded Meals ================";
    private static final String DIVIDER = "------------------------------------------------";
    private static final String FOOTER = "================================================";

    private final Recipe chickenRice = TestRecipes.recipe("Chicken and Rice", 650, 50, 70, 15);
    private final Recipe oatmeal = TestRecipes.recipe("Protein Oatmeal", 450, 35, 60, 8);
    private final RecipeBook recipeBook = new RecipeBook();
    private final MealLog log = new MealLog();

    @Test
    void constructor_nullLog_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewMealsCommand(null));
    }

    @Test
    void execute_noMealsRecorded_returnsEmptyMessage() {
        assertEquals("No meals recorded yet.", new ViewMealsCommand(log).execute(recipeBook));
    }

    @Test
    void execute_recordedMeals_listsEachMealAndTotals() {
        log.record(chickenRice);
        log.record(oatmeal);
        String expected = String.join(System.lineSeparator(),
                HEADER,
                "Meal 1: Chicken and Rice (650 kcal | P: 50g, C: 70g, F: 15g)",
                "Meal 2: Protein Oatmeal (450 kcal | P: 35g, C: 60g, F: 8g)",
                DIVIDER,
                "Total: 1100 kcal | Protein: 85g | Carbs: 130g | Fats: 23g",
                FOOTER);

        assertEquals(expected, new ViewMealsCommand(log).execute(recipeBook));
    }

    @Test
    void execute_mealRecordedAfterCommandCreated_isIncluded() {
        ViewMealsCommand command = new ViewMealsCommand(log);
        assertEquals("No meals recorded yet.", command.execute(recipeBook));

        log.record(oatmeal);

        assertTrue(command.execute(recipeBook).contains("Meal 1: Protein Oatmeal"));
    }

    @Test
    void execute_repeatedViewing_leavesLogAndRecipesUnchanged() {
        recipeBook.add(chickenRice);
        log.record(chickenRice);
        ViewMealsCommand command = new ViewMealsCommand(log);

        String first = command.execute(recipeBook);
        String second = command.execute(recipeBook);

        assertEquals(first, second);
        assertEquals(List.of(chickenRice), log.getMeals());
        assertEquals(List.of(chickenRice), recipeBook.getRecipes());
    }

    @Test
    void execute_nullRecipeBook_isIgnoredBecauseViewingDoesNotReadRecipes() {
        log.record(oatmeal);

        assertTrue(new ViewMealsCommand(log).execute(null).contains("Meal 1: Protein Oatmeal"));
    }

    @Test
    void confirmationPrompt_viewingNeedsNoConfirmation() throws MishMashException {
        assertEquals(Optional.empty(), new ViewMealsCommand(log).confirmationPrompt(recipeBook));
    }

    @Test
    void usage_namesTheCommandWithoutArguments() {
        assertEquals("Format: view-meals", ViewMealsCommand.USAGE);
        assertEquals("view-meals", ViewMealsCommand.COMMAND_WORD);
    }
}
//@@author
