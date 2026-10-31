package mishmash.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import mishmash.model.IngredientCustomization;

/**
 * Checks strict parsing of selected positions and complete planned-meal ingredient replacements.
 */
class CustomizeMealParserTest {
    private final CustomizeMealParser parser = new CustomizeMealParser();

    @Test
    void parse_validFields_acceptsAnyOrderRepeatedIngredientsAndDecimals() {
        IngredientCustomization customization = parser.parse(
                "i/Brown Rice:125.5g m/02 d/1 i/Tofu:200g");

        assertEquals(1, customization.dayNumber());
        assertEquals(2, customization.mealNumber());
        assertEquals(List.of("Brown Rice", "Tofu"), customization.ingredients().stream()
                .map(item -> item.getName()).toList());
        assertEquals(new BigDecimal("125.5"), customization.ingredients().get(0).getQuantity());
    }

    @Test
    void parse_missingDuplicateUnknownOrUnprefixedFields_rejectsInput() {
        List<String> invalidArguments = List.of(
                "", "d/1 m/1", "d/1 i/Rice:1g", "m/1 i/Rice:1g",
                "d/1 d/2 m/1 i/Rice:1g", "d/1 m/1 x/Rice:1g",
                "unexpected d/1 m/1 i/Rice:1g", "d/1 m/1 i/");

        for (String arguments : invalidArguments) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(arguments), arguments);
        }
    }

    @Test
    void parse_invalidPositionsAndIngredients_rejectsInput() {
        List<String> invalidArguments = List.of(
                "d/0 m/1 i/Rice:1g", "d/1 m/-1 i/Rice:1g",
                "d/2147483648 m/1 i/Rice:1g", "d/1 m/1.5 i/Rice:1g",
                "d/1 m/1 i/Rice:0g", "d/1 m/1 i/Rice:-1g",
                "d/1 m/1 i/:1g", "d/1 m/1 i/Rice:1", "d/1 m/1 i/Rice:1g/ml");

        for (String arguments : invalidArguments) {
            assertThrows(IllegalArgumentException.class, () -> parser.parse(arguments), arguments);
        }
    }
}
