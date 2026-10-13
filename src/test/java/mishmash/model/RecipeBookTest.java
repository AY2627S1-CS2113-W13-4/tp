package mishmash.model;

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

    @Test
    void remove_middleRecipe_returnsItAndShiftsLaterRecipesUp() {
        RecipeBook book = new RecipeBook();
        Recipe first = createRecipe("First");
        Recipe second = createRecipe("Second");
        Recipe third = createRecipe("Third");
        book.add(first);
        book.add(second);
        book.add(third);

        Recipe removed = book.remove(1);

        assertEquals(second, removed);
        assertEquals(List.of(first, third), book.getRecipes());
        assertEquals(2, book.size());
    }

    @Test
    void remove_firstThenLastRecipe_preservesOrderOfTheRest() {
        RecipeBook book = new RecipeBook();
        Recipe first = createRecipe("First");
        Recipe second = createRecipe("Second");
        Recipe third = createRecipe("Third");
        book.add(first);
        book.add(second);
        book.add(third);

        assertEquals(first, book.remove(0));
        assertEquals(List.of(second, third), book.getRecipes());

        assertEquals(third, book.remove(1));
        assertEquals(List.of(second), book.getRecipes());
    }

    @Test
    void remove_onlyRecipe_leavesEmptyBook() {
        RecipeBook book = new RecipeBook();
        Recipe only = createRecipe("Only");
        book.add(only);

        assertEquals(only, book.remove(0));

        assertEquals(0, book.size());
        assertEquals(0, book.getRecipes().size());
    }

    @Test
    void remove_duplicateNames_removesOnlyRecipeAtIndex() {
        RecipeBook book = new RecipeBook();
        Recipe firstRice = createRecipe("Rice");
        Recipe secondRice = createRecipe("Rice");
        book.add(firstRice);
        book.add(secondRice);

        Recipe removed = book.remove(1);

        // Recipe has no equals(), so these checks compare the exact objects, not just the names.
        assertEquals(secondRice, removed);
        assertEquals(List.of(firstRice), book.getRecipes());
    }

    @Test
    void remove_invalidIndex_throwsAndLeavesBookUnchanged() {
        RecipeBook book = new RecipeBook();
        Recipe first = createRecipe("First");
        Recipe second = createRecipe("Second");
        book.add(first);
        book.add(second);

        for (int index : new int[] {-1, 2, 3, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            assertThrows(IndexOutOfBoundsException.class, () -> book.remove(index));
        }

        assertEquals(List.of(first, second), book.getRecipes());
    }

    @Test
    void remove_emptyBook_throwsIndexOutOfBoundsException() {
        RecipeBook book = new RecipeBook();

        assertThrows(IndexOutOfBoundsException.class, () -> book.remove(0));

        assertEquals(0, book.size());
    }

    @Test
    void size_afterAddsAndRemoves_matchesStoredRecipes() {
        RecipeBook book = new RecipeBook();
        assertEquals(0, book.size());

        book.add(createRecipe("First"));
        book.add(createRecipe("Second"));
        assertEquals(2, book.size());

        book.remove(0);
        assertEquals(1, book.size());
        assertEquals(book.getRecipes().size(), book.size());
    }

    /**
     * Creates a minimal valid recipe with the given name.
     */
    private Recipe createRecipe(String name) {
        return new Recipe(name, 100, 1, 1, 1,
                List.of(new GroceryItem("Rice", new BigDecimal("100"), "g")));
    }
}

