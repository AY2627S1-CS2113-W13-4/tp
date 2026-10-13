package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks ingredient aggregation, ordering, quantity precision, and input handling.
 */
class GroceryListGeneratorTest {
    private final GroceryListGenerator generator = new GroceryListGenerator();

    @Test
    void generate_emptyList_returnsEmptyList() {
        assertEquals(List.of(), generator.generate(List.of()));
    }

    @Test
    void generate_singleIngredient_preservesNameQuantityAndUnit() {
        List<GroceryItem> groceries = generator.generate(List.of(
                new GroceryItem("Rice", new BigDecimal("200"), "g")));

        assertEquals(1, groceries.size());
        assertItem(groceries.get(0), "Rice", "200", "g");
    }

    @Test
    void generate_repeatedIngredients_combinesQuantitiesInFirstAppearanceOrder() {
        List<GroceryItem> ingredients = List.of(
                new GroceryItem("Rice", new BigDecimal("200"), "g"),
                new GroceryItem("Chicken", new BigDecimal("150"), "g"),
                new GroceryItem("Rice", new BigDecimal("100"), "g"),
                new GroceryItem("Oil", new BigDecimal("20"), "ml"),
                new GroceryItem("Chicken", new BigDecimal("250"), "g"),
                new GroceryItem("Rice", new BigDecimal("50"), "g"));

        List<GroceryItem> groceries = generator.generate(ingredients);

        assertEquals(3, groceries.size());
        assertItem(groceries.get(0), "Rice", "350", "g");
        assertItem(groceries.get(1), "Chicken", "400", "g");
        assertItem(groceries.get(2), "Oil", "20", "ml");
    }

    @Test
    void generate_sameNameWithDifferentUnits_keepsUnitsSeparate() {
        List<GroceryItem> ingredients = List.of(
                new GroceryItem("Rice", new BigDecimal("1.5"), "cups"),
                new GroceryItem("Rice", new BigDecimal("200"), "g"),
                new GroceryItem("Rice", new BigDecimal("0.5"), "cups"));

        List<GroceryItem> groceries = generator.generate(ingredients);

        assertEquals(2, groceries.size());
        assertItem(groceries.get(0), "Rice", "2", "cups");
        assertItem(groceries.get(1), "Rice", "200", "g");
    }

    @Test
    void generate_differentNamesWithSameUnit_keepsIngredientsSeparate() {
        List<GroceryItem> ingredients = List.of(
                new GroceryItem("Rice", new BigDecimal("200"), "g"),
                new GroceryItem("Beans", new BigDecimal("100"), "g"));

        List<GroceryItem> groceries = generator.generate(ingredients);

        assertEquals(2, groceries.size());
        assertItem(groceries.get(0), "Rice", "200", "g");
        assertItem(groceries.get(1), "Beans", "100", "g");
    }

    @Test
    void generate_decimalQuantities_addsWithoutRounding() {
        List<GroceryItem> ingredients = List.of(
                new GroceryItem("Oil", new BigDecimal("0.10"), "ml"),
                new GroceryItem("Oil", new BigDecimal("0.2"), "ml"),
                new GroceryItem("Oil", new BigDecimal("0.003"), "ml"));

        List<GroceryItem> groceries = generator.generate(ingredients);

        assertEquals(1, groceries.size());
        assertItem(groceries.get(0), "Oil", "0.303", "ml");
    }

    @Test
    void generate_zeroQuantities_preservesZeroAndCombinesWithPositiveQuantities() {
        List<GroceryItem> ingredients = List.of(
                new GroceryItem("Beans", BigDecimal.ZERO, "g"),
                new GroceryItem("Rice", BigDecimal.ZERO, "g"),
                new GroceryItem("Beans", BigDecimal.ZERO, "g"),
                new GroceryItem("Rice", new BigDecimal("100"), "g"));

        List<GroceryItem> groceries = generator.generate(ingredients);

        assertEquals(2, groceries.size());
        assertItem(groceries.get(0), "Beans", "0", "g");
        assertItem(groceries.get(1), "Rice", "100", "g");
    }

    @Test
    void generate_repeatedCalls_doesNotChangeInputOrAccumulatePreviousResults() {
        GroceryItem rice = new GroceryItem("Rice", new BigDecimal("200"), "g");
        List<GroceryItem> ingredients = new ArrayList<>(List.of(rice, rice));
        List<GroceryItem> originalIngredients = List.copyOf(ingredients);

        List<GroceryItem> firstGroceries = generator.generate(ingredients);
        generator.generate(List.of(new GroceryItem("Beans", BigDecimal.ONE, "g")));
        List<GroceryItem> secondGroceries = generator.generate(ingredients);

        assertEquals(originalIngredients, ingredients);
        assertItem(rice, "Rice", "200", "g");
        assertEquals(1, firstGroceries.size());
        assertEquals(1, secondGroceries.size());
        assertItem(firstGroceries.get(0), "Rice", "400", "g");
        assertItem(secondGroceries.get(0), "Rice", "400", "g");
    }

    @Test
    void generate_mutableInput_returnsIndependentUnmodifiableList() {
        List<GroceryItem> ingredients = new ArrayList<>(List.of(
                new GroceryItem("Rice", BigDecimal.ONE, "g")));

        List<GroceryItem> groceries = generator.generate(ingredients);
        ingredients.clear();

        assertEquals(1, groceries.size());
        assertItem(groceries.get(0), "Rice", "1", "g");
        assertThrows(UnsupportedOperationException.class,
                () -> groceries.add(new GroceryItem("Beans", BigDecimal.ONE, "g")));
        assertThrows(UnsupportedOperationException.class, () -> groceries.remove(0));
    }

    @Test
    void generate_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> generator.generate(null));
    }

    @Test
    void generate_nullIngredient_throwsNullPointerException() {
        // Arrays.asList permits null so the generator's validation is exercised.
        List<GroceryItem> ingredients = Arrays.asList(
                new GroceryItem("Rice", BigDecimal.ONE, "g"), null);

        assertThrows(NullPointerException.class, () -> generator.generate(ingredients));
    }

    /**
     * Checks all grocery item fields, comparing quantities by value regardless of decimal scale.
     *
     * @param item generated grocery item to check
     * @param expectedName expected ingredient name
     * @param expectedQuantity expected quantity expressed as a decimal string
     * @param expectedUnit expected quantity unit
     */
    private void assertItem(GroceryItem item, String expectedName, String expectedQuantity, String expectedUnit) {
        assertEquals(expectedName, item.getName());
        assertEquals(0, new BigDecimal(expectedQuantity).compareTo(item.getQuantity()),
                "Quantity should equal " + expectedQuantity);
        assertEquals(expectedUnit, item.getUnit());
    }
}
