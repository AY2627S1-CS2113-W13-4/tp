package mishmash.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import mishmash.model.GroceryItem;
import mishmash.model.RecipeBook;
import mishmash.testutil.TestRecipes;

/**
 * Checks display of supplied grocery data, including absent and generated-empty lists.
 */
class ViewListCommandTest {
    private static final String GROCERY_LIST_HEADER = "================ Grocery List ================";
    private static final String GROCERY_LIST_FOOTER = "=============================================";

    private final RecipeBook recipeBook = new RecipeBook();
    private final GroceryItem rice = new GroceryItem("Rice", new BigDecimal("200"), "g");

    @Test
    void execute_nonEmptyBook_preservesRecipesAcrossAllGroceryStates() {
        recipeBook.add(TestRecipes.recipe("Stored recipe", 300, 20, 40, 5));
        recipeBook.add(TestRecipes.recipe("Another stored recipe", 200, 10, 30, 4));
        var originalRecipes = recipeBook.getRecipes();
        for (ViewListCommand command : List.of(new ViewListCommand(Optional::empty),
                new ViewListCommand(() -> Optional.of(List.of())),
                new ViewListCommand(() -> Optional.of(List.of(rice))))) {
            command.execute(recipeBook);

            assertEquals(originalRecipes, recipeBook.getRecipes());
            assertEquals(2, recipeBook.size());
        }
    }

    @Test
    void execute_generatedList_displaysSuppliedItemsWithoutChangingRecipes() {
        List<GroceryItem> groceries = List.of(rice, new GroceryItem("Beans", new BigDecimal("50"), "g"));
        String expected = String.join(System.lineSeparator(), GROCERY_LIST_HEADER,
                "[ ] Rice: 200g", "[ ] Beans: 50g", GROCERY_LIST_FOOTER, "Total items to purchase: 2");

        assertEquals(expected, new ViewListCommand(() -> Optional.of(groceries)).execute(recipeBook));
        assertTrue(recipeBook.getRecipes().isEmpty());
    }

    @Test
    void execute_noGeneratedList_displaysGeneratePrompt() {
        assertEquals("No grocery list generated. Use 'generate-list' first.",
                new ViewListCommand(() -> Optional.empty()).execute(recipeBook));
    }

    @Test
    void execute_generatedEmptyList_displaysZeroItems() {
        String expected = String.join(System.lineSeparator(),
                GROCERY_LIST_HEADER, GROCERY_LIST_FOOTER, "Total items to purchase: 0");

        assertEquals(expected, new ViewListCommand(() -> Optional.of(List.of())).execute(recipeBook));
    }

    @Test
    void execute_replacedOrClearedList_readsLatestData() {
        AtomicReference<Optional<List<GroceryItem>>> currentGroceries =
                new AtomicReference<>(Optional.of(List.of(rice)));
        ViewListCommand command = new ViewListCommand(currentGroceries::get);
        command.execute(recipeBook);

        currentGroceries.set(Optional.of(List.of(new GroceryItem("Beans", new BigDecimal("50"), "g"))));
        String expected = String.join(System.lineSeparator(), GROCERY_LIST_HEADER,
                "[ ] Beans: 50g", GROCERY_LIST_FOOTER, "Total items to purchase: 1");
        assertEquals(expected, command.execute(recipeBook));

        currentGroceries.set(Optional.empty());
        assertEquals("No grocery list generated. Use 'generate-list' first.", command.execute(recipeBook));
    }

    @Test
    void execute_nullOptionalFromSource_reportsBrokenSourceAndCanReadReplacement() {
        AtomicReference<Optional<List<GroceryItem>>> currentGroceries = new AtomicReference<>();
        ViewListCommand command = new ViewListCommand(currentGroceries::get);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> command.execute(recipeBook));
        assertEquals("Grocery list source returned null; use Optional.empty() for no list.",
                exception.getMessage());
        assertTrue(recipeBook.getRecipes().isEmpty());

        currentGroceries.set(Optional.of(List.of(rice)));
        String expected = String.join(System.lineSeparator(), GROCERY_LIST_HEADER,
                "[ ] Rice: 200g", GROCERY_LIST_FOOTER, "Total items to purchase: 1");
        assertEquals(expected, command.execute(recipeBook));
    }

    @Test
    void constructor_nullSource_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewListCommand(null));
    }
}
