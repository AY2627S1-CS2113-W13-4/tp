package seedu.duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Ensures stricter validation still accepts supported boundaries and formatting.
 */
class AddRecipeBoundaryTest {
    @TestFactory
    Stream<DynamicTest> parseCommand_nutritionBoundaries_acceptsValues() {
        List<DynamicTest> tests = new ArrayList<>();
        String[] fields = {"cal/200", "p/4", "c/45", "f/1"};
        for (int index = 0; index < fields.length; index++) {
            final int fieldIndex = index;
            String field = fields[index];
            for (String value : List.of("0", "2147483647", "0007")) {
                tests.add(DynamicTest.dynamicTest(field + " -> " + value, () -> {
                    Recipe recipe = parse(AddRecipeEdgeCaseTest.VALID_COMMAND
                            .replace(field, field.split("/")[0] + "/" + value));
                    int[] actual = {recipe.getCalories(), recipe.getProtein(),
                        recipe.getCarbs(), recipe.getFats()};
                    assertEquals(Integer.parseInt(value), actual[fieldIndex]);
                }));
            }
        }
        return tests.stream();
    }

    @TestFactory
    Stream<DynamicTest> parseCommand_positiveQuantities_preservesPrecision() {
        return List.of("1", "0.1", "0.000001", "0001.2500", "2147483648",
                "99999999999999999999999999999999999999.123456789")
                .stream().map(value -> DynamicTest.dynamicTest(value, () -> {
                    Recipe recipe = parse(AddRecipeEdgeCaseTest.VALID_COMMAND
                            .replace("150g", value + "g"));
                    assertEquals(new BigDecimal(value), recipe.getIngredients().get(0).getQuantity());
                }));
    }

    @TestFactory
    Stream<DynamicTest> parseCommand_letterUnits_preservesUnits() {
        return List.of("g", "kg", "ml", "mL", "cups", "pieces").stream()
                .map(unit -> DynamicTest.dynamicTest(unit, () -> {
                    Recipe recipe = parse(AddRecipeEdgeCaseTest.VALID_COMMAND
                            .replace("150g", "150" + unit));
                    assertEquals(unit, recipe.getIngredients().get(0).getUnit());
                }));
    }

    @Test
    void parseCommand_tabsAndSpaces_preservesNames() throws MishMashException {
        Recipe recipe = parse("  add-recipe\t n/ Grandma's Rice Bowl   cal/200\t p/4 c/45 f/1"
                + " i/ White Rice : 150.50 g   ");
        assertEquals("Grandma's Rice Bowl", recipe.getName());
        assertEquals("White Rice", recipe.getIngredients().get(0).getName());
        assertEquals(new BigDecimal("150.50"), recipe.getIngredients().get(0).getQuantity());
    }

    @Test
    void parseCommand_reorderedRepeatedIngredients_preservesOrder() throws MishMashException {
        Recipe recipe = parse("add-recipe i/Rice:1g f/0 i/Oil:2ml c/0 n/饭 p/0 cal/0 i/Rice:3g");
        assertEquals("饭", recipe.getName());
        assertEquals(List.of("Rice", "Oil", "Rice"), recipe.getIngredients().stream()
                .map(GroceryItem::getName).toList());
        assertEquals(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")),
                recipe.getIngredients().stream().map(GroceryItem::getQuantity).toList());
    }

    /**
     * Parses and executes a command in a fresh book.
     *
     * @param command The command to execute.
     * @return The added recipe.
     * @throws MishMashException If parsing fails.
     */
    private Recipe parse(String command) throws MishMashException {
        RecipeBook book = new RecipeBook();
        new Parser().parseCommand(command).execute(book);
        assertEquals(1, book.getRecipes().size());
        return book.getRecipes().get(0);
    }
}
