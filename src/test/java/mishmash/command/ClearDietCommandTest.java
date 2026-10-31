package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import mishmash.model.Diet;
import mishmash.model.DietaryPreferences;
import mishmash.model.DietarySettings;
import mishmash.model.FoodTag;
import mishmash.model.RecipeBook;

/**
 * Checks complete, idempotent dietary clearing through the command boundary.
 */
class ClearDietCommandTest {
    private final DietarySettings settings = new DietarySettings();
    private final RecipeBook recipeBook = new RecipeBook();

    @Test
    void execute_restrictedProfile_clearsEveryRestriction() {
        settings.set(new DietaryPreferences(Diet.VEGAN, Set.of(FoodTag.SOY), Set.of("Peanut")));

        String result = new ClearDietCommand(settings).execute(recipeBook);

        assertEquals("Dietary preferences and restrictions cleared." + System.lineSeparator()
                + "Recipes, meal plans, groceries, targets, and recorded meals are unchanged.", result);
        assertFalse(settings.get().isRestricted());
    }

    @Test
    void execute_alreadyClear_isIdempotent() {
        ClearDietCommand command = new ClearDietCommand(settings);

        assertEquals("Dietary preferences are already clear. Nothing was changed.",
                command.execute(recipeBook));
        assertFalse(settings.get().isRestricted());
    }

    @Test
    void constructorAndExecution_nullValues_rejectWithoutChangingSettings() {
        settings.set(new DietaryPreferences(Diet.VEGETARIAN, Set.of(), Set.of()));

        assertThrows(NullPointerException.class, () -> new ClearDietCommand(null));
        assertThrows(NullPointerException.class, () -> new ClearDietCommand(settings).execute(null));
        assertTrue(settings.get().isRestricted());
    }
}
