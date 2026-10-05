package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks recipe addition and its confirmation output.
 */
class AddRecipeCommandTest {
    /**
     * Creates a recipe with three ingredients for command tests.
     */
    private Recipe createRecipe() {
        return new Recipe("Chicken and Rice", 650, 50, 70, 15, List.of(
                new GroceryItem("Chicken Breast", new BigDecimal("200"), "g"),
                new GroceryItem("White Rice", new BigDecimal("150"), "g"),
                new GroceryItem("Olive Oil", new BigDecimal("10"), "ml")));
    }

    @Test
    void execute_validRecipe_addsRecipeAndReturnsConfirmation() {
        Recipe recipe = createRecipe();
        RecipeBook book = new RecipeBook();

        String result = new AddRecipeCommand(recipe).execute(book);

        String expected = String.join(System.lineSeparator(),
                "New recipe added!",
                "Chicken and Rice",
                "650 kcal | Protein: 50g | Carbs: 70g | Fats: 15g",
                "Ingredients:",
                "- Chicken Breast: 200g",
                "- White Rice: 150g",
                "- Olive Oil: 10ml");

        assertEquals(List.of(recipe), book.getRecipes());
        assertEquals(expected, result);
    }

    @Test
    void execute_existingRecipe_preservesExistingEntry() {
        Recipe existing = createRecipe();
        Recipe added = createRecipe();
        RecipeBook book = new RecipeBook();
        book.add(existing);

        new AddRecipeCommand(added).execute(book);

        assertEquals(List.of(existing, added), book.getRecipes());
    }

    @Test
    void constructor_nullRecipe_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddRecipeCommand(null));
    }

    @Test
    void execute_nullBook_throwsNullPointerException() {
        AddRecipeCommand command = new AddRecipeCommand(createRecipe());

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
