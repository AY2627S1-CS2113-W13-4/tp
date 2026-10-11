package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Checks display of supplied grocery data, including absent and generated-empty lists.
 */
class ViewListCommandTest {
    private static final String HEADER = "================ Grocery List ================";
    private static final String FOOTER = "=============================================";

    private final RecipeBook recipeBook = new RecipeBook();
    private final GroceryItem rice = new GroceryItem("Rice", new BigDecimal("200"), "g");

    @Test
    void execute_generatedList_displaysSuppliedItemsWithoutChangingRecipes() {
        List<GroceryItem> groceries = List.of(rice, new GroceryItem("Beans", new BigDecimal("50"), "g"));
        String expected = String.join(System.lineSeparator(), HEADER,
                "[ ] Rice: 200g", "[ ] Beans: 50g", FOOTER, "Total items to purchase: 2");

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
        String expected = String.join(System.lineSeparator(), HEADER, FOOTER, "Total items to purchase: 0");

        assertEquals(expected, new ViewListCommand(() -> Optional.of(List.of())).execute(recipeBook));
    }

    @Test
    void execute_replacedOrClearedList_readsLatestData() {
        AtomicReference<Optional<List<GroceryItem>>> currentList = new AtomicReference<>(Optional.of(List.of(rice)));
        ViewListCommand command = new ViewListCommand(currentList::get);
        command.execute(recipeBook);

        currentList.set(Optional.of(List.of(new GroceryItem("Beans", new BigDecimal("50"), "g"))));
        String expected = String.join(System.lineSeparator(), HEADER,
                "[ ] Beans: 50g", FOOTER, "Total items to purchase: 1");
        assertEquals(expected, command.execute(recipeBook));

        currentList.set(Optional.empty());
        assertEquals("No grocery list generated. Use 'generate-list' first.", command.execute(recipeBook));
    }

    @Test
    void constructor_nullSource_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewListCommand(null));
    }
}
