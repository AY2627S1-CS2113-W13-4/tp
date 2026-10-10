package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks the view-list display, including empty lists and preservation of grocery data.
 */
class GroceryListViewerTest {
    private final GroceryListViewer viewer = new GroceryListViewer();

    @Test
    void view_sampleList_matchesSpecifiedOutput() {
        List<GroceryItem> groceries = List.of(
                new GroceryItem("Chicken Breast", new BigDecimal("400"), "g"),
                new GroceryItem("White Rice", new BigDecimal("300"), "g"),
                new GroceryItem("Olive Oil", new BigDecimal("20"), "ml"),
                new GroceryItem("Rolled Oats", new BigDecimal("100"), "g"),
                new GroceryItem("Whey Protein", new BigDecimal("60"), "g"),
                new GroceryItem("Salmon Fillet", new BigDecimal("200"), "g"),
                new GroceryItem("Mixed Greens", new BigDecimal("150"), "g"));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Chicken Breast: 400g",
                "[ ] White Rice: 300g",
                "[ ] Olive Oil: 20ml",
                "[ ] Rolled Oats: 100g",
                "[ ] Whey Protein: 60g",
                "[ ] Salmon Fillet: 200g",
                "[ ] Mixed Greens: 150g",
                "=============================================",
                "Total items to purchase: 7");

        assertEquals(expected, viewer.view(groceries));
    }

    @Test
    void view_emptyList_displaysZeroItems() {
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "=============================================",
                "Total items to purchase: 0");

        assertEquals(expected, viewer.view(List.of()));
    }

    @Test
    void view_singleItem_displaysPlainQuantityAndOneItem() {
        List<GroceryItem> groceries = List.of(new GroceryItem("Rice", new BigDecimal("1000.00"), "g"));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Rice: 1000g",
                "=============================================",
                "Total items to purchase: 1");

        assertEquals(expected, viewer.view(groceries));
    }

    @Test
    void view_smallDecimalQuantity_preservesDisplayedPrecision() {
        List<GroceryItem> groceries =
                List.of(new GroceryItem("Saffron", new BigDecimal("0.00000100"), "g"));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Saffron: 0.000001g",
                "=============================================",
                "Total items to purchase: 1");

        assertEquals(expected, viewer.view(groceries));
    }

    @Test
    void view_differentLists_displaysOnlyLatestInputAndCount() {
        viewer.view(List.of(new GroceryItem("Rice", new BigDecimal("200"), "g"),
                new GroceryItem("Oil", new BigDecimal("10"), "ml")));
        List<GroceryItem> replacementList =
                List.of(new GroceryItem("Beans", new BigDecimal("50"), "g"));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Beans: 50g",
                "=============================================",
                "Total items to purchase: 1");

        assertEquals(expected, viewer.view(replacementList));
        String emptyDisplay = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "=============================================",
                "Total items to purchase: 0");
        assertEquals(emptyDisplay, viewer.view(List.of()));
    }

    @Test
    void view_sameIngredientWithDifferentUnits_preservesEntriesAndOrder() {
        List<GroceryItem> groceries = List.of(
                new GroceryItem("Rice", new BigDecimal("1.50"), "cups"),
                new GroceryItem("Rice", new BigDecimal("200"), "g"),
                new GroceryItem("Beans", BigDecimal.ZERO, "g"));
        String expected = String.join(System.lineSeparator(),
                "================ Grocery List ================",
                "[ ] Rice: 1.5cups",
                "[ ] Rice: 200g",
                "[ ] Beans: 0g",
                "=============================================",
                "Total items to purchase: 3");

        assertEquals(expected, viewer.view(groceries));
    }

    @Test
    void view_repeatedCalls_doesNotChangeInput() {
        GroceryItem rice = new GroceryItem("Rice", new BigDecimal("200.00"), "g");
        GroceryItem beans = new GroceryItem("Beans", new BigDecimal("50"), "g");
        List<GroceryItem> groceries = new ArrayList<>(List.of(rice, beans));
        List<GroceryItem> originalGroceries = List.copyOf(groceries);

        String firstDisplay = viewer.view(groceries);

        assertEquals(firstDisplay, viewer.view(groceries));
        assertEquals(originalGroceries, groceries);
        assertEquals(new BigDecimal("200.00"), rice.getQuantity());
    }

    @Test
    void view_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> viewer.view(null));
    }

    @Test
    void view_nullItem_throwsNullPointerException() {
        // Arrays.asList permits null so the viewer's validation is exercised.
        List<GroceryItem> groceries = Arrays.asList(
                new GroceryItem("Rice", BigDecimal.ONE, "g"), null);

        assertThrows(NullPointerException.class, () -> viewer.view(groceries));
    }
}
