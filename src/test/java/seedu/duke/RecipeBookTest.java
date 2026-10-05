package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks recipe insertion and read-only access.
 */
class RecipeBookTest {
    @Test
    void add_validRecipes_preservesInsertionOrder() {
        RecipeBook book = new RecipeBook();
        Recipe rice = new Recipe("Rice", 200, 4, 45, 1,
                List.of(new GroceryItem("Rice", new BigDecimal("150"), "g")));
        Recipe water = new Recipe("Water", 0, 0, 0, 0,
                List.of(new GroceryItem("Water", new BigDecimal("250"), "ml")));

        book.add(rice);
        book.add(water);

        assertEquals(List.of(rice, water), book.getRecipes());
        assertThrows(UnsupportedOperationException.class,
                () -> book.getRecipes().clear());
    }

    @Test
    void add_nullRecipe_leavesBookUnchanged() {
        RecipeBook book = new RecipeBook();

        assertThrows(NullPointerException.class, () -> book.add(null));

        assertEquals(0, book.getRecipes().size());
    }
}